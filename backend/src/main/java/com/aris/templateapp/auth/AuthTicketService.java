package com.aris.templateapp.auth;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.security.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Membuat dan memakai tiket sekali pakai (state GitHub, hasil login GitHub, penyambungan akun). */
@Service
@RequiredArgsConstructor
public class AuthTicketService {

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
}
