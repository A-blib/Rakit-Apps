package com.aris.templateapp.data.remote.dto;

/** Hasil masuk/daftar/refresh dari backend. */
public class AuthResponseDto {
    public String accessToken;
    public String refreshToken;
    /** Umur access token dalam detik. */
    public long expiresIn;
    public boolean isNewUser;
    public UserDto user;
}
