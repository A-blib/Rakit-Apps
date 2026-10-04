package com.aris.templateapp.ui.creator;

import com.aris.templateapp.data.remote.dto.GalleryPageDto.GalleryTemplateDto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * "Template untuk anda" (alur-pembuatan-website.md 6.2): template terpopuler di kategori tujuan website user;
 * jika kurang dari {@link #LIMIT}, dilengkapi template terpopuler dari semua kategori tanpa duplikat.
 * Fungsi murni tanpa Android agar mudah diuji.
 */
public final class Recommendations {

    public static final int LIMIT = 6;

    private Recommendations() {
    }

    public static boolean needsFallback(List<GalleryTemplateDto> primary) {
        return primary == null || primary.size() < LIMIT;
    }

    public static List<GalleryTemplateDto> merge(List<GalleryTemplateDto> primary, List<GalleryTemplateDto> fallback) {
        List<GalleryTemplateDto> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        addUnique(primary, result, seen);
        addUnique(fallback, result, seen);
        return result;
    }

    private static void addUnique(List<GalleryTemplateDto> source, List<GalleryTemplateDto> target, Set<String> seen) {
        if (source == null) {
            return;
        }
        for (GalleryTemplateDto item : source) {
            if (target.size() >= LIMIT) {
                return;
            }
            if (seen.add(item.id)) {
                target.add(item);
            }
        }
    }
}
