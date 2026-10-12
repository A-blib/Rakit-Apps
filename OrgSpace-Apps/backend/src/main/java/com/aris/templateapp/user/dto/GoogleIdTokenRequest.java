package com.aris.templateapp.user.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleIdTokenRequest(
        @NotBlank(message = "idToken wajib diisi")
        String idToken) {
}
