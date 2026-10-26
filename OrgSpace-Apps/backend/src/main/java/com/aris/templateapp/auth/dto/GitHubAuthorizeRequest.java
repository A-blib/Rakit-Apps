package com.aris.templateapp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Body {@code /auth/github/authorize-url} dan {@code /users/me/identities/github/authorize-url}.
 *
 * @param linkToken     opsional, sama seperti di {@link LoginRequest} (tidak dipakai saat menyambungkan dari Pengaturan)
 * @param codeChallenge BASE64URL(SHA-256(codeVerifier)) tanpa padding (PKCE S256). Verifier-nya tetap di HP dan baru
 *                      dikirim saat menukar tiket, sehingga app lain yang mencegat deep link tidak bisa memakainya.
 */
public record GitHubAuthorizeRequest(
        String linkToken,
        @NotBlank(message = "codeChallenge wajib diisi")
        @Pattern(regexp = "^[A-Za-z0-9_-]{43}$", message = "Format codeChallenge tidak valid")
        String codeChallenge) {
}
