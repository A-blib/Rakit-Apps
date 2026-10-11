package com.aris.templateapp.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Edit nama tampilan dari tab Profil. Data organisasi diubah lewat Profil organisasi. */
public record CreatorProfileUpdateRequest(
        @NotBlank(message = "Nama tampilan wajib diisi")
        @Size(max = 100, message = "Nama tampilan maksimal 100 karakter")
        String displayName) {
}
