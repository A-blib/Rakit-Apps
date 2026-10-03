package com.aris.templateapp.data.remote.dto;

/** Satu metode login terhubung: { provider, email, createdAt }. */
public class IdentityDto {
    public String provider;
    public String email;
    /** Waktu ISO-8601, mis. "2026-10-04T04:38:43.123Z". */
    public String createdAt;
}
