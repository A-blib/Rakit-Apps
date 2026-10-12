package com.aris.templateapp.data.remote.dto;

/** Body POST /invitations/preview dan /invitations/accept (positionTitle hanya untuk accept). */
public class InvitationCodeRequestDto {
    public final String code;
    public final String positionTitle;

    public InvitationCodeRequestDto(String code, String positionTitle) {
        this.code = code;
        this.positionTitle = positionTitle;
    }
}
