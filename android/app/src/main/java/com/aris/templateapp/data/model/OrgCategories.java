package com.aris.templateapp.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Jenjang dan kategori organisasi dari backend ({@code GET /meta/categories}). Daftarnya bisa berubah tanpa update
 * app, jadi tidak ditulis di strings.xml.
 */
public class OrgCategories {

    private final List<Option> levels;
    private final List<Category> categories;

    public OrgCategories(List<Option> levels, List<Category> categories) {
        this.levels = Collections.unmodifiableList(levels);
        this.categories = Collections.unmodifiableList(categories);
    }

    public List<Option> getLevels() {
        return levels;
    }

    /** Kategori yang boleh dipilih untuk jenjang {@code level}; kosong jika jenjang belum dipilih. */
    public List<Category> categoriesFor(String level) {
        List<Category> result = new ArrayList<>();
        if (level == null) {
            return result;
        }
        for (Category category : categories) {
            if (category.levels.contains(level)) {
                result.add(category);
            }
        }
        return result;
    }

    public static class Option {
        public final String value;
        public final String label;

        public Option(String value, String label) {
            this.value = value;
            this.label = label;
        }
    }

    public static class Category extends Option {
        final Set<String> levels;

        public Category(String value, String label, Set<String> levels) {
            super(value, label);
            this.levels = levels;
        }
    }
}
