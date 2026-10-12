package com.aris.templateapp.data.remote.dto;

/** keepMeAs: "admin", "editor", atau "leave" (Pemilik lama keluar). */
public class TransferOwnershipRequestDto {
    public final String userId;
    public final String keepMeAs;

    public TransferOwnershipRequestDto(String userId, String keepMeAs) {
        this.userId = userId;
        this.keepMeAs = keepMeAs;
    }
}
