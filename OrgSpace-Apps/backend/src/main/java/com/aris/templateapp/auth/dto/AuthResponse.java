package com.aris.templateapp.auth.dto;

import com.aris.templateapp.user.dto.UserResponse;

/**
 * Hasil masuk/daftar/refresh.
 *
 * @param expiresIn umur access token dalam detik; app memakainya untuk tahu kapan token habis
 * @param isNewUser true jika akun baru dibuat di request ini (app lalu membuka onboarding)
 */
public record AuthResponse(String accessToken, String refreshToken, long expiresIn, boolean isNewUser,
                           UserResponse user) {
}
