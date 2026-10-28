package com.aris.templateapp.site.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Data lengkap yang dikirim ke Sistem Hosting Publik (OrgSpace-Hosting)
 * untuk me-render website user secara dinamis (mode template maupun mode rakit/assembled).
 */
public record SiteRenderResponse(
        UUID siteId,
        UUID projectId,
        UUID organizationId,
        String slug,
        String name,
        String organizationName,
        String mode,
        String status,
        int version,
        Instant publishedAt,
        SeoMeta seo,
        Map<String, Object> content,
        List<NewsItem> news,
        List<FormConfig> forms,
        Map<String, List<Map<String, Object>>> collections,
        String compiledHtml,
        // Modul koleksi di template (nama → field → jenis: teks/paragraf/angka/tanggal/link/gambar),
        // agar Portal Pengurus menyiapkan form isiannya sendiri.
        Map<String, Map<String, String>> modules,
        // Angka ringkasan untuk data-ws-stat (pengurus, berita, form.{nama}, {modul kustom}) dan profil umum
        // organisasi untuk data-ws-org. Tanpa nama/email anggota dan tanpa isi form.
        Map<String, Long> stats,
        Map<String, String> org,
        // Pengaturan tampilan Portal Pengurus dari portal.json template (hanya bagian valid; kosong = tampilan standar).
        Map<String, Object> portal
) {
    public record SeoMeta(
            String title,
            String description,
            String faviconUrl,
            String shareImageUrl
    ) {}

    public record NewsItem(
            UUID id,
            String title,
            String summary,
            String content,
            String publishedAt,
            String imageUrl
    ) {}

    public record FormConfig(
            String name,
            String title,
            String submitEndpoint,
            List<FormField> fields
    ) {}

    public record FormField(
            String name,
            String label,
            String type,
            boolean required,
            String placeholder
    ) {}
}
