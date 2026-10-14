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
        String compiledHtml
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
            String publishedAt
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
