package com.aris.templateapp.auth;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.security.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/** Membuat dan memakai tiket sekali pakai (state GitHub, hasil login GitHub, penyambungan akun). */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthTicketService {

    /** Kunci payload tiket GITHUB_STATE/LOGIN_RESULT/LINK_RESULT: hash verifier milik app yang memulai alur. */
    public static final String KEY_CODE_CHALLENGE = "codeChallenge";

    private final AuthTicketRepository repository;
    private final TokenGenerator tokenGenerator;
    private final Clock clock;

    /**
     * Membuat tiket dan mengembalikan nilai aslinya (yang disimpan hanya hash).
     * <p>
     * {@code REQUIRES_NEW}: tiket langsung tersimpan dalam transaksi tersendiri. Ini penting untuk tiket LINK,
     * karena tiket itu dibuat tepat sebelum service melempar {@code ACCOUNT_LINK_REQUIRED}; tanpa transaksi
     * tersendiri, error itu akan me-rollback tiketnya sehingga linkToken yang dikirim ke app tidak berlaku.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String issue(AuthTicketType type, UUID userId, Map<String, Object> payload) {
        String rawToken = tokenGenerator.newToken();
        Instant now = clock.instant();
        repository.save(new AuthTicket(type, tokenGenerator.hash(rawToken), userId, payload, now.plus(type.ttl())));
        return rawToken;
    }

    /**
     * Memakai tiket: harus ada, jenisnya cocok, belum dipakai, dan belum kedaluwarsa. Setelah itu ditandai terpakai.
     *
     * @param errorIfInvalid kode error jika tiket tidak sah (mis. {@code LINK_TOKEN_INVALID} untuk tiket LINK)
     */
    @Transactional
    public AuthTicket consume(AuthTicketType type, String rawToken, ErrorCode errorIfInvalid) {
        Instant now = clock.instant();
        AuthTicket ticket = rawToken == null ? null
                : repository.findByTokenHash(tokenGenerator.hash(rawToken)).orElse(null);
        if (ticket == null || ticket.getType() != type || !ticket.isUsable(now)) {
            throw new ApiException(errorIfInvalid);
        }
        ticket.markUsed(now);
        return ticket;
    }

    /**
     * Sama dengan {@link #consume}, tetapi coretannya disimpan di transaksi sendiri: tetap tercatat "terpakai" walau
     * proses sesudahnya gagal dan transaksinya dibatalkan (mis. login Google yang berakhir ACCOUNT_LINK_REQUIRED).
     * Tanpa ini, nonce Google yang gagal di tengah jalan bisa dipakai lagi.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuthTicket consumeNow(AuthTicketType type, String rawToken, ErrorCode errorIfInvalid) {
        return consume(type, rawToken, errorIfInvalid);
    }

    /** Tiket kedaluwarsa tidak berguna lagi; tanpa pembersihan, tabel terus bertambah (nonce Google bisa diminta siapa saja). */
    @Scheduled(cron = "0 45 3 * * *", zone = "${app.timezone}")
    @Transactional
    public void deleteExpired() {
        int deleted = repository.deleteExpiredBefore(clock.instant().minus(Duration.ofDays(1)));
        log.info("Pembersihan tiket auth: {} tiket kedaluwarsa dihapus", deleted);
    }

    /**
     * Memastikan tiket ditukar oleh app yang memulai alur GitHub (pola PKCE): hash {@code codeVerifier} harus sama
     * dengan {@code codeChallenge} yang disimpan saat URL login dibuat. Deep link {@code templateapp://} bisa ditangkap
     * app lain di HP, dan URL login/callback bisa dikirim ke orang lain; keduanya tidak punya verifier.
     */
    public void requireVerifier(AuthTicket ticket, String codeVerifier) {
        String expected = ticket.payloadString(KEY_CODE_CHALLENGE);
        if (expected == null || codeVerifier == null || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.US_ASCII), challengeOf(codeVerifier).getBytes(StandardCharsets.US_ASCII))) {
            throw new ApiException(ErrorCode.TICKET_INVALID);
        }
    }

    /** PKCE S256: BASE64URL(SHA-256(verifier)) tanpa padding. */
    public static String challengeOf(String codeVerifier) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
