package com.aris.templateapp.data.remote.dto;

/** Peran yang sudah punya kredensial Portal; password tidak pernah dikirim server. */
public class PortalCredentialDto {
    public String role;
    public String username;
    public String updatedAt;
    public boolean changeRecommended;
}
