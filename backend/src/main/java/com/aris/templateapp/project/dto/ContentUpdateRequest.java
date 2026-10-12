package com.aris.templateapp.project.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Sinkron isi dari HP. {@code baseVersion} = versi isi yang terakhir diketahui HP; jika server sudah lebih baru,
 * permintaan ditolak {@code VERSION_CONFLICT} (optimistic locking, pengaman kedua di samping kunci edit).
 */
public record ContentUpdateRequest(
        @NotNull(message = "baseVersion wajib diisi") @Min(0) Integer baseVersion,
        @NotNull(message = "Isi website wajib diisi") Map<String, Object> content,
        @Min(0) int missingCount) {
}
