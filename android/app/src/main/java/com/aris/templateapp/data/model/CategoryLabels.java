package com.aris.templateapp.data.model;

import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Daftar kategori & Isi template terakhir yang diketahui app, untuk label di kartu galeri, Template Anda, dan detail
 * template. Diisi {@code WorkspaceRepository} dari server (atau salinan di HP saat offline), supaya label kategori
 * tidak perlu ditulis ulang di strings.xml setiap kali daftar di server berubah.
 */
public final class CategoryLabels {

    @Nullable private static volatile OrgCategories current;

    private CategoryLabels() {
    }

    public static void update(@Nullable OrgCategories categories) {
        if (categories != null) {
            current = categories;
        }
    }

    @Nullable
    public static OrgCategories current() {
        return current;
    }

    /** Label kategori; sebelum daftar pernah dimuat, kode server ditampilkan apa adanya. */
    public static String category(@Nullable String value) {
        OrgCategories categories = current;
        if (categories != null) {
            return categories.categoryLabel(value);
        }
        return value == null ? "" : value;
    }

    public static List<String> contentTags(@Nullable List<String> values) {
        OrgCategories categories = current;
        return categories == null ? Collections.emptyList() : categories.contentTagLabels(values);
    }
}
