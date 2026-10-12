package com.aris.templateapp.data.model;

import androidx.annotation.Nullable;

import java.util.Objects;

/** Satu website di ruang kerja (kartu tab Website). Dibuat dari server atau dari cache Room saat offline. */
public class Website {

    public final String id;
    public final String organizationId;
    public final String name;
    public final ProjectMode mode;
    public final WebsiteStatus status;
    public final boolean hasUnpublishedChanges;
    @Nullable public final String siteUrl;
    public final int missingCount;
    @Nullable public final String thumbnailUrl;
    public final long createdAt;
    public final long updatedAt;
    @Nullable public final String updatedByName;
    /** Nama anggota lain yang sedang mengedit; null jika tidak ada. */
    @Nullable public final String lockedByName;

    public Website(String id, String organizationId, String name, ProjectMode mode, WebsiteStatus status,
                   boolean hasUnpublishedChanges, @Nullable String siteUrl, int missingCount,
                   @Nullable String thumbnailUrl, long createdAt, long updatedAt, @Nullable String updatedByName,
                   @Nullable String lockedByName) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.mode = mode;
        this.status = status;
        this.hasUnpublishedChanges = hasUnpublishedChanges;
        this.siteUrl = siteUrl;
        this.missingCount = missingCount;
        this.thumbnailUrl = thumbnailUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.updatedByName = updatedByName;
        this.lockedByName = lockedByName;
    }

    /** Online dan isi editor lebih baru dari versi online (badge "Ada perubahan belum diterbitkan"). */
    public boolean isOnlineWithChanges() {
        return status == WebsiteStatus.ONLINE && hasUnpublishedChanges;
    }

    /** Pernah diterbitkan (punya alamat): hapus wajib mengetik ulang nama. */
    public boolean wasPublished() {
        return status != WebsiteStatus.DRAFT;
    }

    // equals dipakai DiffUtil untuk tahu kartu mana yang berubah.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Website)) return false;
        Website that = (Website) o;
        return hasUnpublishedChanges == that.hasUnpublishedChanges && missingCount == that.missingCount
                && createdAt == that.createdAt && updatedAt == that.updatedAt && id.equals(that.id)
                && organizationId.equals(that.organizationId) && name.equals(that.name) && mode == that.mode
                && status == that.status && Objects.equals(siteUrl, that.siteUrl)
                && Objects.equals(thumbnailUrl, that.thumbnailUrl) && Objects.equals(updatedByName, that.updatedByName)
                && Objects.equals(lockedByName, that.lockedByName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, status, hasUnpublishedChanges, updatedAt, lockedByName);
    }
}
