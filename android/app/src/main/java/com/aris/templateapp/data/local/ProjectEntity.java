package com.aris.templateapp.data.local;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.aris.templateapp.data.model.ProjectMode;
import com.aris.templateapp.data.model.ProjectStatus;

/**
 * Satu baris tabel {@code projects} di HP (alur-pembuatan-website.md bagian 4.6). Versi awal hanya METADATA;
 * isi website (file JSON di {@link #contentPath}) formatnya menunggu diskusi editor.
 * <p>
 * Room memetakan field publik ke kolom. Waktu disimpan sebagai epoch milidetik (angka) karena SQLite tidak punya
 * tipe tanggal; urutan "terakhir diedit" cukup membandingkan angka.
 */
@Entity(tableName = "projects", indices = {@Index("updated_at"), @Index("status")})
public class ProjectEntity {

    @PrimaryKey
    @NonNull
    public String id;

    @NonNull
    public String name;

    @NonNull
    public ProjectMode mode;

    /** Template asal (null untuk custom); dipakai menghitung "Didownload" provider saat export pertama. */
    @Nullable
    @ColumnInfo(name = "source_template_id")
    public String sourceTemplateId;

    /** Versi paket template yang dipakai project ini (null untuk custom). */
    @Nullable
    @ColumnInfo(name = "template_version")
    public Integer templateVersion;

    @NonNull
    public ProjectStatus status;

    /** Jumlah isian yang belum lengkap, untuk teks "3 isian belum diisi". */
    @ColumnInfo(name = "missing_count")
    public int missingCount;

    @Nullable
    @ColumnInfo(name = "thumbnail_path")
    public String thumbnailPath;

    @Nullable
    @ColumnInfo(name = "content_path")
    public String contentPath;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;

    @Nullable
    @ColumnInfo(name = "last_exported_at")
    public Long lastExportedAt;

    /**
     * true untuk project dari tombol debug "Isi project contoh". Tombol "Hapus project contoh" hanya menghapus
     * baris ini, sehingga project buatan user sendiri tidak ikut terhapus.
     */
    @ColumnInfo(name = "is_sample", defaultValue = "0")
    public boolean sample;

    public ProjectEntity(@NonNull String id, @NonNull String name, @NonNull ProjectMode mode,
                         @NonNull ProjectStatus status, long createdAt, long updatedAt) {
        this.id = id;
        this.name = name;
        this.mode = mode;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Sudah pernah diexport, tetapi diedit lagi setelahnya: file ZIP yang sudah online ketinggalan (bagian 4.2). */
    public boolean hasChangesSinceExport() {
        return status == ProjectStatus.EXPORTED && lastExportedAt != null && updatedAt > lastExportedAt;
    }
}
