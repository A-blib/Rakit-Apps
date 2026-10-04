package com.aris.templateapp.template.dto;

import com.aris.templateapp.template.TemplateEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Body POST /templates/{id}/events (bagian 3.6).
 *
 * @param projectId wajib untuk download (satu project dihitung sekali)
 * @param installId ID acak per instalasi app (tamu tetap terhitung tanpa identitas)
 * @param occurredAt waktu kejadian di HP; export bisa terjadi offline lalu dikirim belakangan
 */
public record TemplateEventRequest(
        @NotNull(message = "Jenis event wajib diisi")
        TemplateEventType type,

        @Size(max = 64, message = "projectId maksimal 64 karakter")
        String projectId,

        @NotBlank(message = "installId wajib diisi")
        @Size(max = 64, message = "installId maksimal 64 karakter")
        String installId,

        @NotNull(message = "occurredAt wajib diisi")
        Instant occurredAt) {
}
