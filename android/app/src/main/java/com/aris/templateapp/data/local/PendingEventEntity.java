package com.aris.templateapp.data.local;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Event statistik provider yang belum terkirim karena offline (alur-buat-website-via-template.md bagian 10).
 * Dikirim WorkManager saat ada koneksi, lalu barisnya dihapus.
 */
@Entity(tableName = "pending_events")
public class PendingEventEntity {

    @PrimaryKey
    @NonNull
    public String id;

    @NonNull
    @ColumnInfo(name = "template_id")
    public String templateId;

    /** "view" atau "download". */
    @NonNull
    public String type;

    @Nullable
    @ColumnInfo(name = "project_id")
    public String projectId;

    /** Waktu kejadian di HP (ISO-8601), dikirim apa adanya agar statistik masuk ke hari yang benar. */
    @NonNull
    @ColumnInfo(name = "occurred_at")
    public String occurredAt;

    public int attempts;

    public PendingEventEntity(@NonNull String id, @NonNull String templateId, @NonNull String type,
                              @NonNull String occurredAt) {
        this.id = id;
        this.templateId = templateId;
        this.type = type;
        this.occurredAt = occurredAt;
    }
}
