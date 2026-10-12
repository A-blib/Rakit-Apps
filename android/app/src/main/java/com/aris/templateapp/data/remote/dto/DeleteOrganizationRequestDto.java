package com.aris.templateapp.data.remote.dto;

/** Body DELETE /organizations/{id}: nama organisasi diketik ulang sebagai konfirmasi. */
public class DeleteOrganizationRequestDto {
    public final String confirmName;

    public DeleteOrganizationRequestDto(String confirmName) {
        this.confirmName = confirmName;
    }
}
