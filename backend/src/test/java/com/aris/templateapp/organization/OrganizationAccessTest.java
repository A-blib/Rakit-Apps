package com.aris.templateapp.organization;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/** Satu baris test = satu baris tabel peran revisi-fondasi-orgspace.md bagian 3.1 (O = Pemilik, A = Admin, E = Editor). */
class OrganizationAccessTest {

    @ParameterizedTest(name = "{0}: Pemilik={1} Admin={2} Editor={3}")
    @CsvSource({
            "VIEW,                 true, true,  true",
            "EDIT_DRAFT,           true, true,  true",
            "MANAGE_CONTENT,       true, true,  true",
            "PUBLISH,              true, true,  false",
            "MANAGE_SITE_SETTINGS, true, true,  false",
            "VIEW_SUBMISSIONS,     true, true,  false",
            "EDIT_PROFILE,         true, true,  false",
            "MANAGE_MEMBERS,       true, true,  false",
            "DELETE_SITE,          true, false, false",
            "TRANSFER_OWNERSHIP,   true, false, false",
            "DELETE_ORGANIZATION,  true, false, false"})
    void actionsFollowRoleTable(OrgAction action, boolean owner, boolean admin, boolean editor) {
        assertThat(action.allowedFor(MemberRole.OWNER)).isEqualTo(owner);
        assertThat(action.allowedFor(MemberRole.ADMIN)).isEqualTo(admin);
        assertThat(action.allowedFor(MemberRole.EDITOR)).isEqualTo(editor);
    }

    @ParameterizedTest(name = "{0} mengubah {1} menjadi {2}: {3}")
    @CsvSource({
            // Admin boleh mengatur Admin/Editor, tapi tidak boleh menyentuh Pemilik.
            "ADMIN,  EDITOR, ADMIN,  true",
            "ADMIN,  ADMIN,  EDITOR, true",
            "ADMIN,  OWNER,  EDITOR, false",
            "OWNER,  ADMIN,  EDITOR, true",
            "OWNER,  OWNER,  ADMIN,  true",
            // Peran Pemilik hanya lewat serah terima.
            "OWNER,  ADMIN,  OWNER,  false",
            "ADMIN,  EDITOR, OWNER,  false",
            // Editor tidak bisa mengatur anggota sama sekali.
            "EDITOR, EDITOR, ADMIN,  false"})
    void changeRoleRules(MemberRole actor, MemberRole target, MemberRole newRole, boolean allowed) {
        assertThat(OrganizationAccess.canChangeRole(actor, target, newRole)).isEqualTo(allowed);
    }

    @ParameterizedTest(name = "{0} mengeluarkan {1}: {2}")
    @CsvSource({
            "OWNER,  ADMIN,  true",
            "OWNER,  OWNER,  true",
            "ADMIN,  EDITOR, true",
            "ADMIN,  ADMIN,  true",
            "ADMIN,  OWNER,  false",
            "EDITOR, EDITOR, false"})
    void removeRules(MemberRole actor, MemberRole target, boolean allowed) {
        assertThat(OrganizationAccess.canRemove(actor, target)).isEqualTo(allowed);
    }

    @ParameterizedTest(name = "{0} mengundang sebagai {1}: {2}")
    @CsvSource({
            "OWNER,  ADMIN,  true",
            "ADMIN,  ADMIN,  true",
            "ADMIN,  EDITOR, true",
            "OWNER,  OWNER,  false",
            "EDITOR, EDITOR, false"})
    void inviteRules(MemberRole actor, MemberRole invited, boolean allowed) {
        assertThat(OrganizationAccess.canInvite(actor, invited)).isEqualTo(allowed);
    }

    @ParameterizedTest(name = "{0} keluar saat ada {1} Pemilik: {2}")
    @CsvSource({
            "OWNER,  1, false",
            "OWNER,  2, true",
            "ADMIN,  1, true",
            "EDITOR, 1, true"})
    void leaveRules(MemberRole role, long owners, boolean allowed) {
        assertThat(OrganizationAccess.canLeave(role, owners)).isEqualTo(allowed);
    }
}
