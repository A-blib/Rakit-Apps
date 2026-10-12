package com.aris.templateapp.ui.creator;

import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.remote.dto.GalleryPageDto.GalleryTemplateDto;

import java.util.Collections;
import java.util.List;

/** Keadaan tab Template pada satu waktu. Objek baru dibuat setiap kali ada perubahan (tidak diubah). */
public final class GalleryListState {

    public final List<GalleryTemplateDto> items;
    public final boolean loadingFirstPage;
    public final boolean loadingMore;
    /** Error halaman pertama (tampilan error penuh); null jika tidak ada. */
    public final ApiError error;

    GalleryListState(List<GalleryTemplateDto> items, boolean loadingFirstPage, boolean loadingMore, ApiError error) {
        this.items = Collections.unmodifiableList(items);
        this.loadingFirstPage = loadingFirstPage;
        this.loadingMore = loadingMore;
        this.error = error;
    }
}
