package com.aris.templateapp.project.dto;

import java.time.Instant;

/** Kunci edit berlaku sampai {@code expiresAt}; app memperpanjangnya tiap 45 detik selama editor terbuka. */
public record LockResponse(Instant expiresAt) {
}
