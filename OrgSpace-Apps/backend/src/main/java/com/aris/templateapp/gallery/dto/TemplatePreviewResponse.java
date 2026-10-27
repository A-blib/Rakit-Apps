package com.aris.templateapp.gallery.dto;

import com.aris.templateapp.site.dto.SiteRenderResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Data contoh untuk preview template di OrgSpace-Hosting. Bentuknya sama dengan bagian modul
 * {@link SiteRenderResponse}, agar Hosting mengisi modul data-ws-* dengan runtime yang sama. Semua isinya buatan,
 * tidak pernah diambil dari website atau organisasi user.
 */
public record TemplatePreviewResponse(
        UUID templateId,
        String name,
        List<SiteRenderResponse.NewsItem> news,
        Map<String, List<Map<String, Object>>> collections,
        Map<String, Map<String, String>> modules,
        Map<String, Long> stats,
        Map<String, String> org) {
}
