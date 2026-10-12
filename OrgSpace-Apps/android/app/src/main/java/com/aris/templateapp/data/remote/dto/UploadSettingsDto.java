package com.aris.templateapp.data.remote.dto;

import java.util.List;

/** Pengaturan upload dari server: batas ukuran, ukuran potongan, host luar yang boleh dimuat WebView. */
public class UploadSettingsDto {
    public long maxZipBytes;
    public int chunkSize;
    public int draftLimit;
    public int maxPages;
    public List<String> allowedHosts;
}
