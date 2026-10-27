package com.aris.templateapp.portal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Mengubah teks hero landing page (admin). Semua field dikirim ulang; gambar diunggah lewat endpoint terpisah. */
public record LandingHeroRequest(
        @NotBlank(message = "Judul wajib diisi") @Size(max = 120) String headline,
        @NotBlank(message = "Judul berwarna wajib diisi") @Size(max = 80) String headlineHighlight,
        @NotBlank(message = "Subjudul wajib diisi") @Size(max = 300) String subheadline,
        @NotBlank(message = "Teks tombol utama wajib diisi") @Size(max = 60) String primaryCtaLabel,
        @NotBlank(message = "Teks tombol kedua wajib diisi") @Size(max = 60) String secondaryCtaLabel,
        // Hanya path di situs sendiri, anchor, atau http(s): menolak "javascript:" yang bisa menjalankan script.
        @NotBlank(message = "Link tombol kedua wajib diisi") @Size(max = 300)
        @Pattern(regexp = "^(/|#|https?://)\\S*$", message = "Link harus diawali /, #, http://, atau https://")
        String secondaryCtaUrl,
        @NotNull @Size(max = 3, message = "Maksimal 3 poin")
        List<@NotBlank @Size(max = 40, message = "Poin maksimal 40 karakter") @Pattern(regexp = "[^\\n\\r]*") String> highlights,
        @NotBlank(message = "Deskripsi gambar wajib diisi") @Size(max = 200) String imageAlt) {
}
