package com.aris.templateapp.gallery;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.gallery.dto.GalleryPageResponse;
import com.aris.templateapp.gallery.dto.GalleryTemplateResponse;
import com.aris.templateapp.user.WebsitePurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Galeri Template publik (alur-pembuatan-website.md bagian 6.2). Boleh dipanggil tamu. */
@Service
@RequiredArgsConstructor
public class GalleryService {

    static final int DEFAULT_SIZE = 20;
    /** Batas atas agar satu request tidak bisa meminta seluruh isi tabel. */
    static final int MAX_SIZE = 50;

    private final GalleryQueries queries;

    @Transactional(readOnly = true)
    public GalleryPageResponse list(String category, String query, String sort, int page, Integer size) {
        WebsitePurpose categoryFilter;
        GalleryQueries.SortOrder sortOrder;
        try {
            categoryFilter = category == null || category.isBlank() || category.equals("all") ? null
                    : PersistableEnum.fromValue(WebsitePurpose.class, category);
            sortOrder = GalleryQueries.SortOrder.from(sort == null ? "popular" : sort);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, e.getMessage());
        }
        int pageSize = size == null ? DEFAULT_SIZE : size;
        if (page < 0 || pageSize < 1 || pageSize > MAX_SIZE) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR,
                    "Halaman tidak boleh negatif dan size harus 1–" + MAX_SIZE + ".");
        }
        String search = query == null || query.isBlank() ? null : query.trim();

        List<GalleryTemplateResponse> items = queries.list(categoryFilter, search, sortOrder, page, pageSize);
        long total = queries.count(categoryFilter, search);
        int totalPages = (int) ((total + pageSize - 1) / pageSize);
        return new GalleryPageResponse(items, page, pageSize, total, totalPages);
    }
}
