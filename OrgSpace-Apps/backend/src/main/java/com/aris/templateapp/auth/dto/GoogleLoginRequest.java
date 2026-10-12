package com.aris.templateapp.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * @param idToken   token dari Credential Manager di Android
 * @param linkToken opsional, sama seperti di {@link LoginRequest}
 */
public record GoogleLoginRequest(
        @NotBlank(message = "idToken wajib diisi")
        String idToken,

        String linkToken) {
}
