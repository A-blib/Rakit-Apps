package com.aris.templateapp.data.model;

import androidx.annotation.Nullable;

/** Satu anggota ruang kerja. */
public class Member {

    public final String userId;
    public final String displayName;
    public final WorkspaceRole role;
    @Nullable public final String positionTitle;

    public Member(String userId, String displayName, WorkspaceRole role, @Nullable String positionTitle) {
        this.userId = userId;
        this.displayName = displayName;
        this.role = role;
        this.positionTitle = positionTitle;
    }
}
