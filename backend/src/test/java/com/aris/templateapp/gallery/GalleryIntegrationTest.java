package com.aris.templateapp.gallery;

import com.aris.templateapp.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
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

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Galeri Template untuk pembuat website (alur-pembuatan-website.md bagian 6.2) dan edit profil pembuat website
 * (bagian 5.4). Data uji dibuat lewat SQL di dalam test; tabel templates dikosongkan dulu karena galeri
 * menampilkan template milik SEMUA provider, termasuk sisa test lain.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GalleryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    private UUID studioA;
    private UUID studioB;

    @BeforeEach
    void setUp() throws Exception {
        jdbc.update("DELETE FROM templates");
        studioA = provider("Rina Studio");
        studioB = provider("Budi Design");
    }

    // ---------- galeri ----------

    @Test
    void guestSeesOnlyPublishedTemplatesOfActiveProvidersSortedByDownloads() throws Exception {
        UUID kue = template(studioA, "Toko Kue Modern", "umkm", "published", 3);
        UUID sekolah = template(studioB, "Sekolah Hijau", "sekolah", "published", 2);
        template(studioA, "Draft Rahasia", "umkm", "draft", 1);
        template(studioA, "Gagal Cek", "umkm", "check_failed", 1);
        downloads(kue, 5);
        downloads(sekolah, 2);
        views(sekolah, 10);

        // Tanpa token sama sekali (tamu).
        mockMvc.perform(get("/api/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.items[*].name", contains("Toko Kue Modern", "Sekolah Hijau")))
                .andExpect(jsonPath("$.items[0].creatorName").value("Rina Studio"))
                .andExpect(jsonPath("$.items[0].category").value("umkm"))
                .andExpect(jsonPath("$.items[0].downloads").value(5))
                .andExpect(jsonPath("$.items[0].thumbnailUrl").doesNotExist());

        // Provider ditangguhkan → templatenya ikut hilang dari galeri.
        jdbc.update("UPDATE provider_profiles SET status = 'suspended' WHERE user_id = ?", studioA);
        mockMvc.perform(get("/api/templates"))
                .andExpect(jsonPath("$.items[*].name", contains("Sekolah Hijau")));
    }

    @Test
    void newestSortFollowsPublishDate() throws Exception {
        template(studioA, "Lama", "lainnya", "published", 10);
        template(studioA, "Baru", "lainnya", "published", 1);
        template(studioB, "Tengah", "lainnya", "published", 5);

        mockMvc.perform(get("/api/templates?sort=newest"))
                .andExpect(jsonPath("$.items[*].name", contains("Baru", "Tengah", "Lama")));
    }

    @Test
    void categoryAndSearchFilter() throws Exception {
        template(studioA, "Toko Kue Modern", "umkm", "published", 1);
        template(studioA, "Toko Baju 50%", "umkm", "published", 1);
        template(studioB, "Sekolah Hijau", "sekolah", "published", 1);

        mockMvc.perform(get("/api/templates").param("category", "umkm"))
                .andExpect(jsonPath("$.totalItems").value(2));
        mockMvc.perform(get("/api/templates").param("category", "all").param("q", "TOKO"))
                .andExpect(jsonPath("$.totalItems").value(2));
        // % dicari sebagai huruf biasa, bukan wildcard.
        mockMvc.perform(get("/api/templates").param("q", "50%"))
                .andExpect(jsonPath("$.items[*].name", contains("Toko Baju 50%")));
        mockMvc.perform(get("/api/templates").param("category", "sekolah").param("q", "kue"))
                .andExpect(jsonPath("$.totalItems").value(0))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void paginationAndSizeForRecommendations() throws Exception {
        for (int i = 0; i < 8; i++) {
            template(studioA, "UMKM " + i, "umkm", "published", i + 1);
        }

        // "Template untuk anda": category=tujuan website, sort=popular, size=6.
        mockMvc.perform(get("/api/templates?category=umkm&sort=popular&size=6"))
                .andExpect(jsonPath("$.items", hasSize(6)))
                .andExpect(jsonPath("$.size").value(6))
                .andExpect(jsonPath("$.totalItems").value(8))
                .andExpect(jsonPath("$.totalPages").value(2));
        mockMvc.perform(get("/api/templates?category=umkm&size=6&page=1"))
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Test
    void invalidParametersAreRejected() throws Exception {
        mockMvc.perform(get("/api/templates?sort=acak"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(get("/api/templates?category=hewan"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/templates?size=51"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/templates?page=-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clickingTemplateAsGuestCountsAsView() throws Exception {
        UUID kue = template(studioA, "Toko Kue Modern", "umkm", "published", 1);

        mockMvc.perform(post("/api/templates/" + kue + "/events").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"view\",\"installId\":\"hp-tamu\",\"occurredAt\":\"%s\"}"
                                .formatted(Instant.now())))
                .andExpect(status().isAccepted());

        Integer views = jdbc.queryForObject(
                "SELECT count(*) FROM template_events WHERE template_id = ? AND type = 'view'", Integer.class, kue);
        org.assertj.core.api.Assertions.assertThat(views).isEqualTo(1);
    }

    // ---------- edit profil pembuat website ----------

    @Test
    void creatorProfileCanBeEditedWithoutChangingModeOrOnboarding() throws Exception {
        String token = register();
        mockMvc.perform(post("/api/users/me/onboarding/creator").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Aris\",\"websitePurpose\":\"sekolah\",\"organizationName\":\"SMK 1\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/users/me/creator-profile").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"  Aris Muslim \",\"websitePurpose\":\"umkm\",\"organizationName\":\"  \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Aris Muslim"))
                .andExpect(jsonPath("$.creatorProfile.websitePurpose").value("umkm"))
                .andExpect(jsonPath("$.creatorProfile.organizationName").doesNotExist())
                .andExpect(jsonPath("$.onboardingCompleted").value(true))
                .andExpect(jsonPath("$.activeMode").value("creator"));
    }

    @Test
    void creatorProfileValidationAndLogin() throws Exception {
        mockMvc.perform(patch("/api/users/me/creator-profile").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Aris\"}"))
                .andExpect(status().isUnauthorized());

        String token = register();
        mockMvc.perform(patch("/api/users/me/creator-profile").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\" \",\"websitePurpose\":\"hewan\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    // ---------- pembantu ----------

    private String register() throws Exception {
        String body = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Aris\",\"email\":\"user-%s@mail.com\",\"password\":\"rahasia123\"}"
                                .formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    private UUID provider(String creatorName) throws Exception {
        String token = register();
        String me = mockMvc.perform(post("/api/users/me/onboarding/provider")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"creatorName\":\"%s\",\"agreedToTerms\":true}".formatted(creatorName)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(me, "$.id"));
    }

    /** @param publishedDaysAgo dipakai untuk urutan "Terbaru" */
    private UUID template(UUID providerId, String name, String category, String status, int publishedDaysAgo) {
        Timestamp published = "published".equals(status)
                ? Timestamp.from(Instant.now().minus(publishedDaysAgo, ChronoUnit.DAYS)) : null;
        return jdbc.queryForObject("""
                INSERT INTO templates (provider_id, name, category, status, published_at) VALUES (?, ?, ?, ?, ?)
                RETURNING id""", UUID.class, providerId, name, category, status, published);
    }

    private void downloads(UUID templateId, int count) {
        for (int i = 0; i < count; i++) {
            jdbc.update("""
                    INSERT INTO template_events (template_id, type, project_id, install_id, occurred_at)
                    VALUES (?, 'download', ?, 'test', now())""", templateId, UUID.randomUUID().toString());
        }
    }

    private void views(UUID templateId, int count) {
        for (int i = 0; i < count; i++) {
            jdbc.update("""
                    INSERT INTO template_events (template_id, type, install_id, occurred_at)
                    VALUES (?, 'view', 'test', now())""", templateId);
        }
    }
}
