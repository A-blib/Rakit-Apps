package com.aris.templateapp.portal.dto;

import java.time.Instant;
import java.util.List;

/**
 * Isi hero landing page. {@code imageUpdatedAt = null} berarti belum ada gambar unggahan (web memakai gambar
 * bawaannya); jika terisi, gambar ada di {@code GET /api/public/landing/hero/image}.
 */
public record LandingHeroResponse(
        String headline,
        String headlineHighlight,
        String subheadline,
        String primaryCtaLabel,
        String secondaryCtaLabel,
        String secondaryCtaUrl,
        List<String> highlights,
        String imageAlt,
        Instant imageUpdatedAt,
        Instant updatedAt) {
}
