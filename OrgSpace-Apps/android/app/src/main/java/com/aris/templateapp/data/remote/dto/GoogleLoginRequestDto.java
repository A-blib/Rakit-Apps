package com.aris.templateapp.data.remote.dto;

/** Body /auth/google. linkToken null tidak dikirim (Gson melewati field null). */
public class GoogleLoginRequestDto {
    public final String idToken;
    public final String linkToken;

    public GoogleLoginRequestDto(String idToken, String linkToken) {
        this.idToken = idToken;
        this.linkToken = linkToken;
    }
}
