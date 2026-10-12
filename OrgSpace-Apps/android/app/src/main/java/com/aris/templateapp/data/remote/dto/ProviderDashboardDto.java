package com.aris.templateapp.data.remote.dto;

import java.util.List;

/**
 * Data Beranda provider (GET /providers/me/dashboard).
 * <p>
 * Data provider hanya ditampilkan (tidak diubah di app), jadi DTO ini dipakai langsung oleh UI tanpa
 * dipetakan ke model terpisah; nama field dijaga aturan R8 di keepRules/rules.keep.
 */
public class ProviderDashboardDto {
    public String creatorName;
    public boolean hasTemplates;
    public boolean profileComplete;
    public List<ActionItemDto> actionItems;
    public SummaryDto summary;
    public List<DailyCountDto> downloadTrend;
    public List<PopularTemplateDto> popular;

    /** Satu kartu "Perlu tindakan". */
    public static class ActionItemDto {
        public static final String CHECK_FAILED = "TEMPLATE_CHECK_FAILED";
        public static final String WARNING = "TEMPLATE_WARNING";
        public static final String DRAFT = "DRAFT";
        public static final String PROFILE_INCOMPLETE = "PROFILE_INCOMPLETE";

        public String kind;
        public String templateId;
        public String templateName;
        public int errorCount;
        public int warningCount;
    }

    public static class SummaryDto {
        public long active;
        public long views;
        public long downloads;
        public String period;
    }

    /** Satu titik grafik: tanggal "yyyy-MM-dd" + jumlah download (0 jika tidak ada). */
    public static class DailyCountDto {
        public String date;
        public long count;
    }

    public static class PopularTemplateDto {
        public int rank;
        public String id;
        public String name;
        public String thumbnailUrl;
        public long downloads;
        public long views;
    }
}
