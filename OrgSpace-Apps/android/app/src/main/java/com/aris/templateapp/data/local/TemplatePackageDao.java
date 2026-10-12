package com.aris.templateapp.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/** Query tabel {@code template_packages}. Semua dipanggil dari thread latar (bukan LiveData). */
@Dao
public interface TemplatePackageDao {

    /** Versi terbaru yang tersimpan untuk satu template, atau null jika belum pernah diunduh. */
    @Query("SELECT * FROM template_packages WHERE template_id = :templateId ORDER BY version DESC LIMIT 1")
    TemplatePackageEntity findLatest(String templateId);

    @Query("SELECT * FROM template_packages WHERE template_id = :templateId AND version = :version")
    TemplatePackageEntity find(String templateId, int version);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(TemplatePackageEntity entity);

    @Query("UPDATE template_packages SET last_used_at = :now WHERE template_id = :templateId AND version = :version")
    void markUsed(String templateId, int version, long now);

    /**
     * Paket yang boleh dihapus otomatis (bagian 11.1): tidak dibuka sejak {@code :before} dan tidak dipakai project
     * mana pun.
     */
    @Query("SELECT * FROM template_packages p WHERE p.last_used_at < :before AND NOT EXISTS ("
            + " SELECT 1 FROM projects pr WHERE pr.source_template_id = p.template_id"
            + " AND (pr.template_version IS NULL OR pr.template_version = p.version))")
    List<TemplatePackageEntity> findUnused(long before);

    @Query("DELETE FROM template_packages WHERE template_id = :templateId AND version = :version")
    void delete(String templateId, int version);
}
