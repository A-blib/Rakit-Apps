package com.aris.templateapp.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Langkah terakhir onboarding pembuat website. App memanggil endpoint organisasi/undangan lebih dulu
 * (buat organisasi, gabung lewat kode, atau ruang pribadi), baru endpoint ini.
 */
public record CreatorOnboardingRequest(
        @NotBlank(message = "Nama tampilan wajib diisi")
        @Size(max = 100, message = "Nama tampilan maksimal 100 karakter")
        String displayName) {
}
