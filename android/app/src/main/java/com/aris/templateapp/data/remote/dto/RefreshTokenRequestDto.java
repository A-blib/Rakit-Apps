package com.aris.templateapp.data.remote.dto;

/** Body untuk /auth/refresh dan /auth/logout. */
public class RefreshTokenRequestDto {
    public final String refreshToken;

    public RefreshTokenRequestDto(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
