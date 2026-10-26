package com.aris.templateapp.security;

import com.aris.templateapp.auth.AuthTicketService;
import com.aris.templateapp.auth.AuthTicketType;
import com.aris.templateapp.auth.IdentityProvider;
import com.aris.templateapp.auth.SocialProfile;
import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.config.AppProperties;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * Memeriksa idToken dari Credential Manager di Android.
 * <p>
 * Yang dicek library Google: tanda tangan memakai kunci publik Google (diunduh & di-cache otomatis),
 * penerbit {@code accounts.google.com}, belum kedaluwarsa, dan audience = Web Client ID kita.
 * Cek audience penting: tanpa itu, idToken yang dibuat untuk app LAIN juga akan diterima.
 * <p>
 * Nonce: ID token berlaku sekitar 1 jam, jadi tanpa nonce siapa pun yang mendapat salinannya bisa masuk sebagai pemiliknya
 * berulang kali. App meminta nonce ke {@code POST /api/auth/google/nonce}, Credential Manager menanamnya ke dalam token
 * (ikut ditandatangani Google), lalu di sini nonce itu wajib ada di daftar server dan langsung dicoret. Setiap ID token
 * hanya bisa dipakai sekali. Berlaku untuk login Google dan menyambungkan Google dari Pengaturan.
 */
@Slf4j
@Component
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;
    private final boolean configured;
    private final AuthTicketService ticketService;

    public GoogleTokenVerifier(AppProperties properties, AuthTicketService ticketService) {
        this.ticketService = ticketService;
        String webClientId = properties.google().webClientId();
        this.configured = webClientId != null && !webClientId.isBlank();
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(configured ? List.of(webClientId) : List.of())
                .build();
    }

    /** @throws ApiException {@code SOCIAL_AUTH_FAILED} jika token tidak sah */
    public SocialProfile verify(String idToken) {
        if (!configured) {
            log.warn("GOOGLE_WEB_CLIENT_ID belum diisi di .env; login Google tidak bisa diverifikasi");
            throw new ApiException(ErrorCode.SOCIAL_AUTH_FAILED);
        }
        GoogleIdToken token;
        try {
            token = verifier.verify(idToken);
        } catch (GeneralSecurityException | IOException | IllegalArgumentException e) {
            log.warn("Verifikasi idToken Google gagal: {}", e.getMessage());
            throw new ApiException(ErrorCode.SOCIAL_AUTH_FAILED);
        }
        if (token == null) {
            throw new ApiException(ErrorCode.SOCIAL_AUTH_FAILED);
        }
        return toProfile(token.getPayload());
    }

    /** Isi token yang tanda tangan, penerbit, audience, dan masa berlakunya sudah dicek library Google. */
    SocialProfile toProfile(GoogleIdToken.Payload payload) {
        // Dicoret di transaksi sendiri: tetap terpakai walau login sesudahnya gagal (mis. ACCOUNT_LINK_REQUIRED).
        ticketService.consumeNow(AuthTicketType.GOOGLE_NONCE, payload.getNonce(), ErrorCode.SOCIAL_AUTH_FAILED);
        return new SocialProfile(
                IdentityProvider.GOOGLE,
                payload.getSubject(),
                payload.getEmail(),
                Boolean.TRUE.equals(payload.getEmailVerified()),
                (String) payload.get("name"),
                (String) payload.get("picture"));
    }
}
