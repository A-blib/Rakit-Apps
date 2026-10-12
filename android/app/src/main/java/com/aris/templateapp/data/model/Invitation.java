package com.aris.templateapp.data.model;

import androidx.annotation.Nullable;

import java.time.Instant;

/**
 * Undangan anggota. {@code code} hanya ada tepat setelah undangan dibuat (server tidak menyimpan kode aslinya),
 * jadi undangan di daftar "Undangan aktif" tidak punya kode.
 */
public class Invitation {

    public final String id;
    public final WorkspaceRole role;
    @Nullable public final String code;
    @Nullable public final String createdByName;
    @Nullable public final Instant expiresAt;

    public Invitation(String id, WorkspaceRole role, @Nullable String code, @Nullable String createdByName,
                      @Nullable Instant expiresAt) {
        this.id = id;
        this.role = role;
        this.code = code;
        this.createdByName = createdByName;
        this.expiresAt = expiresAt;
    }
}
