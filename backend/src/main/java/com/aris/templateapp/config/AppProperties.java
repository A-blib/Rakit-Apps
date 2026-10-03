package com.aris.templateapp.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Semua pengaturan milik app (prefix {@code app.} di application.yml) dalam satu tempat bertipe jelas,
 * sehingga class lain tidak perlu membaca string properti satu per satu.
 * <p>
 * {@code @Validated} membuat app gagal start dengan pesan jelas jika nilai wajib kosong,
 * daripada baru error saat endpoint dipanggil.
 */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @Valid Jwt jwt,
        Google google,
        GitHub github,
        @NotBlank String deepLink) {

    public record Jwt(
            // HS256 butuh kunci minimal 256 bit (32 byte).
            @NotBlank @Size(min = 32, message = "JWT_SECRET minimal 32 karakter") String secret,
            @Min(1) int accessTtlMinutes,
            @Min(1) int refreshTtlDays) {
    }

    // Boleh kosong sampai OAuth disiapkan di Fase 03.
    public record Google(String webClientId) {
    }

    public record GitHub(String clientId, String clientSecret, String redirectUri) {
    }
}
