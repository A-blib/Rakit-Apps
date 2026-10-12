package com.aris.templateapp.template.dto;

import com.aris.templateapp.organization.OrgCategory;
import com.aris.templateapp.template.TemplateStatus;

import java.time.Instant;
import java.util.UUID;

/** Satu kartu di daftar "Template Anda" (bagian 6.2). Angka dilihat/didownload adalah total sejak awal. */
public record TemplateSummaryResponse(
        UUID id,
        String name,
        OrgCategory category,
        String thumbnailUrl,
        TemplateStatus status,
        int errorCount,
        int warningCount,
        long views,
        long downloads,
        Instant updatedAt) {
}
