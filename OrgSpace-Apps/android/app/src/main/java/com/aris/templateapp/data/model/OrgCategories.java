package com.aris.templateapp.data.model;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Jenjang, kategori organisasi, dan pilihan Isi template dari backend ({@code GET /meta/categories}). Daftarnya bisa
 * berubah tanpa update app, jadi tidak ditulis di strings.xml.
 */
public class OrgCategories {

    private final List<Option> levels;
    private final List<Category> categories;
    private final List<ContentTag> contentTags;

    public OrgCategories(List<Option> levels, List<Category> categories, List<ContentTag> contentTags) {
        this.levels = Collections.unmodifiableList(levels);
        this.categories = Collections.unmodifiableList(categories);
        this.contentTags = Collections.unmodifiableList(contentTags);
    }

    /** Semua kategori (tamu & ruang pribadi tidak punya jenjang). */
    public List<Category> getCategories() {
        return categories;
    }

    public List<ContentTag> getContentTags() {
        return contentTags;
    }

    /** Label kategori untuk kode server; kode tidak dikenal ditampilkan apa adanya. */
    public String categoryLabel(@Nullable String value) {
        for (Category category : categories) {
            if (category.value.equals(value)) {
                return category.label;
            }
        }
        return value == null ? "" : value;
    }

    /** Label Isi template, urut sesuai daftar resmi; kode tidak dikenal dilewati. */
    public List<String> contentTagLabels(@Nullable List<String> values) {
        List<String> labels = new ArrayList<>();
        if (values == null) {
            return labels;
        }
        for (ContentTag tag : contentTags) {
            if (values.contains(tag.value)) {
                labels.add(tag.label);
            }
        }
        return labels;
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

    public static class ContentTag extends Option {
        public final boolean core;

        public ContentTag(String value, String label, boolean core) {
            super(value, label);
            this.core = core;
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
