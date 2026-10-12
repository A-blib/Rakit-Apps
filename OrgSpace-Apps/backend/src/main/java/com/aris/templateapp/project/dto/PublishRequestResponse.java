package com.aris.templateapp.project.dto;

import java.time.Instant;
import java.util.UUID;

/** Satu permintaan "Minta diterbitkan" yang masih terbuka (banner di Beranda & halaman kelola). */
public record PublishRequestResponse(
        UUID id,
        UUID projectId,
        String projectName,
        String requestedByName,
        String note,
        Instant createdAt) {
}
