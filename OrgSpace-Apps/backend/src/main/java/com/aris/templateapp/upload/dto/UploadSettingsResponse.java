package com.aris.templateapp.upload.dto;

import java.util.List;

/**
 * Pengaturan yang dibutuhkan app (dari {@code app.upload}), agar batas dan daftar tidak ditulis ulang di app
 * dan bisa diubah tanpa update app.
 *
 * @param allowedHosts host luar yang boleh dimuat WebView saat menampilkan template (CDN terpercaya, Google Fonts,
 *                     iframe YouTube/Maps); permintaan ke host lain diblokir
 */
public record UploadSettingsResponse(long maxZipBytes, int chunkSize, int draftLimit, int maxPages,
                                     List<String> allowedHosts) {
}
