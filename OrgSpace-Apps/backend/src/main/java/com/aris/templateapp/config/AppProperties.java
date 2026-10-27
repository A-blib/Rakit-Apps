package com.aris.templateapp.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

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
        @Valid Upload upload,
        @Valid Org org,
        @Valid Users users,
        @Valid Sites sites,
        @Valid Projects projects,
        @Valid Portal portal) {

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
     * Ruang kerja organisasi (revisi-fondasi-orgspace.md bagian 3.2 & 4.1).
     *
     * @param maxMembers    batas anggota per organisasi (F4)
     * @param inviteTtlDays masa berlaku kode undangan (F2)
     * @param logoMaxKb     ukuran maksimal file logo yang diunggah
     * @param logoDir       folder logo hasil olahan server (relatif ke folder kerja backend)
     */
    public record Org(
            @Min(2) int maxMembers,
            @Min(1) int inviteTtlDays,
            @Min(16) int logoMaxKb,
            @NotBlank String logoDir) {
    }

    /**
     * Akun user.
     *
     * @param avatarMaxKb ukuran maksimal file foto profil yang diunggah
     * @param avatarDir   folder foto profil hasil olahan server (relatif ke folder kerja backend)
     */
    public record Users(
            @Min(16) int avatarMaxKb,
            @NotBlank String avatarDir) {
    }

    /**
     * Website yang sedang diedit di cloud (alur-buat-website-via-template.md 3.1 & 12).
     *
     * @param assetsDir          folder gambar website (relatif ke folder kerja backend)
     * @param assetMaxKb         batas satu gambar setelah diproses HP (E4)
     * @param storageMaxMbPerOrg batas total gambar per ruang kerja (E5)
     * @param lockTtlSeconds     umur kunci edit; app memperpanjangnya selama editor terbuka (E1)
     */
    public record Projects(
            @NotBlank String assetsDir,
            @Min(16) int assetMaxKb,
            @Min(1) int storageMaxMbPerOrg,
            @Min(30) int lockTtlSeconds) {
    }

    /**
     * Portal web (landing, docs, monitoring, kritik &amp; saran) yang memakai API ini (docs/rancangan/portal-web.md).
     *
     * @param androidDownloadUrl  link unduh app untuk tombol di landing page; kosong = tombol disembunyikan web
     * @param feedbackDailyLimit  batas kritik &amp; saran baru per user per 24 jam (mencegah spam)
     * @param usageFlushSeconds   jeda menulis hitungan lalu lintas &amp; pengguna aktif dari memori ke database
     */
    public record Portal(
            String androidDownloadUrl,
            @Min(1) int feedbackDailyLimit,
            @Min(5) int usageFlushSeconds) {
    }

    /**
     * Website organisasi yang diterbitkan (alur-publish-website.md 7.1). Antrean bangun & domain sendiri
     * menambah properti di sini pada fase publish.
     *
     * @param domain         domain khusus website organisasi ({DOMAIN_SITUS}), boleh berisi port untuk development
     * @param urlScheme      {@code https} di VPS, {@code http} di laptop
     * @param maxPerOrg      batas website beralamat per organisasi (D1)
     * @param maxPerPersonal batas website beralamat per ruang pribadi (D1)
     */
    public record Sites(
            @NotBlank String domain,
            @NotBlank String urlScheme,
            @Min(1) int maxPerOrg,
            @Min(1) int maxPerPersonal,
            String rootDir,
            Integer buildTimeoutSeconds,
            Integer maxOutputMb,
            Integer maxFiles,
            Integer keepReleases,
            String customTarget,
            String publicIp,
            String verifyTxtPrefix,
            String hostingUrl,
            // true hanya di profile dev: verifikasi domain kustom boleh dilewati untuk uji di laptop.
            boolean allowDnsBypass) {

        public Sites {
            if (rootDir == null || rootDir.isBlank()) {
                rootDir = "./data/sites";
            }
            if (buildTimeoutSeconds == null || buildTimeoutSeconds <= 0) {
                buildTimeoutSeconds = 120;
            }
            if (maxOutputMb == null || maxOutputMb <= 0) {
                maxOutputMb = 50;
            }
            if (maxFiles == null || maxFiles <= 0) {
                maxFiles = 500;
            }
            if (keepReleases == null || keepReleases <= 0) {
                keepReleases = 3;
            }
            if (customTarget == null || customTarget.isBlank()) {
                customTarget = "custom." + (domain != null && !domain.contains("localhost") ? domain : "orgspace.site");
            }
            if (publicIp == null || publicIp.isBlank()) {
                publicIp = "103.175.216.42";
            }
            if (verifyTxtPrefix == null || verifyTxtPrefix.isBlank()) {
                verifyTxtPrefix = "_orgspace-verify";
            }
            if (hostingUrl == null || hostingUrl.isBlank()) {
                hostingUrl = "http://localhost:3001";
            }
            hostingUrl = hostingUrl.replaceAll("/+$", "");
        }

        public String storageRootDir() {
            return rootDir != null && !rootDir.isBlank() ? rootDir : "./data/sites";
        }

        public int timeoutSeconds() {
            return buildTimeoutSeconds != null && buildTimeoutSeconds > 0 ? buildTimeoutSeconds : 120;
        }

        public int outputMaxMb() {
            return maxOutputMb != null && maxOutputMb > 0 ? maxOutputMb : 50;
        }

        public int filesMax() {
            return maxFiles != null && maxFiles > 0 ? maxFiles : 500;
        }

        public int releasesToKeep() {
            return keepReleases != null && keepReleases > 0 ? keepReleases : 3;
        }

        /**
         * Alamat publik website di OrgSpace-Hosting berbentuk path, mis. {@code https://orgspace.app/osis-smansa}
         * (keputusan Aris 19 Oktober 2026: path, bukan subdomain).
         */
        public String urlOf(String slug) {
            return hostingUrl + "/" + slug;
        }

        /** Preview template di OrgSpace-Hosting; "demo" dicadangkan sebagai slug, jadi tidak bentrok dengan website user. */
        public String previewUrlOf(UUID templateId) {
            return hostingUrl + "/demo/" + templateId + "/";
        }
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
