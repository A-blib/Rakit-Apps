package com.aris.templateapp.project.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Halaman kelola website dan editor: data kartu + isi editor + info terbit.
 *
 * @param content        isi editor (format values.json)
 * @param contentVersion versi isi; dikirim balik sebagai baseVersion saat sinkron
 * @param publishedAt    terakhir diterbitkan, null jika belum pernah
 * @param openRequest      permintaan "Minta diterbitkan" yang masih terbuka, jika ada
 * @param suspensionReason alasan dari tim OrgSpace jika website ditangguhkan
 */
public record WebsiteDetailResponse(
        WebsiteSummaryResponse website,
        Map<String, Object> content,
        int contentVersion,
        Instant publishedAt,
        PublishRequestResponse openRequest,
        String suspensionReason) {
}
