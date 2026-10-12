package com.aris.templateapp.user.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Form penyedia template. Setelah dikirim, provider langsung aktif. */
public record ProviderOnboardingRequest(
        @NotBlank(message = "Nama kreator wajib diisi")
        @Size(max = 100, message = "Nama kreator maksimal 100 karakter")
        String creatorName,

        @Size(max = 300, message = "Bio maksimal 300 karakter")
        String bio,

        @Size(max = 500, message = "Link maksimal 500 karakter")
        @Pattern(regexp = "^https?://[^\\s/$.?#][^\\s]*$", message = "Link harus URL yang valid (diawali http:// atau https://)")
        String portfolioUrl,

        // Elemen list juga divalidasi: setiap keahlian tidak boleh kosong dan maksimal 50 karakter.
        @Size(max = 10, message = "Maksimal 10 keahlian")
        List<@NotBlank(message = "Keahlian tidak boleh kosong") @Size(max = 50, message = "Keahlian maksimal 50 karakter") String> specialties,

        @AssertTrue(message = "Kamu harus menyetujui aturan provider")
        boolean agreedToTerms) {
}
