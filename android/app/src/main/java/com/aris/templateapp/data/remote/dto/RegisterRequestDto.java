package com.aris.templateapp.data.remote.dto;

/** Body /auth/register. */
public class RegisterRequestDto {
    public final String displayName;
    public final String email;
    public final String password;

    public RegisterRequestDto(String displayName, String email, String password) {
        this.displayName = displayName;
        this.email = email;
        this.password = password;
    }
}
