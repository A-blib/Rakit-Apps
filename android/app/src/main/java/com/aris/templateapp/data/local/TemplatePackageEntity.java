package com.aris.templateapp.data.local;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

/**
 * Paket template yang sudah diunduh ke HP (alur-buat-website-via-template.md bagian 11.1). Nama, kreator, dan
 * kategori disalin ke sini agar editor bisa dibuka tanpa internet. Isi paketnya ada di folder {@link #path}.
 */
@Entity(tableName = "template_packages", primaryKeys = {"template_id", "version"})
public class TemplatePackageEntity {

    @NonNull
    @ColumnInfo(name = "template_id")
    public String templateId;

    public int version;

    @NonNull
    public String name;

    @Nullable
    @ColumnInfo(name = "creator_name")
    public String creatorName;

    @Nullable
    public String category;

    /** Folder hasil ekstrak (absolut), mis. .../files/templates/{id}/1. */
    @NonNull
    public String path;

    @ColumnInfo(name = "size_bytes")
    public long sizeBytes;

    @ColumnInfo(name = "downloaded_at")
    public long downloadedAt;

    @ColumnInfo(name = "last_used_at")
    public long lastUsedAt;

    public TemplatePackageEntity(@NonNull String templateId, int version, @NonNull String name, @NonNull String path,
                                 long sizeBytes, long downloadedAt, long lastUsedAt) {
        this.templateId = templateId;
        this.version = version;
        this.name = name;
        this.path = path;
        this.sizeBytes = sizeBytes;
        this.downloadedAt = downloadedAt;
        this.lastUsedAt = lastUsedAt;
    }
}
