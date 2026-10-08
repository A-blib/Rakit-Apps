package com.aris.templateapp.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

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
        @NotBlank String deepLink,
        // Zona waktu untuk mengelompokkan statistik per hari (mis. download per tanggal di Indonesia).
        @NotBlank String timezone,
        @Valid Upload upload) {

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

    /**
     * Pengaturan fitur Upload (alur-fitur-upload.md bagian 5.7 & 5.7a). Semua batas dan daftar disimpan di sini,
     * bukan di kode, agar bisa diubah tanpa update app.
     *
     * @param storageDir folder penyimpanan ZIP di server (relatif ke folder kerja backend)
     */
    public record Upload(
            @NotBlank String storageDir,
            @Valid Limits limits,
            @Min(64 * 1024) int chunkSizeBytes,
            @Min(1) int draftLimit,
            @Min(1) int draftExpireDays,
            @Min(1) int draftWarnDays,
            @Min(1) int sessionExpireHours,
            List<String> trustedCdnHosts,
            List<AllowedLibrary> allowedLibraries,
            List<KnownLibrary> knownLibraries,
            List<String> iframeAllowed,
            List<String> trackerPatterns,
            List<String> remoteDataPatterns) {
    }

    /** Batas ukuran (bagian 5.7a) dan ambang pengecekan HTML. */
    public record Limits(
            @Min(1) long maxZipBytes,
            @Min(1) long maxExtractedBytes,
            @Min(1) int maxFiles,
            @Min(1) long maxFileBytes,
            @Min(1) long imageWarnBytes,
            @Min(1) long pageWarnBytes,
            @Min(1) int maxPages,
            @Min(1) long cssWarnBytes,
            @Min(1) long base64WarnBytes,
            // "Konten utama dibuat JS" (bagian 5.6 C): body berteks kurang dari ini DAN gambar kurang dari ini.
            @Min(0) int jsBodyMinTextChars,
            @Min(0) int jsBodyMinImages,
            // Kasus ekstrem "hampir tanpa teks/gambar yang bisa ditandai"; syarat 3 isian sebenarnya ada di langkah 4.
            @Min(1) int minEditableElements) {
    }

    /**
     * Library yang boleh dimuat dari CDN (bagian 10.4).
     *
     * @param packages nama paket di CDN, mis. {@code bootstrap} (npm/cdnjs) atau {@code @fortawesome/fontawesome-free}
     */
    public record AllowedLibrary(@NotBlank String name, List<String> packages) {
    }

    /** File library terkenal yang dikenali dari hash SHA-256-nya, sehingga tidak dipindai ulang (bagian 5.6 F). */
    public record KnownLibrary(@NotBlank String name, @NotBlank String version, @NotBlank String sha256) {
    }
}
