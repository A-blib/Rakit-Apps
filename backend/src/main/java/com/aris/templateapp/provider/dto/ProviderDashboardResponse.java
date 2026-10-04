package com.aris.templateapp.provider.dto;

import java.util.List;

/**
 * Semua data Beranda provider dalam satu respons, agar layar cukup memanggil satu endpoint
 * (alur-provider.md bagian 3.1).
 *
 * @param hasTemplates false → app menampilkan checklist provider baru, bukan ringkasan/grafik/populer
 * @param actionItems  isi "Perlu tindakan"; jumlahnya menjadi badge tab Beranda
 */
public record ProviderDashboardResponse(
        String creatorName,
        boolean hasTemplates,
        boolean profileComplete,
        List<ActionItemResponse> actionItems,
        SummaryResponse summary,
        List<DailyCountResponse> downloadTrend,
        List<PopularTemplateResponse> popular) {
}
