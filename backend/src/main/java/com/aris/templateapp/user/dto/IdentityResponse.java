package com.aris.templateapp.user.dto;

import com.aris.templateapp.auth.IdentityProvider;

import java.time.Instant;

/** Satu metode login yang tersambung, untuk layar Pengaturan → Metode login terhubung. */
public record IdentityResponse(IdentityProvider provider, String email, Instant createdAt) {
}
