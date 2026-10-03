package com.aris.templateapp.security;

import com.aris.templateapp.config.AppProperties;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Membuat dan memeriksa access token JWT (HS256). Isinya hanya {@code sub} = ID user dan waktu berlaku,
 * karena data lain (nama, mode) bisa berubah dan selalu diambil dari database.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final Duration accessTtl;
    private final Clock clock;
    private final JwtParser parser;

    public JwtService(AppProperties properties, Clock clock) {
        this.key = Keys.hmacShaKeyFor(properties.jwt().secret().getBytes(StandardCharsets.UTF_8));
        this.accessTtl = Duration.ofMinutes(properties.jwt().accessTtlMinutes());
        this.clock = clock;
        this.parser = Jwts.parser()
                .verifyWith(key)
                // Parser memakai jam yang sama dengan pembuat token, agar test jam palsu konsisten.
                .clock(() -> Date.from(clock.instant()))
                .build();
    }

    public String createAccessToken(UUID userId) {
        Instant now = clock.instant();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTtl)))
                // Algoritma ditulis eksplisit; tanpa ini JJWT memilih HS512 jika kunci cukup panjang.
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /** Umur access token dalam detik, untuk field {@code expiresIn} di AuthResponse. */
    public long accessTtlSeconds() {
        return accessTtl.toSeconds();
    }

    /** ID user jika token sah dan belum kedaluwarsa; kosong jika tanda tangan salah, kedaluwarsa, atau rusak. */
    public Optional<UUID> parseUserId(String token) {
        try {
            String subject = parser.parseSignedClaims(token).getPayload().getSubject();
            return Optional.of(UUID.fromString(subject));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
