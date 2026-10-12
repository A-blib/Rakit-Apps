package com.aris.templateapp.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Nama wajib diisi")
        @Size(max = 100, message = "Nama maksimal 100 karakter")
        String displayName,

        @NotBlank(message = "Email wajib diisi")
        @Email(message = "Format email tidak valid")
        @Size(max = 255, message = "Email maksimal 255 karakter")
        String email,

        // BCrypt hanya membaca 72 byte pertama, jadi password lebih panjang ditolak sejak awal.
        @NotBlank(message = "Password wajib diisi")
        @Size(min = 8, max = 72, message = "Password harus 8–72 karakter")
        String password) {
}
