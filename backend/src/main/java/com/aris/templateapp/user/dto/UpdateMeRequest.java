package com.aris.templateapp.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Ubah data akun pribadi dari tab Profil. Data organisasi diubah lewat Profil organisasi. */
public record UpdateMeRequest(
        @NotBlank(message = "Nama tampilan wajib diisi")
        @Size(max = 100, message = "Nama tampilan maksimal 100 karakter")
        String displayName) {
}
