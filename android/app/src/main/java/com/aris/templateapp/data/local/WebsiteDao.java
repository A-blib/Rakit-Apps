package com.aris.templateapp.data.local;

import androidx.annotation.WorkerThread;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

/** Cache tab Website. Dipanggil dari thread latar (repository), jadi tidak memakai LiveData. */
@Dao
public abstract class WebsiteDao {

    @WorkerThread
    @Query("SELECT * FROM websites WHERE organization_id = :organizationId ORDER BY updated_at DESC")
    public abstract List<WebsiteEntity> forOrganization(String organizationId);

    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void upsert(List<WebsiteEntity> websites);

    @WorkerThread
    @Query("DELETE FROM websites WHERE organization_id = :organizationId")
    public abstract void clear(String organizationId);

    @WorkerThread
    @Query("DELETE FROM websites WHERE id = :id")
    public abstract void delete(String id);

    /** Halaman pertama daftar lengkap menggantikan isi cache ruang kerja itu (website yang dihapus ikut hilang). */
    @WorkerThread
    @Transaction
    public void replace(String organizationId, List<WebsiteEntity> websites) {
        clear(organizationId);
        upsert(websites);
    }

    /** Keluar akun: cache semua ruang kerja dibuang. */
    @WorkerThread
    @Query("DELETE FROM websites")
    public abstract void clearAll();
}
