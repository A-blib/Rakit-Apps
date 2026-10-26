package com.aris.templateapp.site.dto;

import java.util.List;
import java.util.UUID;

public record DirectPublishResponse(
        UUID projectId,
        UUID organizationId,
        String organizationName,
        UUID siteId,
        String slug,
        String url,
        int port,
        String status,
        Integer version,
        // Kunci isian foto yang filenya tidak ikut terkirim; website memakai foto contoh template untuk isian itu.
        List<String> missingImages
) {
}
