package com.aris.templateapp.data.repository;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.data.local.ProjectCounts;
import com.aris.templateapp.data.local.ProjectDao;
import com.aris.templateapp.data.local.ProjectEntity;
import com.aris.templateapp.data.model.ProjectFilter;
import com.aris.templateapp.data.model.ProjectStatus;

import java.time.Clock;
import java.util.List;
import java.util.Locale;
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
    private final Clock clock;

    @Inject
    public ProjectRepository(ProjectDao dao, AppExecutors executors) {
        this(dao, executors, Clock.systemUTC());
    }

    ProjectRepository(ProjectDao dao, AppExecutors executors, Clock clock) {
        this.dao = dao;
        this.executors = executors;
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
            if (source != null) {
                dao.insert(copyOf(source, copyName, UUID.randomUUID().toString(), clock.millis()));
            }
        });
    }

    public void delete(String id) {
        executors.diskIO().execute(() -> dao.delete(id));
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
        String escaped = query.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
