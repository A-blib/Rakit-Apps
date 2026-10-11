package com.aris.templateapp.data.remote.dto;

import java.util.List;

/** GET /meta/categories: jenjang + kategori organisasi beserta jenjang yang boleh memilihnya. */
public class CategoriesDto {
    public List<Level> levels;
    public List<Category> categories;

    public static class Level {
        public String value;
        public String label;
    }

    public static class Category {
        public String value;
        public String label;
        public List<String> levels;
    }
}
