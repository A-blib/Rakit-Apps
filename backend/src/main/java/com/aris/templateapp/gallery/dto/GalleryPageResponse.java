package com.aris.templateapp.gallery.dto;

import java.util.List;

/** Satu halaman galeri Template. */
public record GalleryPageResponse(
        List<GalleryTemplateResponse> items,
        int page,
        int size,
        long totalItems,
        int totalPages) {
}
