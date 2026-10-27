package com.aris.templateapp.portal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Membuat/mengubah info terbaru (admin). Semua field dikirim ulang; gambar diunggah lewat endpoint terpisah. */
public record PortalUpdateRequest(
        @NotBlank @Pattern(regexp = "fitur|optimasi|desain|lainnya", message = "Kategori tidak dikenal") String category,
        @NotBlank(message = "Judul wajib diisi") @Size(max = 120) String title,
        @NotBlank(message = "Ringkasan wajib diisi") @Size(max = 300) String summary,
        @NotBlank(message = "Isi lengkap wajib diisi") @Size(max = 20000, message = "Isi lengkap maksimal 20.000 karakter")
        String content,
        @Size(max = 200) String imageAlt,
        boolean published,
        // Lama tampil di portal sejak pertama tayang; null = tanpa batas.
        @Min(value = 1, message = "Lama tampil minimal 1 hari") @Max(value = 3650, message = "Lama tampil maksimal 3650 hari")
        Integer displayDays) {
}
