package com.aris.templateapp.gallery.dto;

import com.aris.templateapp.organization.OrgCategory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Satu kartu di galeri Template (alur-pembuatan-website.md bagian 6.2).
 *
 * @param downloads   total download sejak tayang ("dipakai N kali": dihitung saat website pertama terbit/diexport)
 * @param contentTags "Isi template" untuk label "Punya: Struktur · Berita · Kontak"
 */
public record GalleryTemplateResponse(
        UUID id,
        String name,
        OrgCategory category,
        String thumbnailUrl,
        String creatorName,
        long downloads,
        Instant publishedAt,
        List<String> contentTags) {
}
