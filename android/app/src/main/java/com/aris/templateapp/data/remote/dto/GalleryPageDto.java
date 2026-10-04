package com.aris.templateapp.data.remote.dto;

import java.util.List;

/** Satu halaman galeri Template (GET /templates). */
public class GalleryPageDto {
    public List<GalleryTemplateDto> items;
    public int page;
    public int size;
    public long totalItems;
    public int totalPages;

    public static class GalleryTemplateDto {
        public String id;
        public String name;
        public String category;
        public String thumbnailUrl;
        public String creatorName;
        public long downloads;
        /** Waktu ISO-8601; bisa null. */
        public String publishedAt;
    }
}
