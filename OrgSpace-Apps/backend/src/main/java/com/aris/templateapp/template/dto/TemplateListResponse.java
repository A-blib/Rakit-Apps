package com.aris.templateapp.template.dto;

import java.util.List;

/** Satu halaman daftar "Template Anda" + jumlah per status untuk chip filter. */
public record TemplateListResponse(
        List<TemplateSummaryResponse> items,
        int page,
        int size,
        long totalItems,
        int totalPages,
        StatusCountsResponse counts) {
}
