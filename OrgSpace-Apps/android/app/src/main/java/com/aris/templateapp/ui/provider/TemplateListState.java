package com.aris.templateapp.ui.provider;

import androidx.annotation.Nullable;

import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.remote.dto.TemplateFolderDto;
import com.aris.templateapp.data.remote.dto.TemplateListDto.StatusCountsDto;
import com.aris.templateapp.data.remote.dto.TemplateListDto.TemplateSummaryDto;

import java.util.Collections;
import java.util.List;

/** Keadaan layar "Template Anda" pada satu waktu. Objek baru dibuat setiap kali ada perubahan (tidak diubah). */
public final class TemplateListState {

    public final List<TemplateSummaryDto> items;
    /** Folder yang tampil di atas daftar; kosong saat folder disembunyikan (di dalam folder / filter aktif). */
    public final List<TemplateFolderDto> folders;
    /** Folder yang sedang dibuka; null = halaman utama. */
    @Nullable
    public final TemplateFolderDto currentFolder;
    /** null sampai halaman pertama berhasil dimuat. */
    public final StatusCountsDto counts;
    /** Memuat halaman pertama (tampilkan kerangka/skeleton jika belum ada isi). */
    public final boolean loadingFirstPage;
    public final boolean loadingMore;
    public final boolean hasMore;
    /** Error halaman pertama (tampilan error penuh); null jika tidak ada. */
    public final ApiError error;

    TemplateListState(List<TemplateSummaryDto> items, List<TemplateFolderDto> folders,
                      @Nullable TemplateFolderDto currentFolder, StatusCountsDto counts, boolean loadingFirstPage,
                      boolean loadingMore, boolean hasMore, ApiError error) {
        this.items = Collections.unmodifiableList(items);
        this.folders = Collections.unmodifiableList(folders);
        this.currentFolder = currentFolder;
        this.counts = counts;
        this.loadingFirstPage = loadingFirstPage;
        this.loadingMore = loadingMore;
        this.hasMore = hasMore;
        this.error = error;
    }

    /**
     * Jumlah "Semua" bernilai 0 di halaman utama. Tanpa filter kategori/pencarian aktif, artinya provider belum
     * punya template sama sekali (di halaman utama jumlah ini mencakup isi semua folder).
     */
    public boolean hasNoTemplates() {
        return currentFolder == null && counts != null && counts.all == 0;
    }

    public boolean isEmpty() {
        return items.isEmpty() && folders.isEmpty();
    }
}
