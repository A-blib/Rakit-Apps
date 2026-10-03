package com.aris.templateapp.auth;

import com.aris.templateapp.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Menguji alur auth email dari ujung ke ujung: request HTTP → controller → service → PostgreSQL (Testcontainers).
 * Setiap test memakai email unik, jadi data antar-test tidak saling mengganggu walau tidak di-rollback.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    private static final String PASSWORD = "rahasia123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void registerLoginMeRefreshLogout() throws Exception {
        String email = uniqueEmail();

        // 1. Daftar: email disimpan huruf kecil, akun baru belum onboarding.
        String registered = register("Aris", email.toUpperCase(), PASSWORD)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isNewUser").value(true))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.activeMode").value("creator"))
                .andExpect(jsonPath("$.user.onboardingCompleted").value(false))
                .andExpect(jsonPath("$.user.roles", empty()))
                .andReturn().getResponse().getContentAsString();

        // 2. /users/me dengan access token hasil daftar.
        me(JsonPath.read(registered, "$.accessToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Aris"));

        // 3. Masuk.
        String loggedIn = login(email, PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isNewUser").value(false))
                .andReturn().getResponse().getContentAsString();
        String refreshToken = JsonPath.read(loggedIn, "$.refreshToken");

        // 4. Refresh: dapat pasangan token baru, refresh token berbeda dari yang lama.
        String refreshed = refresh(refreshToken)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String newRefreshToken = JsonPath.read(refreshed, "$.refreshToken");
        assertThat(newRefreshToken).isNotEqualTo(refreshToken);
        me(JsonPath.read(refreshed, "$.accessToken")).andExpect(status().isOk());

        // 5. Keluar, lalu token itu tidak bisa dipakai refresh lagi.
        mockMvc.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON)
                        .content(json("refreshToken", newRefreshToken)))
                .andExpect(status().isNoContent());
        refresh(newRefreshToken)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_INVALID"));
    }

    @Test
    void reusingRotatedRefreshTokenRevokesAllSessions() throws Exception {
        String first = JsonPath.read(registerOk(uniqueEmail()), "$.refreshToken");
        String second = JsonPath.read(refresh(first).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), "$.refreshToken");

        // Token pertama sudah ditukar; memakainya lagi dianggap pencurian.
        refresh(first).andExpect(status().isUnauthorized());
        // Akibatnya token kedua (yang sah) ikut dicabut.
        refresh(second)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_INVALID"));
    }

    @Test
    void refreshTokenIsStoredOnlyAsHash() throws Exception {
        String email = uniqueEmail();
        String refreshToken = JsonPath.read(registerOk(email), "$.refreshToken");

        String storedHash = jdbc.queryForObject("""
                SELECT t.token_hash FROM refresh_tokens t JOIN users u ON u.id = t.user_id WHERE u.email = ?""",
                String.class, email);

        assertThat(storedHash).hasSize(64).isNotEqualTo(refreshToken);
    }

    @Test
    void expiredRefreshTokenIsRejected() throws Exception {
        String email = uniqueEmail();
        String refreshToken = JsonPath.read(registerOk(email), "$.refreshToken");
        jdbc.update("""
                UPDATE refresh_tokens SET expires_at = now() - interval '1 minute'
                WHERE user_id = (SELECT id FROM users WHERE email = ?)""", email);

        refresh(refreshToken).andExpect(status().isUnauthorized());
    }

    @Test
    void registerWithUsedEmailIgnoringCaseFails() throws Exception {
        String email = uniqueEmail();
        registerOk(email);

        register("Aris Lagi", email.toUpperCase(), PASSWORD)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_USED"));
    }

    @Test
    void registerValidatesEveryField() throws Exception {
        register("", "bukan-email", "pendek")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.displayName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").value("Format email tidak valid"))
                .andExpect(jsonPath("$.fieldErrors.password").value("Password harus 8–72 karakter"));
    }

    @Test
    void wrongPasswordAndUnknownEmailGiveSameError() throws Exception {
        String email = uniqueEmail();
        registerOk(email);

        login(email, "password-salah")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        login(uniqueEmail(), PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void accountWithoutPasswordIsToldToUseSocialLogin() throws Exception {
        String email = uniqueEmail();
        // Akun yang dibuat lewat Google: punya identitas google, tidak punya identitas local.
        UUID userId = jdbc.queryForObject(
                "INSERT INTO users (display_name, email) VALUES ('Aris', ?) RETURNING id", UUID.class, email);
        jdbc.update("""
                INSERT INTO user_identities (user_id, provider, provider_user_id, email, email_verified)
                VALUES (?, 'google', ?, ?, true)""", userId, "google-" + userId, email);

        login(email, PASSWORD)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USE_SOCIAL_LOGIN"))
                .andExpect(jsonPath("$.existingMethods[0]").value("google"))
                .andExpect(jsonPath("$.message").value(containsString("Google")));
    }

    @Test
    void meWithoutValidTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        me("token-asal-asalan")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private String registerOk(String email) throws Exception {
        return register("Aris", email, PASSWORD).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private ResultActions register(String displayName, String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                {"displayName":"%s","email":"%s","password":"%s"}""".formatted(displayName, email, password)));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"%s","password":"%s"}""".formatted(email, password)));
    }

    private ResultActions refresh(String refreshToken) throws Exception {
        return mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content(json("refreshToken", refreshToken)));
    }

    private ResultActions me(String accessToken) throws Exception {
        return mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
    }

    private static String json(String field, String value) {
        return "{\"%s\":\"%s\"}".formatted(field, value);
    }

    private static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@mail.com";
    }
}
