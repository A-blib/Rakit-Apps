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
 * @param templateVersion  versi paket template yang dipakai editor
 * @param lock             pemegang kunci edit saat ini (termasuk diri sendiri), atau null jika tidak ada
 */
public record WebsiteDetailResponse(
        WebsiteSummaryResponse website,
        Map<String, Object> content,
        int contentVersion,
        Instant publishedAt,
        PublishRequestResponse openRequest,
        String suspensionReason,
        Integer templateVersion,
        Lock lock) {

    /** @param mine true jika kunci dipegang user yang meminta (boleh mengedit) */
    public record Lock(String holderName, boolean mine, Instant expiresAt) {
    }
}
