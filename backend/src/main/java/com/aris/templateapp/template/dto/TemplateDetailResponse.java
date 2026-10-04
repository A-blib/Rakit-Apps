package com.aris.templateapp.template.dto;

import com.aris.templateapp.template.TemplateStatus;
import com.aris.templateapp.user.WebsitePurpose;

import java.time.Instant;
import java.util.UUID;

/** Detail template sederhana (bagian 6.2). {@code latestCheck} null jika belum pernah dicek (mis. draft). */
public record TemplateDetailResponse(
        UUID id,
        String name,
        WebsitePurpose category,
        String thumbnailUrl,
        TemplateStatus status,
        int warningCount,
        long views,
        long downloads,
        Instant createdAt,
        Instant updatedAt,
        Instant publishedAt,
        CheckResponse latestCheck) {
}
