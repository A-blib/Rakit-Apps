package com.aris.templateapp.gallery.dto;

import com.aris.templateapp.user.WebsitePurpose;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Detail template di layar Unduh (alur-buat-website-via-template.md bagian 5 & 12).
 *
 * @param pages            nama halaman, mis. ["Beranda", "Tentang"]
 * @param version          versi paket; HP melewati unduhan jika paket versi ini sudah tersimpan
 * @param packageSizeBytes ukuran paket ZIP; null jika template belum punya paket (template demo lama)
 */
public record TemplateDetailResponse(
        UUID id,
        String name,
        WebsitePurpose category,
        String description,
        List<String> keywords,
        String creatorName,
        String thumbnailUrl,
        long downloads,
        Instant publishedAt,
        List<String> pages,
        List<String> libraries,
        boolean responsive,
        int version,
        Long packageSizeBytes) {
}
