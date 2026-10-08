package com.aris.templateapp.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.aris.templateapp.data.model.ProjectStatus;

import java.util.List;

/**
 * Query tabel {@code projects}. Method yang mengembalikan {@link LiveData} otomatis memberi data baru setiap kali
 * tabel berubah (Room memantau tabelnya), jadi layar tidak perlu memuat ulang sendiri setelah ganti nama/hapus.
 * <p>
 * Filter: {@code :status} / {@code :search} bernilai null = tanpa filter itu. {@code :search} sudah berisi pola
 * LIKE yang di-escape (lihat ProjectRepository), sehingga % dan _ dari user dicari sebagai huruf biasa.
 */
@Dao
public interface ProjectDao {

    String FILTER = " WHERE (:status IS NULL OR status = :status)"
            + " AND (:search IS NULL OR name LIKE :search ESCAPE '\\')";

    @Query("SELECT * FROM projects" + FILTER + " ORDER BY updated_at DESC, id")
    LiveData<List<ProjectEntity>> observeByUpdated(ProjectStatus status, String search);

    @Query("SELECT * FROM projects" + FILTER + " ORDER BY created_at DESC, id")
    LiveData<List<ProjectEntity>> observeByCreated(ProjectStatus status, String search);

    /** COLLATE NOCASE: "apel" dan "Apel" diurutkan sebagai huruf yang sama. */
    @Query("SELECT * FROM projects" + FILTER + " ORDER BY name COLLATE NOCASE ASC, id")
    LiveData<List<ProjectEntity>> observeByName(ProjectStatus status, String search);

    /** Jumlah per chip status. Pencarian ikut berlaku, filter status tidak (agar semua chip terisi). */
    @Query("SELECT count(*) AS total,"
            + " COALESCE(sum(status = 'draft'), 0) AS draft,"
            + " COALESCE(sum(status = 'ready'), 0) AS ready,"
            + " COALESCE(sum(status = 'exported'), 0) AS exported"
            + " FROM projects WHERE (:search IS NULL OR name LIKE :search ESCAPE '\\')")
    LiveData<ProjectCounts> observeCounts(String search);

    /** "Lanjutkan project" di Beranda: 1 kartu besar + maksimal 3 kartu kecil. */
    @Query("SELECT * FROM projects ORDER BY updated_at DESC, id LIMIT :limit")
    LiveData<List<ProjectEntity>> observeRecent(int limit);

    @Query("SELECT * FROM projects WHERE id = :id")
    ProjectEntity findById(String id);

    @Insert
    void insert(ProjectEntity project);

    @Insert
    void insertAll(List<ProjectEntity> projects);

    @Query("UPDATE projects SET name = :name WHERE id = :id")
    void rename(String id, String name);

    @Query("DELETE FROM projects WHERE id = :id")
    void delete(String id);

    @Query("DELETE FROM projects WHERE is_sample = 1")
    int deleteSamples();

    /** Nama yang sudah dipakai: nama dasar dan "nama (n)", untuk memberi nomor project baru (bagian 3). */
    @Query("SELECT name FROM projects WHERE name = :base OR name LIKE :numbered ESCAPE '\\'")
    List<String> namesLike(String base, String numbered);

    /** Hasil autosave editor: status & jumlah isian belum lengkap dihitung ulang setiap simpan (bagian 7.1). */
    @Query("UPDATE projects SET status = :status, missing_count = :missingCount, updated_at = :updatedAt WHERE id = :id")
    void updateContent(String id, ProjectStatus status, int missingCount, long updatedAt);

    @Query("UPDATE projects SET thumbnail_path = :path WHERE id = :id")
    void updateThumbnail(String id, String path);

    @Query("UPDATE projects SET status = 'exported', last_exported_at = :exportedAt WHERE id = :id")
    void markExported(String id, long exportedAt);
}
