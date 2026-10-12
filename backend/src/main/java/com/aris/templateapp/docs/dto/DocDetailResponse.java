package com.aris.templateapp.docs.dto;

import java.time.Instant;
import java.util.UUID;

/** Artikel lengkap. {@code body} berformat Markdown (tabel GFM boleh); web yang merender. */
public record DocDetailResponse(UUID id, String slug, String category, String categoryLabel, String audience,
                                String title, String summary, String body, int sortOrder, String status,
                                Instant publishedAt, Instant updatedAt) {
}
