package com.aris.templateapp.security;

import com.aris.templateapp.TestcontainersConfiguration;
import com.aris.templateapp.auth.AuthTicketService;
import com.aris.templateapp.auth.SocialProfile;
import com.aris.templateapp.common.exception.ApiException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Nonce Google seperti absen: ID token hanya diterima jika nonce di dalamnya ada di daftar server, lalu langsung
 * dicoret. Tanda tangan Google sendiri diuji library Google; di sini yang diuji isi token setelah lolos verifikasi.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GoogleNonceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GoogleTokenVerifier verifier;

    @Autowired
    private AuthTicketService ticketService;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TransactionTemplate tx;

    @Test
    void anIdTokenCanOnlyBeUsedOnce() throws Exception {
        GoogleIdToken.Payload rina = payload(nonce());

        SocialProfile profile = verifier.toProfile(rina);
        assertThat(profile.email()).isEqualTo("rina@gmail.com");
        // Salinan token yang sama (mis. bocor) ditolak: nonce-nya sudah dicoret.
        assertThatThrownBy(() -> verifier.toProfile(rina))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void tokensWithoutOrWithUnknownNonceAreRejected() {
        assertThatThrownBy(() -> verifier.toProfile(payload(null))).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> verifier.toProfile(payload("nonce-karangan-penyerang"))).isInstanceOf(ApiException.class);
    }

    @Test
    void nonceStaysUsedEvenIfTheLoginAfterwardsFails() throws Exception {
        GoogleIdToken.Payload rina = payload(nonce());
        // Login yang gagal di tengah jalan (mis. ACCOUNT_LINK_REQUIRED) membatalkan transaksinya.
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
            verifier.toProfile(rina);
            throw new IllegalStateException("login gagal sesudah verifikasi");
        })).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> verifier.toProfile(rina)).isInstanceOf(ApiException.class);
    }

    @Test
    void expiredTicketsAreCleanedUp() {
        String expired = "kedaluwarsa-" + UUID.randomUUID();
        String fresh = "masih-berlaku-" + UUID.randomUUID();
        jdbc.update("INSERT INTO auth_tickets (type, token_hash, payload, expires_at) VALUES ('GOOGLE_NONCE', ?, '{}', now() - interval '2 days')", expired);
        jdbc.update("INSERT INTO auth_tickets (type, token_hash, payload, expires_at) VALUES ('GOOGLE_NONCE', ?, '{}', now() + interval '5 minutes')", fresh);

        ticketService.deleteExpired();

        assertThat(jdbc.queryForObject("SELECT count(*) FROM auth_tickets WHERE token_hash IN (?, ?)", Long.class, expired, fresh))
                .isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM auth_tickets WHERE token_hash = ?", Long.class, fresh)).isEqualTo(1);
    }

    private String nonce() throws Exception {
        return JsonPath.read(mockMvc.perform(post("/api/auth/google/nonce"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), "$.nonce");
    }

    private static GoogleIdToken.Payload payload(String nonce) {
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setSubject("g-" + UUID.randomUUID());
        payload.setEmail("rina@gmail.com");
        payload.setEmailVerified(true);
        payload.setNonce(nonce);
        return payload;
    }
}
