package com.aris.templateapp.user.dto;

import com.aris.templateapp.user.WebsitePurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Form pembuat website. Tombol "Mulai" dan "Lewati" sama-sama memanggil endpoint ini;
 * "Lewati" cukup mengirim displayName (sudah terisi dari Google/GitHub/daftar).
 *
 * @param websitePurpose opsional: sekolah, organisasi, umkm, instansi, pribadi, lainnya
 */
public record CreatorOnboardingRequest(
        @NotBlank(message = "Nama tampilan wajib diisi")
        @Size(max = 100, message = "Nama tampilan maksimal 100 karakter")
        String displayName,

        WebsitePurpose websitePurpose,

        @Size(max = 150, message = "Nama organisasi maksimal 150 karakter")
        String organizationName) {
}
