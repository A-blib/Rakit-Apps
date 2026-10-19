package com.aris.templateapp.site.dto;

import java.time.Instant;
import java.util.UUID;

public record SiteResponse(
        UUID id,
        UUID projectId,
        String slug,
        String url,
        String status,
        Integer version,
        Instant publishedAt,
        String publishedByName,
        boolean hasUnpublishedChanges,
        String seoTitle,
        String seoDescription,
        String customDomain) {
}
