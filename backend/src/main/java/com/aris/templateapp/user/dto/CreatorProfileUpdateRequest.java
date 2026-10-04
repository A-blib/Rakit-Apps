package com.aris.templateapp.user.dto;

import com.aris.templateapp.user.WebsitePurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Edit info pembuat website dari tab Profil (alur-pembuatan-website.md bagian 5.4). Aturannya sama dengan form
 * onboarding; nilai yang dikirim menggantikan seluruh isi lama (websitePurpose/organizationName null = dikosongkan).
 *
 * @param websitePurpose opsional: sekolah, organisasi, umkm, instansi, pribadi, lainnya
 */
public record CreatorProfileUpdateRequest(
        @NotBlank(message = "Nama tampilan wajib diisi")
        @Size(max = 100, message = "Nama tampilan maksimal 100 karakter")
        String displayName,

        WebsitePurpose websitePurpose,

        @Size(max = 150, message = "Nama organisasi maksimal 150 karakter")
        String organizationName) {
}
