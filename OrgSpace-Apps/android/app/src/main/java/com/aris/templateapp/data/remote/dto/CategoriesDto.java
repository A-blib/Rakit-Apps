package com.aris.templateapp.data.remote.dto;

import java.util.List;

/** GET /meta/categories: jenjang, kategori organisasi (+ jenjang yang boleh memilihnya), dan pilihan Isi template. */
public class CategoriesDto {
    public List<Level> levels;
    public List<Category> categories;
    public List<ContentTag> contentTags;

    public static class Level {
        public String value;
        public String label;
    }

    public static class Category {
        public String value;
        public String label;
        public List<String> levels;
    }

    /** core = konten Inti organisasi (Profil, Struktur, Kontak). */
    public static class ContentTag {
        public String value;
        public String label;
        public boolean core;
    }
}
