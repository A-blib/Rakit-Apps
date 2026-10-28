package com.aris.templateapp.site;

import com.aris.templateapp.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * IP asli pengunjung di belakang Caddy/Hosting. Memakai HTTP sungguhan (bukan MockMvc) karena
 * {@code forward-headers-strategy: native} dikerjakan Tomcat, yang tidak dilewati MockMvc. Nilainya sama dengan
 * application-prod.yml.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"server.forward-headers-strategy=native", "app.sites.hosting-key=kunci-hosting-uji"})
@ActiveProfiles("test")
class ForwardedClientIpIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbc;

    private final HttpClient http = HttpClient.newHttpClient();

    @Test
    void formSubmissionsUseTheVisitorIpFromTheProxyAndAreLimitedPerVisitor() throws Exception {
        String slug = "ip-asli-" + UUID.randomUUID().toString().substring(0, 8);
        UUID siteId = onlineSite(slug);

        // Request datang dari proxy lokal (127.0.0.1) yang membawa IP pengunjung, seperti Caddy/Hosting di VPS.
        assertThat(submit(slug, "203.0.113.7")).isEqualTo(200);
        assertThat(jdbc.queryForObject("SELECT ip_address FROM form_submissions WHERE site_id = ?", String.class, siteId))
                .isEqualTo("203.0.113.7");

        for (int i = 1; i < 30; i++) {
            assertThat(submit(slug, "203.0.113.7")).isEqualTo(200);
        }
        HttpResponse<String> limited = http.send(request(slug, "203.0.113.7"), HttpResponse.BodyHandlers.ofString());
        assertThat(limited.statusCode()).as("kiriman ke-31 dalam semenit").isEqualTo(429);
        // Pesan ini yang ditampilkan di bawah formulir website pengunjung.
        assertThat(limited.body()).contains("Terlalu banyak kiriman dari jaringan ini");
        // Pengunjung lain (IP lain) tetap bisa mengirim: batasnya per pengunjung, bukan untuk semua orang.
        assertThat(submit(slug, "198.51.100.20")).isEqualTo(200);
    }

    @Test
    void hostingOnVercelSendsTheVisitorIpWithTheSharedKey() throws Exception {
        String slug = "ip-vercel-" + UUID.randomUUID().toString().substring(0, 8);
        UUID siteId = onlineSite(slug);

        // Hosting di Vercel: request datang dari IP Vercel, IP asli pengunjung di header khusus + kunci yang cocok.
        assertThat(http.send(hostingRequest(slug, "203.0.113.50", "kunci-hosting-uji"), HttpResponse.BodyHandlers.discarding())
                .statusCode()).isEqualTo(200);
        // Kunci salah: header IP diabaikan, yang dicatat IP request biasa.
        assertThat(http.send(hostingRequest(slug, "198.51.100.99", "kunci-palsu"), HttpResponse.BodyHandlers.discarding())
                .statusCode()).isEqualTo(200);
        assertThat(jdbc.queryForList("SELECT ip_address FROM form_submissions WHERE site_id = ? ORDER BY created_at",
                String.class, siteId)).containsExactly("203.0.113.50", "10.0.0.1");
    }

    private HttpRequest hostingRequest(String slug, String visitorIp, String key) {
        return HttpRequest.newBuilder(
                        URI.create("http://localhost:" + port + "/api/public/sites/" + slug + "/forms/pendaftaran"))
                .header("Content-Type", "application/json")
                .header("X-Forwarded-For", "10.0.0.1")
                .header("X-OrgSpace-Visitor-IP", visitorIp)
                .header("X-OrgSpace-Hosting-Key", key)
                .POST(HttpRequest.BodyPublishers.ofString("{\"nama\":\"Rina\"}"))
                .build();
    }

    private int submit(String slug, String visitorIp) throws Exception {
        return http.send(request(slug, visitorIp), HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    private HttpRequest request(String slug, String visitorIp) {
        return HttpRequest.newBuilder(
                        URI.create("http://localhost:" + port + "/api/public/sites/" + slug + "/forms/pendaftaran"))
                .header("Content-Type", "application/json")
                .header("X-Forwarded-For", visitorIp)
                .POST(HttpRequest.BodyPublishers.ofString("{\"nama\":\"Rina\"}"))
                .build();
    }

    private UUID onlineSite(String slug) {
        UUID user = jdbc.queryForObject("INSERT INTO users (display_name, email) VALUES ('Uji IP', ?) RETURNING id",
                UUID.class, slug + "@orgspace.test");
        UUID org = jdbc.queryForObject(
                "INSERT INTO organizations (type, name, created_by) VALUES ('personal', 'Ruang Uji', ?) RETURNING id",
                UUID.class, user);
        UUID project = jdbc.queryForObject(
                "INSERT INTO projects (organization_id, name, mode) VALUES (?, 'Web Uji', 'template') RETURNING id",
                UUID.class, org);
        return jdbc.queryForObject(
                "INSERT INTO sites (project_id, organization_id, slug, status) VALUES (?, ?, ?, 'online') RETURNING id",
                UUID.class, project, org, slug);
    }
}
