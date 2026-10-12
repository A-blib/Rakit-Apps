package com.aris.templateapp.auth;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.security.TokenGenerator;
import com.aris.templateapp.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Siklus hidup refresh token: dibuat saat masuk, dirotasi saat dipakai, dicabut saat keluar.
 * <p>
 * Rotasi: setiap refresh token hanya boleh dipakai SEKALI. Jika token yang sudah ditukar dipakai lagi,
 * kemungkinan token itu dicuri (pemilik asli dan pencuri sama-sama memegangnya), jadi semua sesi
 * user tersebut dicabut dan user harus masuk ulang.
 */
@Slf4j
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final TokenGenerator tokenGenerator;
    private final Clock clock;
    private final Duration ttl;

    public RefreshTokenService(RefreshTokenRepository repository, TokenGenerator tokenGenerator, Clock clock,
                               AppProperties properties) {
        this.repository = repository;
        this.tokenGenerator = tokenGenerator;
        this.clock = clock;
        this.ttl = Duration.ofDays(properties.jwt().refreshTtlDays());
    }

    /** Membuat refresh token baru. Mengembalikan token asli; yang disimpan hanya hash-nya. */
    @Transactional
    public String issue(User user, String deviceName) {
        return save(user, deviceName).rawToken();
    }

    /**
     * Menukar refresh token lama dengan yang baru.
     * <p>
     * {@code noRollbackFor}: saat pemakaian ulang terdeteksi, pencabutan semua token harus TETAP tersimpan
     * walaupun method ini lalu melempar error. Tanpa ini, transaksi di-rollback dan pencabutannya batal.
     */
    @Transactional(noRollbackFor = ApiException.class)
    public Rotation rotate(String rawToken, String deviceName) {
        Instant now = clock.instant();
        RefreshToken current = repository.findByTokenHash(tokenGenerator.hash(rawToken))
                .orElseThrow(RefreshTokenService::invalid);

        if (current.getReplacedBy() != null) {
            log.warn("Refresh token yang sudah dirotasi dipakai lagi; semua sesi user {} dicabut", current.getUser().getId());
            repository.revokeAllForUser(current.getUser().getId(), now);
            throw invalid();
        }
        if (current.isRevoked() || current.isExpired(now)) {
            throw invalid();
        }

        Issued next = save(current.getUser(), deviceName);
        current.rotateTo(next.entity().getId(), now);
        return new Rotation(current.getUser(), next.rawToken());
    }

    /** Mencabut satu refresh token (keluar). Token yang tidak dikenal diabaikan agar keluar selalu berhasil. */
    @Transactional
    public void revoke(String rawToken) {
        repository.findByTokenHash(tokenGenerator.hash(rawToken))
                .ifPresent(token -> token.revoke(clock.instant()));
    }

    private Issued save(User user, String deviceName) {
        String rawToken = tokenGenerator.newToken();
        RefreshToken entity = repository.save(new RefreshToken(
                user, tokenGenerator.hash(rawToken), deviceName, clock.instant().plus(ttl)));
        return new Issued(entity, rawToken);
    }

    private static ApiException invalid() {
        return new ApiException(ErrorCode.REFRESH_TOKEN_INVALID);
    }

    private record Issued(RefreshToken entity, String rawToken) {
    }

    /** Hasil rotasi: pemilik token dan refresh token baru (asli, belum di-hash). */
    public record Rotation(User user, String refreshToken) {
    }
}
