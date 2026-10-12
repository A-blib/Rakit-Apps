package com.aris.templateapp.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/** Antrean event statistik (tabel {@code pending_events}). */
@Dao
public interface PendingEventDao {

    @Insert
    void insert(PendingEventEntity event);

    @Query("SELECT * FROM pending_events ORDER BY occurred_at LIMIT :limit")
    List<PendingEventEntity> oldest(int limit);

    @Query("DELETE FROM pending_events WHERE id = :id")
    void delete(String id);

    @Query("UPDATE pending_events SET attempts = attempts + 1 WHERE id = :id")
    void incrementAttempts(String id);
}
