package com.aris.templateapp.project.dto;

import java.util.List;

/**
 * Isi tab Website: satu halaman daftar + jumlah per chip filter + info batas website beralamat.
 *
 * @param sitesUsed  website ruang kerja ini yang punya alamat (online, dimatikan, ditangguhkan)
 * @param sitesLimit batas website beralamat (alur-publish-website.md D1)
 */
public record WebsiteListResponse(
        List<WebsiteSummaryResponse> items,
        Counts counts,
        int sitesUsed,
        int sitesLimit,
        int page,
        boolean hasMore) {

    /** Jumlah untuk chip "Semua · Draft · Online · Ada perubahan" (sudah memperhitungkan kata pencarian). */
    public record Counts(long all, long draft, long online, long changed) {
    }
}
