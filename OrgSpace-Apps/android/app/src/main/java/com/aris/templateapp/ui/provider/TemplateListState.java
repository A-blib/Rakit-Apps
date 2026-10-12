package com.aris.templateapp.ui.provider;

import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.remote.dto.TemplateListDto.StatusCountsDto;
import com.aris.templateapp.data.remote.dto.TemplateListDto.TemplateSummaryDto;

import java.util.Collections;
import java.util.List;

/** Keadaan layar "Template Anda" pada satu waktu. Objek baru dibuat setiap kali ada perubahan (tidak diubah). */
public final class TemplateListState {

    public final List<TemplateSummaryDto> items;
    /** null sampai halaman pertama berhasil dimuat. */
    public final StatusCountsDto counts;
    /** Memuat halaman pertama (tampilkan kerangka/skeleton jika belum ada isi). */
    public final boolean loadingFirstPage;
    public final boolean loadingMore;
    public final boolean hasMore;
    /** Error halaman pertama (tampilan error penuh); null jika tidak ada. */
    public final ApiError error;

    TemplateListState(List<TemplateSummaryDto> items, StatusCountsDto counts, boolean loadingFirstPage,
                      boolean loadingMore, boolean hasMore, ApiError error) {
        this.items = Collections.unmodifiableList(items);
        this.counts = counts;
        this.loadingFirstPage = loadingFirstPage;
        this.loadingMore = loadingMore;
        this.hasMore = hasMore;
        this.error = error;
    }

    /** Jumlah "Semua" bernilai 0. Tanpa filter kategori/pencarian aktif, artinya provider belum punya template. */
    public boolean hasNoTemplates() {
        return counts != null && counts.all == 0;
    }
}
