package com.aris.templateapp.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Body untuk {@code /auth/refresh} dan {@code /auth/logout}. */
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token wajib diisi")
        String refreshToken) {
}
