package com.aris.templateapp.organization;

/**
 * Aksi di ruang kerja beserta peran minimal yang boleh melakukannya (tabel revisi-fondasi-orgspace.md 3.1).
 * Fitur lain (publish, konten, data masuk) memakai konstanta yang sama, sehingga aturan peran ada di satu tempat.
 */
public enum OrgAction {

    VIEW(MemberRole.EDITOR),
    EDIT_DRAFT(MemberRole.EDITOR),
    MANAGE_CONTENT(MemberRole.EDITOR),
    PUBLISH(MemberRole.ADMIN),
    MANAGE_SITE_SETTINGS(MemberRole.ADMIN),
    VIEW_SUBMISSIONS(MemberRole.ADMIN),
    EDIT_PROFILE(MemberRole.ADMIN),
    MANAGE_MEMBERS(MemberRole.ADMIN),
    DELETE_SITE(MemberRole.OWNER),
    MANAGE_PORTAL_CREDENTIALS(MemberRole.OWNER),
    TRANSFER_OWNERSHIP(MemberRole.OWNER),
    DELETE_ORGANIZATION(MemberRole.OWNER);

    private final MemberRole minimumRole;

    OrgAction(MemberRole minimumRole) {
        this.minimumRole = minimumRole;
    }

    public MemberRole minimumRole() {
        return minimumRole;
    }

    public boolean allowedFor(MemberRole role) {
        return role.atLeast(minimumRole);
    }
}
