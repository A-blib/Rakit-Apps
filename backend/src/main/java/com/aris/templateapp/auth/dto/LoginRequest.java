package com.aris.templateapp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email wajib diisi")
        String email,

        @NotBlank(message = "Password wajib diisi")
        @Size(max = 72, message = "Password maksimal 72 karakter")
        String password) {
}
