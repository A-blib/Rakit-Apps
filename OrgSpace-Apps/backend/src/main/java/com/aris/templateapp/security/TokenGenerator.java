package com.aris.templateapp.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Membuat token acak (refresh token, nanti juga tiket GitHub/link) dan hash SHA-256-nya.
 * Database hanya menyimpan hash, jadi kebocoran database tidak langsung membocorkan token yang masih berlaku.
 */
@Component
public class TokenGenerator {

    private static final int TOKEN_BYTES = 32;

    // SecureRandom memakai sumber acak kriptografis OS (/dev/urandom), bukan Random biasa yang bisa ditebak.
    private final SecureRandom random = new SecureRandom();

    /** 32 byte acak dalam Base64 URL-safe (tanpa '+', '/', '='), aman dipakai di JSON maupun URL. */
    public String newToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Hash SHA-256 dalam 64 karakter hex, sesuai kolom {@code token_hash}. */
    public String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 wajib tersedia di setiap JDK, jadi ini tidak akan terjadi.
            throw new IllegalStateException(e);
        }
    }
}
