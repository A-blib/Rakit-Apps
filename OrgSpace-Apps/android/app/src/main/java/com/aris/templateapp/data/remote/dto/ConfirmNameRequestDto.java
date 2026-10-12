package com.aris.templateapp.data.remote.dto;

/** Body DELETE /projects/{id}: wajib nama website jika website pernah diterbitkan. */
public class ConfirmNameRequestDto {
    public final String confirmName;

    public ConfirmNameRequestDto(String confirmName) {
        this.confirmName = confirmName;
    }
}
