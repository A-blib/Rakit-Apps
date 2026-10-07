package com.aris.templateapp.upload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Memulai upload ZIP per potongan.
 *
 * @param templateId diisi untuk "Upload file perbaikan" (template berstatus check_failed); kosong untuk upload baru
 */
public record CreateUploadSessionRequest(
        @NotBlank(message = "Nama file wajib diisi") @Size(max = 255, message = "Nama file terlalu panjang") String fileName,
        @Positive(message = "Ukuran file tidak valid") long totalSize,
        UUID templateId) {
}
