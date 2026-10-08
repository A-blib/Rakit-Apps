package com.aris.templateapp.security;

import com.aris.templateapp.config.AppProperties;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit test murni: tanpa Spring dan tanpa database, jadi berjalan sangat cepat. */
class JwtServiceTest {

    private static final String SECRET = "rahasia-test-yang-panjangnya-minimal-32-byte";
    private static final Instant NOW = Instant.parse("2026-10-04T08:00:00Z");

    private final UUID userId = UUID.randomUUID();

    @Test
    void validTokenReturnsUserId() {
        JwtService jwt = service(SECRET, NOW);

        String token = jwt.createAccessToken(userId);

        assertThat(jwt.parseUserId(token)).contains(userId);
        assertThat(jwt.accessTtlSeconds()).isEqualTo(15 * 60);
    }

    @Test
    void expiredTokenIsRejected() {
        String token = service(SECRET, NOW).createAccessToken(userId);

        // Jam dimajukan 16 menit, melewati umur token 15 menit.
        JwtService later = service(SECRET, NOW.plus(Duration.ofMinutes(16)));

        assertThat(later.parseUserId(token)).isEmpty();
    }

    @Test
    void tokenSignedWithOtherSecretIsRejected() {
        String token = service("secret-lain-yang-juga-panjangnya-minimal-32-byte", NOW).createAccessToken(userId);

        assertThat(service(SECRET, NOW).parseUserId(token)).isEmpty();
    }

    @Test
    void tamperedOrGarbageTokenIsRejected() {
        JwtService jwt = service(SECRET, NOW);
        String token = jwt.createAccessToken(userId);
        // Mengubah satu karakter di bagian tanda tangan (paling belakang).
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");

        assertThat(jwt.parseUserId(tampered)).isEmpty();
        assertThat(jwt.parseUserId("bukan.token.jwt")).isEmpty();
    }

    @Test
    void tokenUsesHs256() {
        String token = service(SECRET, NOW).createAccessToken(userId);
        String header = new String(java.util.Base64.getUrlDecoder().decode(token.split("\\.")[0]));

        assertThat(header).contains("\"alg\":\"HS256\"");
    }

    private static JwtService service(String secret, Instant now) {
        AppProperties properties = new AppProperties(
                new AppProperties.Jwt(secret, 15, 30),
                new AppProperties.Google(null),
                new AppProperties.GitHub(null, null, null),
                "templateapp://auth/callback",
                "Asia/Jakarta", null);
        return new JwtService(properties, Clock.fixed(now, ZoneOffset.UTC));
    }
}
