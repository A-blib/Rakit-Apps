package com.aris.templateapp.common.response;

import java.util.List;

/**
 * Satu halaman daftar untuk portal web (tabel dengan nomor halaman). {@code page} mulai dari 0.
 * Daftar di app Android memakai bentuk {@code items + hasMore} (gulir tanpa nomor halaman).
 */
public record PageResponse<T>(List<T> items, int page, int size, long totalItems, int totalPages) {

    public static <T> PageResponse<T> of(List<T> items, int page, int size, long totalItems) {
        return new PageResponse<>(items, page, size, totalItems, (int) ((totalItems + size - 1) / size));
    }
}
