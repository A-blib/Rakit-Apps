package com.aris.templateapp.data.model;

import androidx.annotation.Nullable;

/** Pratinjau kode undangan sebelum bergabung: nama, logo, dan peran yang akan didapat. */
public class InvitationPreview {

    public final String organizationName;
    @Nullable public final String logoUrl;
    public final WorkspaceRole role;

    public InvitationPreview(String organizationName, @Nullable String logoUrl, WorkspaceRole role) {
        this.organizationName = organizationName;
        this.logoUrl = logoUrl;
        this.role = role;
    }
}
