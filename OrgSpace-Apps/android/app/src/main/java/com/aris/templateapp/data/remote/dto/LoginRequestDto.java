package com.aris.templateapp.data.remote.dto;

/** Body /auth/login. linkToken hanya diisi saat menyambungkan akun (Fase 08); null tidak dikirim oleh Gson. */
public class LoginRequestDto {
    public final String email;
    public final String password;
    public final String linkToken;

    public LoginRequestDto(String email, String password, String linkToken) {
        this.email = email;
        this.password = password;
        this.linkToken = linkToken;
    }
}
