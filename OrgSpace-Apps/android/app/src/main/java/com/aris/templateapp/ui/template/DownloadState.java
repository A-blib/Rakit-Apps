package com.aris.templateapp.ui.template;

import androidx.annotation.Nullable;

import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.remote.dto.GalleryTemplateDetailDto;

/** Keadaan layar Unduh paket (alur-buat-website-via-template.md bagian 5). Tidak diubah setelah dibuat. */
final class DownloadState {

    enum Phase {
        /** Memeriksa paket di HP / memuat detail dari server. */
        LOADING,
        /** Detail gagal dimuat (offline, template hilang). */
        DETAIL_ERROR,
        /** Menunggu jawaban dialog data seluler. */
        ASK_MOBILE,
        DOWNLOADING,
        EXTRACTING,
        /** Unduhan/ekstrak gagal; detail tetap tampil dengan tombol Coba lagi. */
        FAILED
    }

    final Phase phase;
    @Nullable
    final GalleryTemplateDetailDto detail;
    final long downloaded;
    final long total;
    @Nullable
    final ApiError error;

    private DownloadState(Phase phase, @Nullable GalleryTemplateDetailDto detail, long downloaded, long total,
                          @Nullable ApiError error) {
        this.phase = phase;
        this.detail = detail;
        this.downloaded = downloaded;
        this.total = total;
        this.error = error;
    }

    static DownloadState loading() {
        return new DownloadState(Phase.LOADING, null, 0, 0, null);
    }

    static DownloadState detailError(ApiError error) {
        return new DownloadState(Phase.DETAIL_ERROR, null, 0, 0, error);
    }

    static DownloadState askMobile(GalleryTemplateDetailDto detail) {
        return new DownloadState(Phase.ASK_MOBILE, detail, 0, 0, null);
    }

    static DownloadState downloading(GalleryTemplateDetailDto detail, long downloaded, long total) {
        return new DownloadState(Phase.DOWNLOADING, detail, downloaded, total, null);
    }

    static DownloadState extracting(GalleryTemplateDetailDto detail) {
        return new DownloadState(Phase.EXTRACTING, detail, 0, 0, null);
    }

    static DownloadState failed(GalleryTemplateDetailDto detail, ApiError error) {
        return new DownloadState(Phase.FAILED, detail, 0, 0, error);
    }
}
