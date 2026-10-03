package com.aris.templateapp.security;

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
 */
@Slf4j
@Component
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;
    private final boolean configured;

    public GoogleTokenVerifier(AppProperties properties) {
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

        GoogleIdToken.Payload payload = token.getPayload();
        return new SocialProfile(
                IdentityProvider.GOOGLE,
                payload.getSubject(),
                payload.getEmail(),
                Boolean.TRUE.equals(payload.getEmailVerified()),
                (String) payload.get("name"),
                (String) payload.get("picture"));
    }
}
