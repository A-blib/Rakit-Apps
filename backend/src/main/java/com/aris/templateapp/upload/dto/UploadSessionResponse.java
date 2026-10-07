package com.aris.templateapp.upload.dto;

import java.util.UUID;

/**
 * Posisi upload saat ini. Jika koneksi putus, app memanggil GET sesi lalu melanjutkan dari {@code receivedSize}.
 *
 * @param chunkSize ukuran potongan yang harus dipakai app (byte)
 */
public record UploadSessionResponse(UUID id, String fileName, long totalSize, long receivedSize, int chunkSize) {
}
