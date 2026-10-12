package com.aris.templateapp.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

/**
 * Isi lokal yang tidak bisa disinkron (kunci hilang / konflik) disimpan sebagai cadangan agar tidak ada kerja hilang.
 *
 * @param reason lock_lost / conflict
 */
public record BackupRequest(
        @NotNull(message = "Isi cadangan wajib diisi") Map<String, Object> content,
        @NotBlank @Pattern(regexp = "lock_lost|conflict", message = "Alasan cadangan tidak dikenal") String reason) {
}
