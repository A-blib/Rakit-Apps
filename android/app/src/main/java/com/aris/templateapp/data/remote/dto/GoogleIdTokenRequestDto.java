package com.aris.templateapp.data.remote.dto;

/** Body POST /users/me/identities/google. */
public class GoogleIdTokenRequestDto {
    public final String idToken;

    public GoogleIdTokenRequestDto(String idToken) {
        this.idToken = idToken;
    }
}
