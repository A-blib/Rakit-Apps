package com.aris.templateapp.gallery.dto;

import com.aris.templateapp.user.WebsitePurpose;

import java.time.Instant;
import java.util.UUID;

/**
 * Satu kartu di galeri Template (alur-pembuatan-website.md bagian 6.2).
 *
 * @param downloads total download sejak tayang
 */
public record GalleryTemplateResponse(
        UUID id,
        String name,
        WebsitePurpose category,
        String thumbnailUrl,
        String creatorName,
        long downloads,
        Instant publishedAt) {
}
