package com.aris.templateapp.docs.dto;

import java.util.List;

/** Menu pusat dokumentasi: kategori berurutan, masing-masing berisi artikel tayang. Kategori kosong tidak dikirim. */
public record DocTreeResponse(List<Category> categories) {

    public record Category(String value, String label, List<DocSummaryResponse> articles) {
    }
}
