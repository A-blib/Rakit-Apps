package com.aris.templateapp.project.dto;

import java.util.UUID;

/** Gambar yang sudah tersimpan di server; isi website merujuknya sebagai {@code asset:{assetId}}. */
public record AssetResponse(UUID assetId, int width, int height, long sizeBytes, String url) {
}
