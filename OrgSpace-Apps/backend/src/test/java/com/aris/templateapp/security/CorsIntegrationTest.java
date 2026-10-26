package com.aris.templateapp.security;

import com.aris.templateapp.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CORS hanya untuk Portal Pengurus: alamat Hosting (di test: http://localhost:3001 dari application.yml), varian www,
 * dan domain kustom aktif. Header Access-Control-Allow-Origin yang tidak ada = browser menolak membaca jawaban.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorsIntegrationTest {

    private static final String PORTAL_API = "/api/public/sites/osis/admin/news";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void hostingAddressAndItsWwwVariantAreAllowed() throws Exception {
        preflight("http://localhost:3001")
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3001"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, org.hamcrest.Matchers.containsString("POST")));
        preflight("http://www.localhost:3001")
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://www.localhost:3001"));
    }

    @Test
    void otherWebsitesAndSandboxedPagesAreNotAllowed() throws Exception {
        for (String origin : new String[]{"https://website-jahat.com", "null", "http://localhost:3001.jahat.com",
                "http://localhost:9999"}) {
            preflight(origin).andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
            mockMvc.perform(get("/api/meta/categories").header(HttpHeaders.ORIGIN, origin))
                    .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        }
    }

    @Test
    void publicSiteEndpointsDoNotGrantEveryOriginEither() throws Exception {
        // Dulu PublicSiteController memakai @CrossOrigin("*"), yang menambah izin "*" untuk request biasa (bukan
        // preflight) ke endpoint website publik, melewati aturan CorsOriginPolicy.
        UUID site = site();
        String slug = jdbc.queryForObject("SELECT slug FROM sites WHERE id = ?", String.class, site);
        mockMvc.perform(get("/api/public/sites/" + slug + "/collections/prestasi").header(HttpHeaders.ORIGIN, "https://website-jahat.com"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        mockMvc.perform(get("/api/public/sites/" + slug + "/collections/prestasi").header(HttpHeaders.ORIGIN, "http://localhost:3001"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3001"));
    }

    @Test
    void activeCustomDomainsAreAllowedButPendingOnesAreNot() throws Exception {
        String active = "osis-" + UUID.randomUUID().toString().substring(0, 8) + ".sch.id";
        String pending = "calon-" + UUID.randomUUID().toString().substring(0, 8) + ".sch.id";
        UUID site = site();
        jdbc.update("INSERT INTO site_domains (site_id, hostname, status, verification_token) VALUES (?, ?, 'active', 'x')", site, active);
        jdbc.update("INSERT INTO site_domains (site_id, hostname, status, verification_token) VALUES (?, ?, 'pending', 'y')", site, pending);

        preflight("https://" + active)
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://" + active));
        preflight("https://" + pending).andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void requestsWithoutOriginLikeTheAndroidAppAreUnaffected() throws Exception {
        mockMvc.perform(get("/api/meta/categories"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    private ResultActions preflight(String origin) throws Exception {
        return mockMvc.perform(options(PORTAL_API)
                .header(HttpHeaders.ORIGIN, origin)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"));
    }

    private UUID site() {
        String slug = "cors-" + UUID.randomUUID().toString().substring(0, 8);
        UUID user = jdbc.queryForObject("INSERT INTO users (display_name, email) VALUES ('Uji CORS', ?) RETURNING id",
                UUID.class, slug + "@orgspace.test");
        UUID org = jdbc.queryForObject(
                "INSERT INTO organizations (type, name, created_by) VALUES ('personal', 'Ruang Uji', ?) RETURNING id", UUID.class, user);
        UUID project = jdbc.queryForObject(
                "INSERT INTO projects (organization_id, name, mode) VALUES (?, 'Web Uji', 'template') RETURNING id", UUID.class, org);
        return jdbc.queryForObject(
                "INSERT INTO sites (project_id, organization_id, slug, status) VALUES (?, ?, ?, 'online') RETURNING id",
                UUID.class, project, org, slug);
    }
}
