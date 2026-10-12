package com.aris.templateapp.data.remote.dto;

import java.util.List;

/** Satu halaman "Template Anda" + jumlah per status (GET /providers/me/templates). */
public class TemplateListDto {
    public List<TemplateSummaryDto> items;
    public int page;
    public int size;
    public long totalItems;
    public int totalPages;
    public StatusCountsDto counts;

    public static class TemplateSummaryDto {
        public String id;
        public String name;
        public String category;
        public String thumbnailUrl;
        public String status;
        public int errorCount;
        public int warningCount;
        public long views;
        public long downloads;
        /** Waktu ISO-8601. */
        public String updatedAt;
    }

    public static class StatusCountsDto {
        public long all;
        public long published;
        public long needsFix;
        public long checking;
        public long draft;
        public long disabled;
    }
}
