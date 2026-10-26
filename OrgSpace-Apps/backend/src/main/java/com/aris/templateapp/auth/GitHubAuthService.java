package com.aris.templateapp.auth;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.security.GitHubOAuthClient;
import com.aris.templateapp.user.User;
import com.aris.templateapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Alur login GitHub (bagian 6.4):
 * <pre>
 * 1. App   → POST /auth/github/authorize-url       ← { url } berisi state acak
 * 2. App membuka url di Custom Tab, user login di GitHub
 * 3. GitHub → GET /auth/github/callback?code&state (ke backend, bukan ke app)
 * 4. Backend menukar code, menjalankan logika 6.3, lalu redirect ke templateapp://auth/callback?ticket=...
 * 5. App   → POST /auth/github/exchange { ticket }  ← AuthResponse
 * </pre>
 * Kenapa tidak langsung mengirim token di deep link? URL deep link bisa terbaca app lain atau tercatat di log.
 * Tiket berumur 2 menit, sekali pakai, dan baru menjadi token lewat request HTTPS biasa.
 * <p>
 * Pola PKCE: app membuat {@code codeVerifier} acak yang tidak pernah keluar dari HP, dan hanya mengirim hash-nya
 * ({@code codeChallenge}) saat meminta URL login. Tiket hanya bisa ditukar dengan verifier itu. Ini menutup tiga celah:
 * app lain yang mendaftarkan skema {@code templateapp://} menangkap tiket (akun diambil alih); URL callback hasil
 * login GitHub penyerang dikirim ke korban (korban masuk ke akun penyerang); dan URL "sambungkan GitHub" milik
 * penyerang dibuka korban (GitHub korban tersambung ke akun penyerang). Penyambungan dari Pengaturan karena itu tidak
 * terjadi di callback, tetapi saat app yang login menukar tiket {@code LINK_RESULT} ({@link #completeLinkFromSettings}).
 * <p>
 * {@link #handleCallback} sengaja TIDAK {@code @Transactional}: setiap langkah berjalan di transaksinya sendiri,
 * sehingga error di satu langkah bisa ditangkap lalu diubah menjadi redirect {@code ?error=KODE}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubAuthService {

    static final String KEY_IS_NEW_USER = "isNewUser";
    private static final String KEY_PURPOSE = "purpose";
    private static final String KEY_LINK_TOKEN = "linkToken";
    private static final String PURPOSE_LOGIN = "login";
    private static final String PURPOSE_LINK = "link";

    private final GitHubOAuthClient gitHubClient;
    private final AuthTicketService ticketService;
    private final AccountLinkingService accountLinkingService;
    private final UserRepository userRepository;
    private final AppProperties properties;

    /** URL login GitHub untuk masuk/daftar. linkToken (opsional) dibawa lewat state sampai callback. */
    public String authorizeUrlForLogin(String linkToken, String codeChallenge) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(KEY_PURPOSE, PURPOSE_LOGIN);
        payload.put(AuthTicketService.KEY_CODE_CHALLENGE, codeChallenge);
        if (linkToken != null && !linkToken.isBlank()) {
            payload.put(KEY_LINK_TOKEN, linkToken);
        }
        return gitHubClient.authorizeUrl(ticketService.issue(AuthTicketType.GITHUB_STATE, null, payload));
    }

    /** URL login GitHub untuk menyambungkan GitHub ke user yang sudah login (dari Pengaturan). */
    public String authorizeUrlForLinking(UUID userId, String codeChallenge) {
        Map<String, Object> payload = Map.of(KEY_PURPOSE, PURPOSE_LINK, AuthTicketService.KEY_CODE_CHALLENGE, codeChallenge);
        return gitHubClient.authorizeUrl(ticketService.issue(AuthTicketType.GITHUB_STATE, userId, payload));
    }

    /** Menangani callback dari GitHub dan selalu mengembalikan alamat deep link (berhasil maupun gagal). */
    public URI handleCallback(String code, String state, String gitHubError) {
        try {
            AuthTicket stateTicket = ticketService.consume(AuthTicketType.GITHUB_STATE, state, ErrorCode.TICKET_INVALID);
            // GitHub mengirim ?error=access_denied jika user menekan "Cancel".
            if (gitHubError != null || code == null) {
                return redirect(Map.of("error", ErrorCode.SOCIAL_AUTH_FAILED.name()));
            }
            SocialProfile profile = gitHubClient.fetchProfile(code);
            String codeChallenge = stateTicket.payloadString(AuthTicketService.KEY_CODE_CHALLENGE);

            if (PURPOSE_LINK.equals(stateTicket.payloadString(KEY_PURPOSE))) {
                // Belum disambungkan di sini: browser yang membuka callback belum tentu milik user yang memulai.
                Map<String, Object> pending = AccountLinkingService.identityPayload(profile);
                pending.put(AuthTicketService.KEY_CODE_CHALLENGE, codeChallenge);
                return redirect(Map.of("linkTicket",
                        ticketService.issue(AuthTicketType.LINK_RESULT, stateTicket.getUserId(), pending)));
            }

            AccountLinkingService.SignInResult result = accountLinkingService.signIn(profile);
            String linkToken = stateTicket.payloadString(KEY_LINK_TOKEN);
            if (linkToken != null) {
                accountLinkingService.completePendingLink(result.user(), linkToken);
            }
            String loginTicket = ticketService.issue(AuthTicketType.LOGIN_RESULT, result.user().getId(),
                    Map.of(KEY_IS_NEW_USER, result.isNewUser(), AuthTicketService.KEY_CODE_CHALLENGE, codeChallenge));
            return redirect(Map.of("ticket", loginTicket));
        } catch (ApiException e) {
            return redirectForError(e);
        }
    }

    /**
     * Langkah akhir "sambungkan GitHub" dari Pengaturan: hanya app yang memulai alur (punya verifier) dan sedang login
     * sebagai user yang sama yang bisa menyambungkan identitas GitHub di tiket.
     */
    @Transactional
    public void completeLinkFromSettings(UUID userId, String rawTicket, String codeVerifier) {
        AuthTicket ticket = ticketService.consume(AuthTicketType.LINK_RESULT, rawTicket, ErrorCode.TICKET_INVALID);
        ticketService.requireVerifier(ticket, codeVerifier);
        if (!userId.equals(ticket.getUserId())) {
            throw new ApiException(ErrorCode.LINK_USER_MISMATCH);
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
        accountLinkingService.linkToUser(user, AccountLinkingService.profileFrom(ticket));
    }

    private URI redirectForError(ApiException e) {
        Map<String, String> params = new HashMap<>();
        params.put("error", e.getErrorCode().name());
        if (e.getErrorCode() == ErrorCode.ACCOUNT_LINK_REQUIRED) {
            params.put("linkToken", e.getLinkToken());
            params.put("methods", String.join(",", e.getExistingMethods()));
        }
        return redirect(params);
    }

    private URI redirect(Map<String, ?> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(properties.deepLink());
        params.forEach(builder::queryParam);
        return builder.encode().build().toUri();
    }
}
