package com.aris.templateapp.auth;

/**
 * Data akun Google/GitHub yang sudah diverifikasi, dalam bentuk yang sama untuk kedua provider.
 * AccountLinkingService hanya bekerja dengan record ini, jadi tidak peduli dari mana datanya.
 *
 * @param providerUserId ID tetap di sisi provider (Google: {@code sub}, GitHub: ID angka). Email bisa berubah, ID ini tidak.
 * @param email          email dari provider (boleh null, mis. email GitHub privat)
 * @param emailVerified  true hanya jika provider menjamin email itu milik user; syarat untuk penyambungan akun
 */
public record SocialProfile(
        IdentityProvider provider,
        String providerUserId,
        String email,
        boolean emailVerified,
        String displayName,
        String avatarUrl) {

    /** Email yang boleh dipakai untuk mencari akun lain: hanya jika terverifikasi. */
    public String verifiedEmail() {
        return emailVerified ? email : null;
    }
}
