package com.aris.templateapp.data.repository;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;

import com.aris.templateapp.core.template.ProjectStore;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.data.local.ProjectCounts;
import com.aris.templateapp.data.local.ProjectDao;
import com.aris.templateapp.data.local.ProjectEntity;
import com.aris.templateapp.data.model.ProjectFilter;
import com.aris.templateapp.data.model.ProjectStatus;

import java.io.IOException;
import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Project milik user, tersimpan di HP (Room), jadi semuanya tetap jalan tanpa internet. Membaca memakai LiveData
 * (Room menjalankan query di thread latar sendiri); menulis dijalankan di {@link AppExecutors#diskIO()}.
 */
@Singleton
public class ProjectRepository {

    private final ProjectDao dao;
    private final AppExecutors executors;
    private final ProjectStore store;
    private final Clock clock;

    @Inject
    public ProjectRepository(ProjectDao dao, AppExecutors executors, ProjectStore store) {
        this(dao, executors, store, Clock.systemUTC());
    }

    ProjectRepository(ProjectDao dao, AppExecutors executors, ProjectStore store, Clock clock) {
        this.dao = dao;
        this.executors = executors;
        this.store = store;
        this.clock = clock;
    }

    public LiveData<List<ProjectEntity>> observe(ProjectFilter filter) {
        String search = likePattern(filter.query);
        switch (filter.sort) {
            case CREATED:
                return dao.observeByCreated(filter.status, search);
            case NAME:
                return dao.observeByName(filter.status, search);
            case UPDATED:
            default:
                return dao.observeByUpdated(filter.status, search);
        }
    }

    /** @param query kata kunci; kosong = jumlah seluruh project (dipakai juga Beranda & Profil). */
    public LiveData<ProjectCounts> observeCounts(String query) {
        return dao.observeCounts(likePattern(query));
    }

    public LiveData<List<ProjectEntity>> observeRecent(int limit) {
        return dao.observeRecent(limit);
    }

    /**
     * Ganti nama TIDAK mengubah {@code updated_at}: "Diedit … lalu" dan "Ada perubahan sejak export terakhir"
     * menyangkut isi website, sedangkan nama project tidak ikut ke file ZIP.
     */
    public void rename(String id, String name) {
        executors.diskIO().execute(() -> dao.rename(id, name.trim()));
    }

    /**
     * Salinan berdiri sendiri: id & waktu baru, belum pernah diexport. Salinan project yang sudah diexport
     * berstatus "Siap export" (isinya lengkap, tetapi salinan ini sendiri belum pernah diexport).
     */
    public void duplicate(String id, String copyName) {
        executors.diskIO().execute(() -> {
            ProjectEntity source = dao.findById(id);
            if (source == null) {
                return;
            }
            ProjectEntity copy = copyOf(source, copyName, UUID.randomUUID().toString(), clock.millis());
            if (source.contentPath != null) {
                // Isi website (values.json + gambar) ikut disalin agar salinan bisa diedit terpisah.
                try {
                    store.copy(source.id, copy.id);
                    copy.contentPath = store.dir(copy.id).getAbsolutePath();
                } catch (IOException e) {
                    store.delete(copy.id);
                    return;
                }
            }
            dao.insert(copy);
        });
    }

    /** Menghapus baris project beserta folder isinya (values.json, gambar, thumbnail). */
    public void delete(String id) {
        executors.diskIO().execute(() -> {
            dao.delete(id);
            store.delete(id);
        });
    }

    // ---------- editor template mode (alur-buat-website-via-template.md bagian 6.7) ----------

    @WorkerThread
    @Nullable
    public ProjectEntity findSync(String id) {
        return dao.findById(id);
    }

    /** "Toko Kue" → "Toko Kue" jika belum dipakai, selain itu "Toko Kue (2)", "Toko Kue (3)", ... */
    @WorkerThread
    public String uniqueName(String base) {
        String trimmed = base.trim();
        Set<String> used = new HashSet<>(dao.namesLike(trimmed, escapeLike(trimmed) + " (%)"));
        return nextName(trimmed, used);
    }

    static String nextName(String base, Set<String> used) {
        if (!used.contains(base)) {
            return base;
        }
        int n = 2;
        while (used.contains(base + " (" + n + ")")) {
            n++;
        }
        return base + " (" + n + ")";
    }

    @WorkerThread
    public void insertSync(ProjectEntity project) {
        dao.insert(project);
    }

    @WorkerThread
    public void updateContent(String id, ProjectStatus status, int missingCount, long updatedAt) {
        dao.updateContent(id, status, missingCount, updatedAt);
    }

    @WorkerThread
    public void updateThumbnail(String id, String path) {
        dao.updateThumbnail(id, path);
    }

    @WorkerThread
    public void markExported(String id, long exportedAt) {
        dao.markExported(id, exportedAt);
    }

    public long now() {
        return clock.millis();
    }

    /** Dipakai tombol debug "Isi project contoh". */
    public void insertAll(List<ProjectEntity> projects) {
        executors.diskIO().execute(() -> dao.insertAll(projects));
    }

    /** Tombol debug "Hapus project contoh": hanya project contoh, project buatan user tidak ikut terhapus. */
    public void deleteSamples(Consumer<Integer> onDeleted) {
        executors.diskIO().execute(() -> {
            int count = dao.deleteSamples();
            executors.mainThread().execute(() -> onDeleted.accept(count));
        });
    }

    static ProjectEntity copyOf(ProjectEntity source, String name, String newId, long now) {
        ProjectStatus status = source.status == ProjectStatus.EXPORTED ? ProjectStatus.READY : source.status;
        ProjectEntity copy = new ProjectEntity(newId, name, source.mode, status, now, now);
        copy.sourceTemplateId = source.sourceTemplateId;
        copy.templateVersion = source.templateVersion;
        copy.missingCount = source.missingCount;
        copy.sample = source.sample;
        return copy;
    }

    /**
     * Kata kunci → pola LIKE "%kata%" dengan \, % dan _ di-escape, agar "50%" dicari sebagai teks biasa.
     * Kosong → null (tanpa filter). LIKE di SQLite sudah tidak membedakan huruf besar/kecil untuk huruf latin.
     */
    @Nullable
    static String likePattern(@Nullable String query) {
        if (query == null || query.trim().isEmpty()) {
            return null;
        }
        return "%" + escapeLike(query.trim().toLowerCase(Locale.ROOT)) + "%";
    }

    static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
