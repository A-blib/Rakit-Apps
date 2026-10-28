package com.aris.templateapp.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Username + password Portal untuk satu peran. Password selalu diisi ulang; yang lama tidak bisa dilihat. */
public record PortalCredentialRequest(
        @NotBlank(message = "Username wajib diisi")
        @Pattern(regexp = "^[A-Za-z0-9._-]{3,40}$",
                message = "Username 3–40 karakter: huruf, angka, titik, garis bawah, atau tanda minus")
        String username,
        // BCrypt hanya membaca 72 byte pertama, jadi password yang lebih panjang ditolak daripada diam-diam dipotong.
        @NotBlank(message = "Password wajib diisi")
        @Size(min = 8, max = 72, message = "Password 8–72 karakter")
        String password) {
}
