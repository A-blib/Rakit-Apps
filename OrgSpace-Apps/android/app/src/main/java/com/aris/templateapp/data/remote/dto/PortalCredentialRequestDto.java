package com.aris.templateapp.data.remote.dto;

public class PortalCredentialRequestDto {
    public final String username;
    public final String password;

    public PortalCredentialRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
