package com.aris.templateapp.security;

import com.aris.templateapp.config.AppProperties;
import io.jsonwebtoken.Claims;
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

    /** Klaim token Portal Pengurus: token hanya berlaku untuk satu website, bukan API OrgSpace lain. */
    private static final String SITE_CLAIM = "site";
    // Portal pengurus tidak punya refresh token; 8 jam cukup untuk satu sesi kerja.
    private static final Duration SITE_TTL = Duration.ofHours(8);

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

    /**
     * Token Portal Pengurus untuk satu website. Portal berjalan di domain hosting bersama halaman template buatan
     * provider, jadi tokennya dibatasi: tidak bisa dipakai untuk API OrgSpace lain (lihat {@link #parseUserId}).
     */
    public String createSiteToken(UUID userId, UUID siteId) {
        Instant now = clock.instant();
        return Jwts.builder()
                .subject(userId.toString())
                .claim(SITE_CLAIM, siteId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(SITE_TTL)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /** ID user jika token app sah dan belum kedaluwarsa; kosong jika rusak, kedaluwarsa, atau token portal website. */
    public Optional<UUID> parseUserId(String token) {
        return claims(token).filter(c -> c.get(SITE_CLAIM) == null).flatMap(JwtService::subject);
    }

    /** ID user jika token portal sah untuk website {@code siteId}. */
    public Optional<UUID> parseSiteUserId(String token, UUID siteId) {
        return claims(token).filter(c -> siteId.toString().equals(c.get(SITE_CLAIM))).flatMap(JwtService::subject);
    }

    private Optional<Claims> claims(String token) {
        try {
            return Optional.of(parser.parseSignedClaims(token).getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static Optional<UUID> subject(Claims claims) {
        try {
            return Optional.of(UUID.fromString(claims.getSubject()));
        } catch (IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }
}
