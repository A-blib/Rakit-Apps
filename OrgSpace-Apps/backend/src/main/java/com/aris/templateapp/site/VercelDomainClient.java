package com.aris.templateapp.site;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Mendaftarkan domain milik organisasi ke project Vercel OrgSpace-Hosting (Vercel hanya melayani domain yang terdaftar
 * di project-nya, lalu membuat sertifikat HTTPS sendiri). Satu token dipakai untuk semua domain. Tanpa
 * {@code VERCEL_TOKEN}/{@code VERCEL_PROJECT_ID} (mis. di laptop atau Hosting di VPS dengan Caddy) semua aksi dilewati.
 */
@Slf4j
@Component
public class VercelDomainClient {

    static final String API_URL = "https://api.vercel.com";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final String token;
    private final String projectId;
    private final String teamId;
    private final RestClient restClient;

    @Autowired
    public VercelDomainClient(@Value("${app.vercel.token:}") String token,
                              @Value("${app.vercel.project-id:}") String projectId,
                              @Value("${app.vercel.team-id:}") String teamId) {
        this(token, projectId, teamId, RestClient.builder().requestFactory(
                new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(TIMEOUT).build())));
    }

    // Dipakai test untuk memasang server palsu (MockRestServiceServer) ke builder.
    VercelDomainClient(String token, String projectId, String teamId, RestClient.Builder builder) {
        this.token = token == null ? "" : token.strip();
        this.projectId = projectId == null ? "" : projectId.strip();
        this.teamId = teamId == null ? "" : teamId.strip();
        this.restClient = builder.build();
    }

    public boolean enabled() {
        return !token.isEmpty() && !projectId.isEmpty();
    }

    /**
     * Mendaftarkan domain ke project. Domain yang sudah terdaftar di project ini dianggap berhasil.
     *
     * @return catatan untuk pengguna jika Vercel masih meminta bukti kepemilikan tambahan (record TXT), atau null
     */
    @SuppressWarnings("unchecked")
    public String addDomain(String hostname) {
        if (!enabled()) {
            return null;
        }
        try {
            Map<String, Object> result = restClient.post().uri(url("/v10/projects/" + projectId + "/domains"))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .body(Map.of("name", hostname))
                    .retrieve().body(Map.class);
            return verificationNote(result);
        } catch (HttpClientErrorException.Conflict e) {
            // Sudah ada: aman jika memang di project ini; jika di project/akun lain, Vercel menolak melayaninya.
            Map<String, Object> existing = projectDomain(hostname);
            if (existing != null) {
                return verificationNote(existing);
            }
            log.warn("Domain {} ditolak Vercel: {}", hostname, e.getResponseBodyAsString());
            throw new ApiException(ErrorCode.DOMAIN_PROVIDER_FAILED,
                    "Domain ini sudah dipakai di akun Vercel lain. Lepas dulu dari sana, lalu verifikasi lagi.");
        } catch (RestClientException e) {
            log.warn("Gagal mendaftarkan domain {} ke Vercel: {}", hostname, e.getMessage());
            throw new ApiException(ErrorCode.DOMAIN_PROVIDER_FAILED);
        }
    }

    /** Melepas domain dari project. Domain yang sudah tidak ada dianggap berhasil; kegagalan lain hanya dicatat. */
    public void removeDomain(String hostname) {
        if (!enabled()) {
            return;
        }
        try {
            restClient.delete().uri(url("/v9/projects/" + projectId + "/domains/" + hostname))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve().toBodilessEntity();
        } catch (HttpClientErrorException.NotFound e) {
            // Sudah tidak terdaftar.
        } catch (RestClientException e) {
            log.warn("Gagal melepas domain {} dari Vercel (hapus manual di Vercel → Domains): {}", hostname, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> projectDomain(String hostname) {
        try {
            return restClient.get().uri(url("/v9/projects/" + projectId + "/domains/" + hostname))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve().body(Map.class);
        } catch (RestClientException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static String verificationNote(Map<String, Object> domain) {
        if (domain == null || !Boolean.FALSE.equals(domain.get("verified"))) {
            return null;
        }
        Object verification = domain.get("verification");
        if (verification instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> first) {
            return "Server hosting meminta bukti tambahan: buat record " + first.get("type") + " dengan nama "
                    + first.get("domain") + " dan isi " + first.get("value") + ", lalu tekan Verifikasi lagi.";
        }
        return "Server hosting belum memverifikasi domain ini. Tekan Verifikasi lagi beberapa saat lagi.";
    }

    private String url(String path) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(API_URL + path);
        if (!teamId.isEmpty()) {
            uri.queryParam("teamId", teamId);
        }
        return uri.toUriString();
    }
}
