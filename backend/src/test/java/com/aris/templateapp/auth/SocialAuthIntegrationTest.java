package com.aris.templateapp.auth;

import com.aris.templateapp.TestcontainersConfiguration;
import com.aris.templateapp.security.GitHubOAuthClient;
import com.aris.templateapp.security.GoogleTokenVerifier;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Alur Google, GitHub, dan penyambungan akun dari ujung ke ujung (skenario 5–8 bagian 13.2).
 * <p>
 * Google & GitHub tidak dihubungi sungguhan:
 * - {@code @MockitoBean} mengganti GoogleTokenVerifier dengan mock: kita tentukan idToken mana menghasilkan profil apa.
 * - {@code @MockitoSpyBean} membungkus GitHubOAuthClient asli: authorizeUrl tetap asli, hanya fetchProfile yang dipalsukan.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SocialAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GoogleTokenVerifier googleVerifier;

    @MockitoSpyBean
    private GitHubOAuthClient gitHubClient;

    @Test
    void googleCreatesAccountOnFirstLoginAndReusesItAfterwards() throws Exception {
        String email = uniqueEmail();
        googleReturns("tok-1", googleProfile("g-" + UUID.randomUUID(), email));

        String first = google("tok-1", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isNewUser").value(true))
                .andExpect(jsonPath("$.user.displayName").value("Aris Google"))
                .andExpect(jsonPath("$.user.email").value(email))
                .andReturn().getResponse().getContentAsString();

        google("tok-1", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isNewUser").value(false))
                .andExpect(jsonPath("$.user.id").value((String) JsonPath.read(first, "$.user.id")));
    }

    @Test
    void googleAccountCannotUseEmailLogin() throws Exception {
        String email = uniqueEmail();
        googleReturns("tok-1", googleProfile("g-" + UUID.randomUUID(), email));
        google("tok-1", null).andExpect(status().isOk());

        // Skenario 6.
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"rahasia123\"}".formatted(email)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USE_SOCIAL_LOGIN"))
                .andExpect(jsonPath("$.existingMethods[0]").value("google"));
    }

    @Test
    void googleThenGitHubWithSameEmailLinksAfterProvingOwnership() throws Exception {
        // Skenario 7: akun dibuat lewat Google.
        String email = uniqueEmail();
        String gitHubId = String.valueOf(System.nanoTime());
        googleReturns("tok-google", googleProfile("g-" + UUID.randomUUID(), email));
        String userId = JsonPath.read(google("tok-google", null).andReturn().getResponse().getContentAsString(),
                "$.user.id");

        // Masuk lewat GitHub dengan email terverifikasi yang sama → diminta masuk Google dulu.
        MultiValueMap<String, String> redirect = gitHubLogin("code-1", gitHubProfile(gitHubId, email, true), null);
        assertThat(redirect.getFirst("error")).isEqualTo("ACCOUNT_LINK_REQUIRED");
        assertThat(redirect.getFirst("methods")).isEqualTo("google");
        String linkToken = redirect.getFirst("linkToken");
        assertThat(linkToken).isNotBlank();

        // User masuk dengan Google sambil membawa linkToken → GitHub tersambung.
        String afterLink = google("tok-google", linkToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(userId))
                .andReturn().getResponse().getContentAsString();
        identities(JsonPath.read(afterLink, "$.accessToken"))
                .andExpect(jsonPath("$[*].provider", containsInAnyOrder("google", "github")));

        // Masuk GitHub berikutnya langsung ke akun yang sama.
        MultiValueMap<String, String> next = gitHubLogin("code-2", gitHubProfile(gitHubId, email, true), null);
        exchange(next.getFirst("ticket"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isNewUser").value(false))
                .andExpect(jsonPath("$.user.id").value(userId));
    }

    @Test
    void emailAccountThenGoogleLinksAfterPasswordLogin() throws Exception {
        String email = uniqueEmail();
        register(email);
        googleReturns("tok-x", googleProfile("g-" + UUID.randomUUID(), email));

        String error = google("tok-x", null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ACCOUNT_LINK_REQUIRED"))
                .andExpect(jsonPath("$.existingMethods[0]").value("local"))
                .andReturn().getResponse().getContentAsString();
        String linkToken = JsonPath.read(error, "$.linkToken");

        login(email, linkToken).andExpect(status().isOk());
        // Setelah tersambung, login Google langsung berhasil.
        google("tok-x", null).andExpect(status().isOk()).andExpect(jsonPath("$.isNewUser").value(false));
    }

    @Test
    void linkTokenUsedByAnotherAccountIsRejected() throws Exception {
        String ownerEmail = uniqueEmail();
        register(ownerEmail);
        googleReturns("tok-x", googleProfile("g-" + UUID.randomUUID(), ownerEmail));
        String linkToken = JsonPath.read(google("tok-x", null).andReturn().getResponse().getContentAsString(),
                "$.linkToken");

        String otherEmail = uniqueEmail();
        register(otherEmail);
        login(otherEmail, linkToken)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("LINK_USER_MISMATCH"));
    }

    @Test
    void unknownLinkTokenIsRejected() throws Exception {
        String email = uniqueEmail();
        register(email);

        login(email, "token-ngawur")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LINK_TOKEN_INVALID"));
    }

    @Test
    void gitHubWithPrivateEmailCreatesSeparateAccountThatCanBeLinkedManually() throws Exception {
        // Skenario 8: akun utama dibuat dengan email.
        String email = uniqueEmail();
        String accessToken = JsonPath.read(register(email), "$.accessToken");
        String gitHubId = String.valueOf(System.nanoTime());

        // GitHub dengan email lain/privat → akun baru terpisah, tidak diminta penyambungan.
        MultiValueMap<String, String> redirect = gitHubLogin("code-1", gitHubProfile(gitHubId, null, false), null);
        exchange(redirect.getFirst("ticket")).andExpect(jsonPath("$.isNewUser").value(true));

        // Dari Pengaturan akun utama: sambungkan GitHub → ditolak karena GitHub itu sudah punya akun sendiri.
        assertThat(gitHubLinkFromSettings(accessToken, "code-2", gitHubProfile(gitHubId, null, false))
                .getFirst("error")).isEqualTo("IDENTITY_IN_USE");

        // Akun GitHub lain yang belum dipakai bisa disambungkan.
        String freshGitHubId = String.valueOf(System.nanoTime());
        assertThat(gitHubLinkFromSettings(accessToken, "code-3", gitHubProfile(freshGitHubId, null, false))
                .getFirst("result")).isEqualTo("linked");
        identities(accessToken).andExpect(jsonPath("$[*].provider", containsInAnyOrder("local", "github")));
    }

    @Test
    void gitHubCallbackRejectsUnknownStateAndCancelledLogin() throws Exception {
        assertThat(callback("code-1", "state-palsu", null).getFirst("error")).isEqualTo("TICKET_INVALID");

        String state = stateOf(authorizeUrl(null));
        assertThat(callback(null, state, "access_denied").getFirst("error")).isEqualTo("SOCIAL_AUTH_FAILED");
    }

    @Test
    void loginTicketCanOnlyBeExchangedOnce() throws Exception {
        MultiValueMap<String, String> redirect = gitHubLogin("code-1",
                gitHubProfile(String.valueOf(System.nanoTime()), null, false), null);
        String ticket = redirect.getFirst("ticket");

        exchange(ticket).andExpect(status().isOk());
        exchange(ticket)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TICKET_INVALID"));
    }

    @Test
    void lastIdentityCannotBeRemoved() throws Exception {
        String email = uniqueEmail();
        String accessToken = JsonPath.read(register(email), "$.accessToken");

        mockMvc.perform(delete("/api/users/me/identities/local").header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LAST_IDENTITY"));

        // Setelah Google tersambung, identitas local boleh dilepas.
        googleReturns("tok-link", googleProfile("g-" + UUID.randomUUID(), uniqueEmail()));
        mockMvc.perform(post("/api/users/me/identities/google").header(HttpHeaders.AUTHORIZATION, bearer(accessToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"idToken\":\"tok-link\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].provider", containsInAnyOrder("local", "google")));
        mockMvc.perform(delete("/api/users/me/identities/local").header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isNoContent());
        identities(accessToken).andExpect(jsonPath("$[*].provider", containsInAnyOrder("google")));
    }

    // ---- helper ----

    private void googleReturns(String idToken, SocialProfile profile) {
        when(googleVerifier.verify(idToken)).thenReturn(profile);
    }

    private static SocialProfile googleProfile(String sub, String email) {
        return new SocialProfile(IdentityProvider.GOOGLE, sub, email, true, "Aris Google", null);
    }

    private static SocialProfile gitHubProfile(String id, String email, boolean verified) {
        return new SocialProfile(IdentityProvider.GITHUB, id, email, verified, "Aris GitHub", null);
    }

    private ResultActions google(String idToken, String linkToken) throws Exception {
        String body = linkToken == null ? "{\"idToken\":\"%s\"}".formatted(idToken)
                : "{\"idToken\":\"%s\",\"linkToken\":\"%s\"}".formatted(idToken, linkToken);
        return mockMvc.perform(post("/api/auth/google").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    /** Menjalankan langkah 1–4 alur GitHub dan mengembalikan query parameter deep link hasilnya. */
    private MultiValueMap<String, String> gitHubLogin(String code, SocialProfile profile, String linkToken)
            throws Exception {
        doReturn(profile).when(gitHubClient).fetchProfile(code);
        return callback(code, stateOf(authorizeUrl(linkToken)), null);
    }

    private MultiValueMap<String, String> gitHubLinkFromSettings(String accessToken, String code, SocialProfile profile)
            throws Exception {
        doReturn(profile).when(gitHubClient).fetchProfile(code);
        String url = JsonPath.read(mockMvc.perform(post("/api/users/me/identities/github/authorize-url")
                        .header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), "$.url");
        return callback(code, stateOf(url), null);
    }

    private String authorizeUrl(String linkToken) throws Exception {
        String body = linkToken == null ? "{}" : "{\"linkToken\":\"%s\"}".formatted(linkToken);
        return JsonPath.read(mockMvc.perform(post("/api/auth/github/authorize-url")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), "$.url");
    }

    private MultiValueMap<String, String> callback(String code, String state, String error) throws Exception {
        var request = get("/api/auth/github/callback");
        if (code != null) request.param("code", code);
        if (state != null) request.param("state", state);
        if (error != null) request.param("error", error);
        String location = mockMvc.perform(request)
                .andExpect(status().isFound())
                .andReturn().getResponse().getHeader(HttpHeaders.LOCATION);
        assertThat(location).startsWith("templateapp://auth/callback");
        return UriComponentsBuilder.fromUriString(location).build(true).getQueryParams();
    }

    private static String stateOf(String authorizeUrl) {
        return UriComponentsBuilder.fromUriString(authorizeUrl).build().getQueryParams().getFirst("state");
    }

    private ResultActions exchange(String ticket) throws Exception {
        return mockMvc.perform(post("/api/auth/github/exchange").contentType(MediaType.APPLICATION_JSON)
                .content("{\"ticket\":\"%s\"}".formatted(ticket)));
    }

    private String register(String email) throws Exception {
        return mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Aris\",\"email\":\"%s\",\"password\":\"rahasia123\"}".formatted(email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    private ResultActions login(String email, String linkToken) throws Exception {
        return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"password\":\"rahasia123\",\"linkToken\":\"%s\"}".formatted(email, linkToken)));
    }

    private ResultActions identities(String accessToken) throws Exception {
        return mockMvc.perform(get("/api/users/me/identities").header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk());
    }

    private static String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@mail.com";
    }
}
