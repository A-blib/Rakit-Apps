package com.aris.templateapp.project.dto;

import java.time.Instant;
import java.util.UUID;

/** Satu cadangan isi website (menu ⋮ → Cadangan). */
public record BackupResponse(UUID id, String reason, Instant createdAt, String createdByName) {
}
