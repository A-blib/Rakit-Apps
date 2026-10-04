package com.aris.templateapp.provider;

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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Dashboard Provider (docs/rancangan/alur-provider.md): data uji dibuat langsung lewat SQL di dalam test,
 * sesuai keputusan "data dummy hanya hidup di dalam test" selama fitur Upload belum ada.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProviderDashboardIntegrationTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Jakarta");
    private static final String PROVIDER_FORM = """
            {"creatorName":"Studio Aris","agreedToTerms":true}""";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    private String token;
    private UUID providerId;

    @BeforeEach
    void registerProvider() throws Exception {
        token = register();
        String me = send(post("/api/users/me/onboarding/provider"), PROVIDER_FORM)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        providerId = UUID.fromString(JsonPath.read(me, "$.id"));
    }

    // ---------- akses ----------

    @Test
    void nonProviderAndSuspendedProviderAreRejected() throws Exception {
        String creatorToken = register();
        mockMvc.perform(get("/api/providers/me/dashboard").header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PROVIDER_REQUIRED"));

        jdbc.update("UPDATE provider_profiles SET status = 'suspended' WHERE user_id = ?", providerId);
        send(get("/api/providers/me/dashboard"), null)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PROVIDER_SUSPENDED"));
    }

    // ---------- Beranda ----------

    @Test
    void newProviderSeesEmptyDashboardWithChecklist() throws Exception {
        send(get("/api/providers/me/dashboard"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creatorName").value("Studio Aris"))
                .andExpect(jsonPath("$.hasTemplates").value(false))
                .andExpect(jsonPath("$.profileComplete").value(false))
                .andExpect(jsonPath("$.actionItems[*].kind", contains("PROFILE_INCOMPLETE")))
                .andExpect(jsonPath("$.summary.active").value(0))
                .andExpect(jsonPath("$.summary.downloads").value(0))
                // 7 titik bernilai 0, bukan daftar kosong.
                .andExpect(jsonPath("$.downloadTrend", hasSize(7)))
                .andExpect(jsonPath("$.downloadTrend[6].date").value(today().toString()))
                .andExpect(jsonPath("$.downloadTrend[*].count", contains(0, 0, 0, 0, 0, 0, 0)))
                .andExpect(jsonPath("$.popular", hasSize(0)));
    }

    @Test
    void summaryTrendAndPopularFollowThePeriod() throws Exception {
        UUID a = template("Profil Sekolah", "sekolah", "published", 0);
        UUID b = template("UMKM Kuliner", "umkm", "published", 0);
        UUID c = template("Portofolio", "pribadi", "published", 0);
        template("Landing Event", "lainnya", "check_failed", 0);

        // a: 3 download (hari ini 2, kemarin 1) · b: 3 download hari ini tapi lebih sedikit dilihat · c: 1 download 10 hari lalu
        download(a, 0); download(a, 0); download(a, 1);
        download(b, 0); download(b, 0); download(b, 0);
        download(c, 10);
        view(a, 0); view(a, 0); view(a, 2); view(b, 0);

        send(get("/api/providers/me/dashboard?period=7d"), null)
                .andExpect(jsonPath("$.hasTemplates").value(true))
                .andExpect(jsonPath("$.summary.active").value(3))
                .andExpect(jsonPath("$.summary.views").value(4))
                .andExpect(jsonPath("$.summary.downloads").value(6))
                .andExpect(jsonPath("$.downloadTrend[*].count", contains(0, 0, 0, 0, 0, 1, 5)))
                // Download sama (3), a lebih banyak dilihat → a di atas. c tidak punya download di 7 hari terakhir.
                .andExpect(jsonPath("$.popular[*].name", contains("Profil Sekolah", "UMKM Kuliner")))
                .andExpect(jsonPath("$.popular[0].rank").value(1));

        send(get("/api/providers/me/dashboard?period=30d"), null)
                .andExpect(jsonPath("$.summary.downloads").value(7))
                .andExpect(jsonPath("$.downloadTrend", hasSize(30)))
                .andExpect(jsonPath("$.popular[*].name", contains("Profil Sekolah", "UMKM Kuliner", "Portofolio")));
    }

    @Test
    void downloadAtEarlyMorningWibCountsForThatWibDate() throws Exception {
        UUID a = template("Profil Sekolah", "sekolah", "published", 0);
        // 00.30 WIB hari ini = 17.30 UTC kemarin. Harus masuk ke tanggal hari ini (WIB), bukan kemarin.
        insertEvent(a, "download", UUID.randomUUID().toString(), today().atTime(LocalTime.of(0, 30)));

        send(get("/api/providers/me/stats/downloads?period=7d"), null)
                .andExpect(jsonPath("$", hasSize(7)))
                .andExpect(jsonPath("$[6].count").value(1))
                .andExpect(jsonPath("$[5].count").value(0));
    }

    @Test
    void actionItemsListProblemsAndDisappearWhenFixed() throws Exception {
        UUID failed = template("Landing Event", "lainnya", "check_failed", 1);
        UUID check = check(failed, 1, "failed");
        issue(check, "error"); issue(check, "error"); issue(check, "warning");
        UUID warned = template("Profil Sekolah", "sekolah", "published", 3);
        template("Draft Baru", "umkm", "draft", 0);
        template("Rapi", "umkm", "published", 0);

        send(get("/api/providers/me/dashboard"), null)
                .andExpect(jsonPath("$.actionItems[*].kind",
                        contains("TEMPLATE_CHECK_FAILED", "TEMPLATE_WARNING", "DRAFT", "PROFILE_INCOMPLETE")))
                .andExpect(jsonPath("$.actionItems[0].errorCount").value(2))
                .andExpect(jsonPath("$.actionItems[1].warningCount").value(3));

        // Peringatan beres + profil dilengkapi → kartunya hilang tanpa dihapus manual.
        jdbc.update("UPDATE templates SET warning_count = 0 WHERE id = ?", warned);
        send(patch("/api/providers/me/profile"), """
                {"creatorName":"Studio Aris","bio":"Template sekolah","specialties":["Sekolah"]}""").andExpect(status().isOk());
        send(get("/api/providers/me/dashboard"), null)
                .andExpect(jsonPath("$.profileComplete").value(true))
                .andExpect(jsonPath("$.actionItems[*].kind", contains("TEMPLATE_CHECK_FAILED", "DRAFT")));
    }

    // ---------- Template Anda ----------

    @Test
    void templateListFiltersCountsSortsAndSearches() throws Exception {
        UUID sekolah = template("Profil Sekolah", "sekolah", "published", 2);
        UUID kuliner = template("UMKM Kuliner", "umkm", "published", 0);
        template("Landing Event", "lainnya", "check_failed", 0);
        template("Draft Toko", "umkm", "draft", 0);
        template("Diskon_50%", "umkm", "disabled", 0);
        download(kuliner, 0); download(kuliner, 1);

        send(get("/api/providers/me/templates"), null)
                .andExpect(jsonPath("$.counts.all").value(5))
                .andExpect(jsonPath("$.counts.published").value(2))
                .andExpect(jsonPath("$.counts.needsFix").value(2))
                .andExpect(jsonPath("$.counts.draft").value(1))
                .andExpect(jsonPath("$.counts.disabled").value(1))
                .andExpect(jsonPath("$.totalItems").value(5));

        send(get("/api/providers/me/templates?status=needs_fix&sort=name"), null)
                .andExpect(jsonPath("$.items[*].name", contains("Landing Event", "Profil Sekolah")));

        send(get("/api/providers/me/templates?status=published&sort=downloads"), null)
                .andExpect(jsonPath("$.items[0].name").value("UMKM Kuliner"))
                .andExpect(jsonPath("$.items[0].downloads").value(2));

        send(get("/api/providers/me/templates?category=umkm&sort=name"), null)
                .andExpect(jsonPath("$.items[*].name", contains("Diskon_50%", "Draft Toko", "UMKM Kuliner")))
                .andExpect(jsonPath("$.counts.all").value(3));

        // Pencarian tidak membedakan huruf besar/kecil, dan % / _ dicari sebagai huruf biasa.
        send(get("/api/providers/me/templates?q=SEKOLAH"), null)
                .andExpect(jsonPath("$.items[*].id", contains(sekolah.toString())));
        send(get("/api/providers/me/templates").param("q", "50%"), null)
                .andExpect(jsonPath("$.items[*].name", contains("Diskon_50%")));

        send(get("/api/providers/me/templates?status=apa"), null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void templateListIsPaginatedBy20() throws Exception {
        for (int i = 1; i <= 25; i++) {
            template("Template %02d".formatted(i), "umkm", "published", 0);
        }

        send(get("/api/providers/me/templates?sort=name&page=0"), null)
                .andExpect(jsonPath("$.items", hasSize(20)))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.items[0].name").value("Template 01"));
        send(get("/api/providers/me/templates?sort=name&page=1"), null)
                .andExpect(jsonPath("$.items", hasSize(5)))
                .andExpect(jsonPath("$.items[0].name").value("Template 21"));
    }

    @Test
    void templateDetailShowsLatestCheckAndHidesOthersTemplates() throws Exception {
        UUID id = template("Landing Event", "lainnya", "check_failed", 1);
        UUID old = check(id, 1, "failed");
        issue(old, "error");
        UUID latest = check(id, 2, "failed");
        issue(latest, "error"); issue(latest, "warning");

        send(get("/api/providers/me/templates/" + id), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("check_failed"))
                .andExpect(jsonPath("$.latestCheck.version").value(2))
                .andExpect(jsonPath("$.latestCheck.errors", hasSize(1)))
                .andExpect(jsonPath("$.latestCheck.warnings", hasSize(1)))
                .andExpect(jsonPath("$.latestCheck.errors[0].suggestion").exists());
        send(get("/api/templates/" + id + "/checks/latest"), null)
                .andExpect(jsonPath("$.version").value(2));

        // Provider lain tidak boleh melihat (404, bukan 403, agar keberadaan template tidak bocor).
        String otherToken = register();
        mockMvc.perform(post("/api/users/me/onboarding/provider").header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken)
                .contentType(MediaType.APPLICATION_JSON).content(PROVIDER_FORM)).andExpect(status().isOk());
        mockMvc.perform(get("/api/providers/me/templates/" + id).header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken))
                .andExpect(status().isNotFound());
    }

    // ---------- event ----------

    @Test
    void eventsCountGuestsOnceAndIgnoreOwnerAndUnpublished() throws Exception {
        UUID published = template("Profil Sekolah", "sekolah", "published", 0);
        UUID draft = template("Draft", "umkm", "draft", 0);
        String now = java.time.Instant.now().toString();

        // Tamu (tanpa token): view & download dihitung.
        guestEvent(published, "{\"type\":\"view\",\"installId\":\"hp-1\",\"occurredAt\":\"%s\"}".formatted(now));
        guestEvent(published, "{\"type\":\"download\",\"projectId\":\"p-1\",\"installId\":\"hp-1\",\"occurredAt\":\"%s\"}".formatted(now));
        // Export ulang project yang sama tidak menambah angka.
        guestEvent(published, "{\"type\":\"download\",\"projectId\":\"p-1\",\"installId\":\"hp-1\",\"occurredAt\":\"%s\"}".formatted(now));
        // Template belum tayang tidak dihitung.
        guestEvent(draft, "{\"type\":\"view\",\"installId\":\"hp-1\",\"occurredAt\":\"%s\"}".formatted(now));
        // Pemilik membuka templatenya sendiri: diabaikan.
        send(post("/api/templates/" + published + "/events"),
                "{\"type\":\"view\",\"installId\":\"hp-owner\",\"occurredAt\":\"%s\"}".formatted(now))
                .andExpect(status().isAccepted());

        assertThat(jdbc.queryForObject("SELECT count(*) FROM template_events WHERE template_id = ? AND type = 'view'",
                Integer.class, published)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM template_events WHERE template_id = ? AND type = 'download'",
                Integer.class, published)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM template_events WHERE template_id = ?",
                Integer.class, draft)).isZero();
    }

    @Test
    void invalidEventsAreRejected() throws Exception {
        UUID published = template("Profil Sekolah", "sekolah", "published", 0);
        String now = java.time.Instant.now().toString();

        mockMvc.perform(post("/api/templates/" + published + "/events").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"download\",\"installId\":\"hp-1\",\"occurredAt\":\"%s\"}".formatted(now)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/templates/" + published + "/events").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"view\",\"installId\":\"hp-1\",\"occurredAt\":\"2099-01-01T00:00:00Z\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/templates/" + UUID.randomUUID() + "/events").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"view\",\"installId\":\"hp-1\",\"occurredAt\":\"%s\"}".formatted(now)))
                .andExpect(status().isNotFound());
    }

    // ---------- profil & notifikasi ----------

    @Test
    void profileCanBeReadAndEdited() throws Exception {
        UUID a = template("Profil Sekolah", "sekolah", "published", 0);
        download(a, 0);

        send(get("/api/providers/me/profile"), null)
                .andExpect(jsonPath("$.creatorName").value("Studio Aris"))
                .andExpect(jsonPath("$.status").value("active"))
                .andExpect(jsonPath("$.publishedCount").value(1))
                .andExpect(jsonPath("$.totalDownloads").value(1));

        send(patch("/api/providers/me/profile"), """
                {"creatorName":"Studio Baru","bio":"Bio","portfolioUrl":"https://aris.dev","specialties":["UMKM","UMKM"]}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creatorName").value("Studio Baru"))
                .andExpect(jsonPath("$.specialties", contains("UMKM")));

        send(patch("/api/providers/me/profile"), "{\"creatorName\":\"\",\"portfolioUrl\":\"aris.dev\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.creatorName").exists())
                .andExpect(jsonPath("$.fieldErrors.portfolioUrl").exists());
    }

    @Test
    void notificationsCanBeListedCountedAndRead() throws Exception {
        UUID template = template("Landing Event", "lainnya", "check_failed", 0);
        UUID notification = jdbc.queryForObject("""
                INSERT INTO notifications (user_id, type, template_id, title, body)
                VALUES (?, 'TEMPLATE_CHECK_FAILED', ?, 'Landing Event tidak lolos', 'Ada 2 error') RETURNING id""",
                UUID.class, providerId, template);

        send(get("/api/notifications/unread-count"), null).andExpect(jsonPath("$.count").value(1));
        send(get("/api/notifications"), null)
                .andExpect(jsonPath("$[0].type").value("TEMPLATE_CHECK_FAILED"))
                .andExpect(jsonPath("$[0].read").value(false));
        send(patch("/api/notifications/" + notification + "/read"), null).andExpect(status().isNoContent());
        send(get("/api/notifications/unread-count"), null).andExpect(jsonPath("$.count").value(0));

        String otherToken = register();
        mockMvc.perform(patch("/api/notifications/" + notification + "/read")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken))
                .andExpect(status().isNotFound());
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

    private ResultActions send(MockHttpServletRequestBuilder request, String json) throws Exception {
        request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        if (json != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return mockMvc.perform(request);
    }

    private void guestEvent(UUID templateId, String json) throws Exception {
        mockMvc.perform(post("/api/templates/" + templateId + "/events").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isAccepted());
    }

    private UUID template(String name, String category, String status, int warnings) {
        return jdbc.queryForObject("""
                INSERT INTO templates (provider_id, name, category, status, warning_count) VALUES (?, ?, ?, ?, ?)
                RETURNING id""", UUID.class, providerId, name, category, status, warnings);
    }

    private UUID check(UUID templateId, int version, String status) {
        return jdbc.queryForObject("""
                INSERT INTO template_checks (template_id, version, status, finished_at) VALUES (?, ?, ?, now())
                RETURNING id""", UUID.class, templateId, version, status);
    }

    private void issue(UUID checkId, String severity) {
        jdbc.update("""
                INSERT INTO template_check_issues (check_id, severity, code, message, suggestion)
                VALUES (?, ?, 'CODE', 'Masalah', 'Saran perbaikan')""", checkId, severity);
    }

    /** Download pada tengah hari WIB, {@code daysAgo} hari sebelum hari ini. */
    private void download(UUID templateId, int daysAgo) {
        insertEvent(templateId, "download", UUID.randomUUID().toString(), today().minusDays(daysAgo).atTime(12, 0));
    }

    private void view(UUID templateId, int daysAgo) {
        insertEvent(templateId, "view", null, today().minusDays(daysAgo).atTime(12, 0));
    }

    private void insertEvent(UUID templateId, String type, String projectId, java.time.LocalDateTime wibTime) {
        jdbc.update("""
                INSERT INTO template_events (template_id, type, project_id, install_id, occurred_at)
                VALUES (?, ?, ?, 'test', ?)""", templateId, type, projectId,
                Timestamp.from(wibTime.atZone(ZONE).toInstant()));
    }

    private static LocalDate today() {
        return LocalDate.now(ZONE);
    }
}
