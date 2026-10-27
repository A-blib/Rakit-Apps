package com.aris.templateapp.portal.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Satu info terbaru. Di daftar (landing & dashboard) {@code content} = null agar ringan; isi lengkap hanya di detail.
 * {@code imageUpdatedAt = null} berarti tanpa gambar; jika terisi, gambar ada di
 * {@code GET /api/public/updates/{id}/image} (hanya untuk yang tayang).
 * {@code expiresAt} = {@code publishedAt + displayDays}; null jika tanpa batas atau belum pernah tayang. Lewat waktu itu,
 * info tidak tampil di publik.
 */
public record PortalUpdateResponse(
        UUID id,
        String category,
        String status,
        String title,
        String summary,
        String content,
        String imageAlt,
        Instant imageUpdatedAt,
        Instant publishedAt,
        Integer displayDays,
        Instant expiresAt,
        // true = tayang tapi sudah lewat masa tampil (tidak terlihat publik); dihitung dengan jam server.
        boolean expired,
        Instant updatedAt) {
}
