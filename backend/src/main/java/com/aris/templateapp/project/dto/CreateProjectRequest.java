package com.aris.templateapp.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * "Pakai template": website baru di ruang kerja (alur-buat-website-via-template.md 12.2).
 *
 * @param templateVersion versi paket yang sudah diunduh HP (harus sama dengan versi tayang saat ini)
 * @param name            kosong = nama template, dengan nomor bila sudah ada ("Aksara (2)")
 * @param deviceId        id instalasi app; kunci edit langsung dipegang perangkat ini
 */
public record CreateProjectRequest(
        @NotNull(message = "Template wajib dipilih") UUID sourceTemplateId,
        @NotNull(message = "Versi template wajib diisi") Integer templateVersion,
        @Size(max = 100, message = "Nama website maksimal 100 karakter") String name,
        @NotBlank(message = "deviceId wajib diisi") @Size(max = 64) String deviceId) {
}
