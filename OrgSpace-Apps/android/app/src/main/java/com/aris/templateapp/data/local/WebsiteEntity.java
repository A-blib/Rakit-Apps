package com.aris.templateapp.data.local;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

/**
 * Cache tab Website per ruang kerja (alur-pembuatan-website.md bagian 10). Sumber kebenaran tetap server; tabel
 * ini hanya agar daftar bisa dibuka saat offline. Mode & status disimpan sebagai teks nilai server.
 * <p>
 * Kunci gabungan (ruang kerja, id) karena website yang sama tidak pernah pindah ruang kerja, dan membersihkan
 * cache satu ruang kerja cukup dengan satu DELETE.
 */
@Entity(tableName = "websites", primaryKeys = {"organization_id", "id"},
        indices = {@Index(value = {"organization_id", "updated_at"})})
public class WebsiteEntity {

    @NonNull
    @ColumnInfo(name = "organization_id")
    public String organizationId = "";

    @NonNull
    public String id = "";

    @NonNull
    public String name = "";

    @NonNull
    public String mode = "template";

    @NonNull
    public String status = "draft";

    @ColumnInfo(name = "has_unpublished_changes")
    public boolean hasUnpublishedChanges;

    @Nullable
    @ColumnInfo(name = "site_url")
    public String siteUrl;

    @ColumnInfo(name = "missing_count")
    public int missingCount;

    @Nullable
    @ColumnInfo(name = "thumbnail_url")
    public String thumbnailUrl;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    @Nullable
    @ColumnInfo(name = "updated_by_name")
    public String updatedByName;

    @Nullable
    @ColumnInfo(name = "locked_by_name")
    public String lockedByName;

    /** Kapan baris ini diambil dari server (teks "Data terakhir disimpan ..."). */
    @ColumnInfo(name = "cached_at")
    public long cachedAt;
}
