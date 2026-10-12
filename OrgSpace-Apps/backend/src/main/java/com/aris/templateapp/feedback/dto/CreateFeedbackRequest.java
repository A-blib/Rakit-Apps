package com.aris.templateapp.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Kritik &amp; saran baru dari app atau web.
 *
 * @param senderRole     creator / provider; kosong = mode aktif akun saat ini
 * @param organizationId ruang kerja yang dibahas (opsional; harus anggota)
 * @param rating         1–5 (opsional)
 */
public record CreateFeedbackRequest(
        @NotBlank(message = "Jenis wajib dipilih")
        @Pattern(regexp = "bug|saran|pertanyaan|keluhan|lainnya", message = "Jenis tidak dikenal") String category,
        @NotBlank(message = "Judul wajib diisi") @Size(max = 150, message = "Judul maksimal 150 karakter") String subject,
        @NotBlank(message = "Pesan wajib diisi") @Size(max = 4000, message = "Pesan maksimal 4000 karakter") String message,
        @Min(value = 1, message = "Nilai 1–5") @Max(value = 5, message = "Nilai 1–5") Integer rating,
        @Pattern(regexp = "creator|provider", message = "Peran pengirim tidak dikenal") String senderRole,
        UUID organizationId,
        @Size(max = 30) String appVersion,
        @Size(max = 150) String deviceInfo) {
}
