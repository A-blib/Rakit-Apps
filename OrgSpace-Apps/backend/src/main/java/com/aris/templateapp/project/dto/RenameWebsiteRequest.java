package com.aris.templateapp.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameWebsiteRequest(
        @NotBlank(message = "Nama website wajib diisi")
        @Size(max = 100, message = "Nama website maksimal 100 karakter")
        String name) {
}
