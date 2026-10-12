package com.aris.templateapp.data.remote.dto;

/** Body PATCH /organizations/{id}/members/{userId}. Null = tidak diubah; jabatan "" = dihapus. */
public class MemberUpdateRequestDto {
    public final String role;
    public final String positionTitle;

    public MemberUpdateRequestDto(String role, String positionTitle) {
        this.role = role;
        this.positionTitle = positionTitle;
    }
}
