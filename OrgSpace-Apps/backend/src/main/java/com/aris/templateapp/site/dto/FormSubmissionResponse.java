package com.aris.templateapp.site.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record FormSubmissionResponse(
        UUID id,
        UUID siteId,
        String formName,
        Map<String, Object> data,
        Instant createdAt
) {
}
