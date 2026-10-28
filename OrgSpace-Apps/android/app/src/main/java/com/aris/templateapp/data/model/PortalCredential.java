package com.aris.templateapp.data.model;

import androidx.annotation.Nullable;

import java.time.Instant;

/**
 * Username Portal Pengurus untuk satu peran organisasi. Berlaku di Portal semua website organisasi itu;
 * password-nya hanya diketahui Pemilik saat mengatur dan tidak bisa dilihat ulang.
 */
public class PortalCredential {

    public final WorkspaceRole role;
    public final String username;
    @Nullable public final Instant updatedAt;
    /** Diatur sebelum serah terima terakhir: pengurus lama masih tahu password-nya. */
    public final boolean changeRecommended;

    public PortalCredential(WorkspaceRole role, String username, @Nullable Instant updatedAt, boolean changeRecommended) {
        this.role = role;
        this.username = username;
        this.updatedAt = updatedAt;
        this.changeRecommended = changeRecommended;
    }
}
