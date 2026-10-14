package com.aris.templateapp.site.dto;

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
        Integer version
) {
}
