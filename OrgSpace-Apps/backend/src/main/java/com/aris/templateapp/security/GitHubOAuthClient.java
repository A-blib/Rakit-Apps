package com.aris.templateapp.security;

import com.aris.templateapp.auth.IdentityProvider;
import com.aris.templateapp.auth.SocialProfile;
import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.config.AppProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

/**
 * Berbicara dengan GitHub: membuat URL login, menukar {@code code} menjadi access token GitHub,
 * lalu mengambil profil dan email. Client secret hanya dipakai di sini, tidak pernah dikirim ke app.
 */
@Slf4j
@Component
public class GitHubOAuthClient {

    static final String AUTHORIZE_URL = "https://github.com/login/oauth/authorize";
    static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
    static final String API_URL = "https://api.github.com";
    private static final String SCOPE = "read:user user:email";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final AppProperties.GitHub config;
    private final RestClient restClient;

    // Ada dua konstruktor, jadi Spring perlu diberi tahu mana yang dipakai untuk membuat bean.
    @Autowired
    public GitHubOAuthClient(AppProperties properties) {
        this(properties, RestClient.builder().requestFactory(requestFactoryWithTimeout()));
    }

    // Dipakai test untuk memasang server palsu (MockRestServiceServer) ke builder.
    GitHubOAuthClient(AppProperties properties, RestClient.Builder builder) {
        this.config = properties.github();
        this.restClient = builder.build();
    }

    /** URL halaman login GitHub. {@code state} dicocokkan lagi saat callback untuk memastikan alurnya dimulai dari app kita. */
    public String authorizeUrl(String state) {
        if (config.clientId() == null || config.clientId().isBlank()) {
            throw new IllegalStateException("GITHUB_CLIENT_ID belum diisi di .env");
        }
        return UriComponentsBuilder.fromUriString(AUTHORIZE_URL)
                .queryParam("client_id", config.clientId())
                .queryParam("redirect_uri", config.redirectUri())
                .queryParam("scope", SCOPE)
                .queryParam("state", state)
                .encode()
                .toUriString();
    }

    /**
     * Menukar {@code code} dari callback dengan profil GitHub. Email diambil dari {@code /user/emails}
     * karena email di {@code /user} bisa kosong (privat) dan tidak menyebut status verifikasi.
     *
     * @throws ApiException {@code SOCIAL_AUTH_FAILED} jika code tidak sah atau GitHub tidak bisa dihubungi
     */
    public SocialProfile fetchProfile(String code) {
        try {
            String accessToken = exchangeCode(code);
            GitHubUser user = restClient.get().uri(API_URL + "/user")
                    .headers(h -> apiHeaders(h, accessToken))
                    .retrieve()
                    .body(GitHubUser.class);
            List<GitHubEmail> emails = restClient.get().uri(API_URL + "/user/emails")
                    .headers(h -> apiHeaders(h, accessToken))
                    .retrieve()
                    .body(GitHubEmail.LIST_TYPE);
            if (user == null || user.id() == null) {
                throw new ApiException(ErrorCode.SOCIAL_AUTH_FAILED);
            }

            GitHubEmail primary = emails == null ? null
                    : emails.stream().filter(GitHubEmail::primary).findFirst().orElse(null);
            String name = user.name() != null && !user.name().isBlank() ? user.name() : user.login();
            return new SocialProfile(
                    IdentityProvider.GITHUB,
                    String.valueOf(user.id()),
                    primary == null ? null : primary.email(),
                    primary != null && primary.verified(),
                    name,
                    user.avatarUrl());
        } catch (RestClientException e) {
            // Pesan exception tidak memuat token; aman ditulis ke log.
            log.warn("Gagal menghubungi GitHub: {}", e.getMessage());
            throw new ApiException(ErrorCode.SOCIAL_AUTH_FAILED);
        }
    }

    private String exchangeCode(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", config.clientId());
        form.add("client_secret", config.clientSecret());
        form.add("code", code);
        form.add("redirect_uri", config.redirectUri());

        TokenResponse response = restClient.post().uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                // Tanpa header ini GitHub membalas dalam format form, bukan JSON.
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .body(TokenResponse.class);
        // GitHub membalas 200 walau code salah/kedaluwarsa; kesalahannya ada di field "error".
        if (response == null || response.accessToken() == null) {
            log.warn("GitHub menolak code OAuth: {}", response == null ? "respons kosong" : response.error());
            throw new ApiException(ErrorCode.SOCIAL_AUTH_FAILED);
        }
        return response.accessToken();
    }

    private static void apiHeaders(HttpHeaders headers, String accessToken) {
        headers.setBearerAuth(accessToken);
        headers.set(HttpHeaders.ACCEPT, "application/vnd.github+json");
        headers.set("X-GitHub-Api-Version", "2022-11-28");
    }

    private static JdkClientHttpRequestFactory requestFactoryWithTimeout() {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(TIMEOUT);
        return factory;
    }

    // Bentuk JSON dari GitHub. @JsonProperty memetakan nama snake_case GitHub ke nama Java.
    record TokenResponse(@JsonProperty("access_token") String accessToken, String error) {
    }

    record GitHubUser(Long id, String login, String name, @JsonProperty("avatar_url") String avatarUrl) {
    }

    record GitHubEmail(String email, boolean primary, boolean verified) {
        // Penanda tipe List<GitHubEmail>; diperlukan karena Java menghapus tipe generik saat runtime.
        static final ParameterizedTypeReference<List<GitHubEmail>> LIST_TYPE = new ParameterizedTypeReference<>() {
        };
    }
}
