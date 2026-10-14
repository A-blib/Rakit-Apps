package com.aris.templateapp.template.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Buat atau ganti nama folder. */
public record FolderRequest(
        @NotBlank(message = "Nama folder wajib diisi")
        @Size(max = 60, message = "Nama folder maksimal 60 karakter")
        String name) {
}
