package com.aris.templateapp.docs.dto;

import java.time.Instant;
import java.util.UUID;

/** Artikel tanpa isi (untuk daftar & menu samping). {@code status} hanya berarti di endpoint admin. */
public record DocSummaryResponse(UUID id, String slug, String category, String audience, String title, String summary,
                                 int sortOrder, String status, Instant publishedAt, Instant updatedAt) {
}
