package com.aris.templateapp.security;

import com.aris.templateapp.auth.IdentityProvider;
import com.aris.templateapp.auth.SocialProfile;
import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Menguji GitHubOAuthClient dengan server GitHub palsu (MockRestServiceServer): tidak ada request
 * sungguhan ke internet; balasan JSON-nya ditulis manual meniru dokumentasi GitHub.
 */
class GitHubOAuthClientTest {

    private MockRestServiceServer github;
    private GitHubOAuthClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        github = MockRestServiceServer.bindTo(builder).build();
        AppProperties properties = new AppProperties(
                new AppProperties.Jwt("x".repeat(32), 15, 30),
                new AppProperties.Google(null),
                new AppProperties.GitHub("client-id", "client-secret", "http://localhost:8080/api/auth/github/callback"),
                "templateapp://auth/callback",
                "Asia/Jakarta", null);
        client = new GitHubOAuthClient(properties, builder);
    }

    @Test
    void authorizeUrlContainsClientIdScopeAndState() {
        String url = client.authorizeUrl("state-abc");

        assertThat(url).startsWith("https://github.com/login/oauth/authorize?")
                .contains("client_id=client-id")
                .contains("state=state-abc")
                .contains("scope=read:user%20user:email");
    }

    @Test
    void fetchProfileUsesPrimaryVerifiedEmail() {
        expectToken();
        expectUser("""
                {"id": 42, "login": "aris", "name": "Aris M", "avatar_url": "https://avatar"}""");
        expectEmails("""
                [{"email": "lain@mail.com", "primary": false, "verified": true},
                 {"email": "aris@mail.com", "primary": true, "verified": true}]""");

        SocialProfile profile = client.fetchProfile("code-1");

        assertThat(profile).isEqualTo(new SocialProfile(
                IdentityProvider.GITHUB, "42", "aris@mail.com", true, "Aris M", "https://avatar"));
        github.verify();
    }

    @Test
    void unverifiedPrimaryEmailIsMarkedUnverifiedAndLoginUsedAsName() {
        expectToken();
        expectUser("""
                {"id": 42, "login": "aris", "name": null, "avatar_url": null}""");
        expectEmails("""
                [{"email": "aris@mail.com", "primary": true, "verified": false}]""");

        SocialProfile profile = client.fetchProfile("code-1");

        assertThat(profile.emailVerified()).isFalse();
        assertThat(profile.verifiedEmail()).isNull();
        assertThat(profile.displayName()).isEqualTo("aris");
    }

    @Test
    void rejectedCodeFails() {
        github.expect(requestTo(GitHubOAuthClient.TOKEN_URL))
                .andRespond(withSuccess("""
                        {"error": "bad_verification_code"}""", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.fetchProfile("code-salah"))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.SOCIAL_AUTH_FAILED));
    }

    @Test
    void gitHubDownFails() {
        github.expect(requestTo(GitHubOAuthClient.TOKEN_URL)).andRespond(withServerError());

        assertThatThrownBy(() -> client.fetchProfile("code-1"))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.SOCIAL_AUTH_FAILED));
    }

    private void expectToken() {
        github.expect(requestTo(GitHubOAuthClient.TOKEN_URL))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"access_token": "gho_token", "token_type": "bearer"}""", MediaType.APPLICATION_JSON));
    }

    private void expectUser(String json) {
        github.expect(requestTo(GitHubOAuthClient.API_URL + "/user"))
                .andExpect(header("Authorization", "Bearer gho_token"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
    }

    private void expectEmails(String json) {
        github.expect(requestTo(GitHubOAuthClient.API_URL + "/user/emails"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
    }
}
