package com.aris.templateapp.organization;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.common.util.Emails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Membatasi percobaan beruntun agar sesuatu tidak bisa ditebak dengan mencoba terus:
 * <ul>
 *   <li>kode undangan: 10 kali per menit per user;</li>
 *   <li>login (app dan Portal Pengurus): per email (melindungi satu akun dari tebakan password) dan per IP (satu
 *       penyerang yang mencoba banyak email). Batas IP lebih longgar karena satu sekolah bisa berbagi satu IP.</li>
 *   <li>formulir publik website: per IP per website, agar bot tidak membanjiri kotak masuk dengan kiriman palsu.
 *       Bawaan 30/menit: cukup untuk satu kelas di lab komputer yang mendaftar bersamaan.</li>
 * </ul>
 * Disimpan di memori: cukup untuk satu server; hilang saat restart, dan itu tidak masalah untuk batas per menit.
 * shortcut: hitungan per proses; jika backend dijalankan lebih dari satu salinan, pindahkan ke database/Redis.
 */
@Component
public class AttemptLimiter {

    static final int MAX_ATTEMPTS = 10;
    static final Duration WINDOW = Duration.ofMinutes(1);
    /** Kunci yang tidak dipakai selama ini dibuang dari memori (lebih lama dari jendela mana pun). */
    static final Duration FORGET_AFTER = Duration.ofMinutes(10);

    private final Clock clock;
    private final int loginPerEmail;
    private final int loginPerIp;
    private final int formPerIp;
    private final Map<Object, Deque<Instant>> attempts = new ConcurrentHashMap<>();

    public AttemptLimiter(Clock clock,
                          @Value("${app.auth.login-max-per-email:10}") int loginPerEmail,
                          @Value("${app.auth.login-max-per-ip:30}") int loginPerIp,
                          @Value("${app.forms.max-per-ip-per-minute:30}") int formPerIp) {
        this.clock = clock;
        this.loginPerEmail = loginPerEmail;
        this.loginPerIp = loginPerIp;
        this.formPerIp = formPerIp;
    }

    /** Percobaan kode undangan oleh satu user. */
    public void check(UUID userId) {
        check(userId, MAX_ATTEMPTS, WINDOW);
    }

    /** Percobaan login, dihitung sebelum password diperiksa (berhasil atau gagal sama-sama dihitung). */
    public void checkLogin(String ip, String email) {
        check("login-ip:" + ip, loginPerIp, WINDOW);
        String normalized = Emails.normalize(email);
        if (normalized != null) {
            check("login-email:" + normalized, loginPerEmail, WINDOW);
        }
    }

    /** Permintaan nonce Google dari satu IP (satu nonce per percobaan login Google, jadi batasnya sama dengan login). */
    public void checkNonceRequest(String ip) {
        check("nonce-ip:" + ip, loginPerIp, WINDOW);
    }

    /** Kiriman formulir publik ke satu website dari satu IP. */
    public void checkFormSubmission(String ip, String slug) {
        // Pesan ini tampil apa adanya di bawah formulir website (site-runtime.ts / DynamicForm di Hosting), jadi
        // ditulis untuk pengunjung: satu WiFi sekolah bisa berbagi IP, dan cukup menunggu sebentar.
        check("form:" + slug.toLowerCase(Locale.ROOT) + ":" + ip, formPerIp, WINDOW,
                "Terlalu banyak kiriman dari jaringan ini. Tunggu sekitar 1 menit, lalu kirim lagi.");
    }

    void check(Object key, int max, Duration window) {
        check(key, max, window, ErrorCode.TOO_MANY_ATTEMPTS.defaultMessage());
    }

    private void check(Object key, int max, Duration window, String message) {
        Instant now = clock.instant();
        Deque<Instant> recent = attempts.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (recent) {
            while (!recent.isEmpty() && recent.peekFirst().isBefore(now.minus(window))) {
                recent.pollFirst();
            }
            if (recent.size() >= max) {
                throw new ApiException(ErrorCode.TOO_MANY_ATTEMPTS, message);
            }
            recent.addLast(now);
        }
    }

    /** Tanpa ini, setiap IP/email yang pernah mencoba login tersimpan selamanya di memori. */
    @Scheduled(fixedDelay = 5, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    void forgetIdleKeys() {
        Instant cutoff = clock.instant().minus(FORGET_AFTER);
        attempts.entrySet().removeIf(entry -> {
            Deque<Instant> recent = entry.getValue();
            synchronized (recent) {
                return recent.isEmpty() || recent.peekLast().isBefore(cutoff);
            }
        });
    }

    int trackedKeys() {
        return attempts.size();
    }
}
