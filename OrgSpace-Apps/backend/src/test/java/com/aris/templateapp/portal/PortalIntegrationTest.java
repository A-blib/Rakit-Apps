package com.aris.templateapp.portal;

import com.aris.templateapp.TestcontainersConfiguration;
import com.aris.templateapp.monitoring.ClientPlatform;
import com.aris.templateapp.monitoring.UsageRecorder;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Fondasi API portal web (docs/rancangan/portal-web.md): akses admin, kritik & saran, dokumentasi, monitoring. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "app.portal.feedback-daily-limit=3")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PortalIntegrationTest {

    private static final String FEEDBACK = """
            {"category":"bug","subject":"Foto hilang","message":"Foto utama tidak tampil di HP teman.","rating":3,
             "appVersion":"1.0.0","deviceInfo":"Pixel 7"}""";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UsageRecorder usageRecorder;

    private Account admin;
    private Account user;

    @BeforeEach
    void setUp() throws Exception {
        admin = register("Tim Admin");
        user = register("Rina");
        jdbc.update("UPDATE users SET is_admin = true WHERE id = ?", admin.id);
    }

    @Test
    void adminEndpointsNeedAdminAccount() throws Exception {
        mockMvc.perform(get("/api/admin/me"))
                .andExpect(status().isUnauthorized());
        send(user, get("/api/admin/me"), null)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ADMIN_ONLY"));
        send(user, get("/api/admin/stats/overview"), null)
                .andExpect(jsonPath("$.code").value("ADMIN_ONLY"));
        send(admin, get("/api/admin/me"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Tim Admin"));

        // Hak admin dicabut langsung berlaku, tanpa menunggu token kedaluwarsa.
        jdbc.update("UPDATE users SET is_admin = false WHERE id = ?", admin.id);
        send(admin, get("/api/admin/me"), null).andExpect(status().isForbidden());
    }

    @Test
    void feedbackConversationBetweenSenderAndStaff() throws Exception {
        String created = send(user, post("/api/feedback").header(ClientPlatform.HEADER, "android"), FEEDBACK)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("new"))
                .andExpect(jsonPath("$.senderRole").value("creator"))
                .andExpect(jsonPath("$.source").value("android"))
                .andExpect(jsonPath("$.messages", hasSize(1)))
                // Info perangkat hanya untuk tim internal.
                .andExpect(jsonPath("$.appVersion").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(created, "$.id");

        send(admin, get("/api/admin/feedback/counts"), null)
                .andExpect(jsonPath("$.newCount").value(1))
                .andExpect(jsonPath("$.unread").value(1));
        send(admin, get("/api/admin/feedback?status=new&q=foto"), null)
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].unread").value(true))
                .andExpect(jsonPath("$.items[0].sender.displayName").value("Rina"))
                .andExpect(jsonPath("$.totalItems").value(1));
        send(admin, get("/api/admin/feedback?status=aneh"), null).andExpect(status().isBadRequest());

        send(admin, get("/api/admin/feedback/" + id), null)
                .andExpect(jsonPath("$.appVersion").value("1.0.0"))
                .andExpect(jsonPath("$.sender.email").exists());
        send(admin, post("/api/admin/feedback/" + id + "/messages"), "{\"body\":\"Sedang kami cek, ya.\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("in_progress"))
                .andExpect(jsonPath("$.messages", hasSize(2)));

        // Pengirim melihat tanda balasan baru, dan nama staf disamarkan.
        send(user, get("/api/feedback/unread-count"), null).andExpect(jsonPath("$.count").value(1));
        send(user, get("/api/feedback"), null)
                .andExpect(jsonPath("$.items[0].unread").value(true))
                .andExpect(jsonPath("$.items[0].messageCount").value(2))
                .andExpect(jsonPath("$.items[0].sender").doesNotExist());
        send(user, get("/api/feedback/" + id), null)
                .andExpect(jsonPath("$.messages[1].fromStaff").value(true))
                .andExpect(jsonPath("$.messages[1].authorName").value("Tim OrgSpace"));
        send(user, get("/api/feedback/unread-count"), null).andExpect(jsonPath("$.count").value(0));

        // Selesai → pengirim menanggapi → terbuka lagi. Ditutup → tidak bisa dibalas.
        send(admin, patch("/api/admin/feedback/" + id), "{\"status\":\"resolved\"}")
                .andExpect(jsonPath("$.status").value("resolved"));
        send(user, post("/api/feedback/" + id + "/messages"), "{\"body\":\"Masih belum bisa.\"}")
                .andExpect(jsonPath("$.status").value("in_progress"));
        send(admin, get("/api/admin/feedback?unread=true"), null).andExpect(jsonPath("$.items", hasSize(1)));
        send(admin, patch("/api/admin/feedback/" + id), "{\"status\":\"closed\"}").andExpect(status().isOk());
        send(user, post("/api/feedback/" + id + "/messages"), "{\"body\":\"Halo?\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FEEDBACK_CLOSED"));

        // Orang lain tidak bisa membuka feedback ini.
        Account stranger = register("Asing");
        send(stranger, get("/api/feedback/" + id), null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FEEDBACK_NOT_FOUND"));
    }

    @Test
    void feedbackIsValidatedAndLimited() throws Exception {
        send(user, post("/api/feedback"), "{\"category\":\"aneh\",\"subject\":\"\",\"message\":\"x\",\"rating\":9}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.category").exists())
                .andExpect(jsonPath("$.fieldErrors.subject").exists())
                .andExpect(jsonPath("$.fieldErrors.rating").exists());
        send(user, post("/api/feedback"), FEEDBACK.replace("\"rating\":3", "\"rating\":3,\"senderRole\":\"provider\""))
                .andExpect(jsonPath("$.code").value("PROVIDER_REQUIRED"));
        send(user, post("/api/feedback"), FEEDBACK.replace("\"rating\":3",
                        "\"rating\":3,\"organizationId\":\"" + UUID.randomUUID() + "\""))
                .andExpect(jsonPath("$.code").value("NOT_ORG_MEMBER"));
        for (int i = 0; i < 3; i++) {
            send(user, post("/api/feedback").header(ClientPlatform.HEADER, "web"), FEEDBACK)
                    .andExpect(jsonPath("$.source").value("web"));
        }
        send(user, post("/api/feedback"), FEEDBACK)
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("TOO_MANY_ATTEMPTS"));
    }

    @Test
    void docsArePublicOnlyWhenPublished() throws Exception {
        mockMvc.perform(get("/api/public/docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[0].value").value("mulai"))
                .andExpect(jsonPath("$.categories[0].label").value("Memulai"));
        mockMvc.perform(get("/api/public/docs/apa-itu-orgspace"))
                .andExpect(jsonPath("$.body").exists())
                .andExpect(jsonPath("$.categoryLabel").value("Memulai"));

        String doc = """
                {"slug":"terbit-pertama","category":"terbit","title":"Menerbitkan website pertama",
                 "body":"## Langkah\\n1. Ketuk Terbitkan.","status":"draft"}""";
        String created = send(admin, post("/api/admin/docs"), doc)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("draft"))
                .andExpect(jsonPath("$.publishedAt").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(created, "$.id");
        mockMvc.perform(get("/api/public/docs/terbit-pertama")).andExpect(status().isNotFound());
        send(admin, post("/api/admin/docs"), doc).andExpect(jsonPath("$.code").value("DOC_SLUG_TAKEN"));
        send(admin, post("/api/admin/docs"), doc.replace("terbit-pertama", "Bukan Slug"))
                .andExpect(jsonPath("$.fieldErrors.slug").exists());
        send(user, post("/api/admin/docs"), doc).andExpect(status().isForbidden());

        send(admin, put("/api/admin/docs/" + id), doc.replace("\"draft\"", "\"published\""))
                .andExpect(jsonPath("$.publishedAt").exists());
        mockMvc.perform(get("/api/public/docs/terbit-pertama")).andExpect(status().isOk());
        mockMvc.perform(get("/api/public/docs"))
                .andExpect(jsonPath("$.categories[*].value").value(hasItem("terbit")));

        send(admin, delete("/api/admin/docs/" + id), null).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/public/docs"))
                .andExpect(jsonPath("$.categories[*].value").value(not(hasItem("terbit"))));
    }

    @Test
    void monitoringCountsUsersActivityAndTraffic() throws Exception {
        // Request di atas (register, admin/me) dicatat di memori; flush menulisnya ke database.
        send(user, get("/api/users/me").header(ClientPlatform.HEADER, "android"), null).andExpect(status().isOk());
        send(admin, get("/api/admin/me").header(ClientPlatform.HEADER, "web"), null).andExpect(status().isOk());
        usageRecorder.flush();

        send(admin, get("/api/admin/stats/overview?period=7d"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totals.users").isNumber())
                .andExpect(jsonPath("$.period.newUsers.current").isNumber())
                .andExpect(jsonPath("$.active.today").isNumber());
        String series = send(admin, get("/api/admin/stats/timeseries?metric=signups&period=7d"), null)
                .andExpect(jsonPath("$.points", hasSize(7)))
                .andReturn().getResponse().getContentAsString();
        assertThat((Integer) JsonPath.read(series, "$.total")).isGreaterThanOrEqualTo(2);
        send(admin, get("/api/admin/stats/timeseries?metric=entah"), null).andExpect(status().isBadRequest());
        send(admin, get("/api/admin/stats/overview?period=1y"), null).andExpect(status().isBadRequest());

        send(admin, get("/api/admin/stats/active-platforms?period=7d"), null)
                .andExpect(jsonPath("$.android").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.web").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
        send(admin, get("/api/admin/stats/signup-methods?period=7d"), null)
                .andExpect(jsonPath("$.email").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));

        // Rute dicatat sebagai pola (tanpa ID); request 401 juga terhitung.
        mockMvc.perform(get("/api/admin/feedback/" + UUID.randomUUID())).andExpect(status().isUnauthorized());
        send(admin, get("/api/admin/feedback/" + UUID.randomUUID()), null).andExpect(status().isNotFound());
        usageRecorder.flush();
        Long patterned = jdbc.queryForObject("""
                SELECT coalesce(sum(client_error_count), 0) FROM api_traffic_daily
                 WHERE method = 'GET' AND route = '/api/admin/feedback/{id}'""", Long.class);
        assertThat(patterned).isGreaterThanOrEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM api_traffic_daily WHERE route LIKE '%-%-%'", Long.class))
                .isZero();
        send(admin, get("/api/admin/stats/traffic?period=7d"), null)
                .andExpect(jsonPath("$.days", hasSize(7)))
                .andExpect(jsonPath("$.topRoutes[*].route").value(hasItem("/api/auth/register")));

        send(admin, get("/api/admin/users?q=rina"), null)
                .andExpect(jsonPath("$.items[*].displayName").value(hasItem("Rina")))
                .andExpect(jsonPath("$.items[0].loginMethods[0]").value("local"));
        send(admin, get("/api/admin/users/" + user.id), null)
                .andExpect(jsonPath("$.lastActiveDay").exists());
        send(admin, get("/api/admin/system"), null)
                .andExpect(jsonPath("$.databaseOk").value(true))
                .andExpect(jsonPath("$.schemaVersion").value("36"));
    }

    @Test
    void landingOverviewIsPublic() throws Exception {
        mockMvc.perform(get("/api/public/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats.organizations").isNumber())
                .andExpect(jsonPath("$.androidDownloadUrl").doesNotExist());
    }

    @Test
    void landingHeroEditedByAdmin() throws Exception {
        String hero = """
                {"headline":"Website Organisasi,","headlineHighlight":"Sekali Klik!","subheadline":"Subjudul baru.",
                 "primaryCtaLabel":"Coba Gratis","secondaryCtaLabel":"Baca Panduan","secondaryCtaUrl":"/docs/mulai",
                 "highlights":["Gratis","Cepat"],"imageAlt":"Foto tim"}""";
        send(user, put("/api/admin/landing/hero"), hero).andExpect(status().isForbidden());
        send(admin, put("/api/admin/landing/hero"), hero.replace("/docs/mulai", "javascript:alert(1)"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.secondaryCtaUrl").exists());
        send(admin, put("/api/admin/landing/hero"), hero)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.highlights", hasSize(2)));
        mockMvc.perform(get("/api/public/landing/hero"))
                .andExpect(jsonPath("$.headlineHighlight").value("Sekali Klik!"))
                .andExpect(jsonPath("$.highlights[1]").value("Cepat"))
                .andExpect(jsonPath("$.imageUpdatedAt").doesNotExist());
        mockMvc.perform(get("/api/public/landing/hero/image")).andExpect(status().isNotFound());

        // Gambar lebar diperkecil maks 1600 px tanpa dipotong; file bukan gambar ditolak.
        var png = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2000, 1000, BufferedImage.TYPE_INT_RGB), "png", png);
        mockMvc.perform(multipart("/api/admin/landing/hero/image")
                        .file(new MockMultipartFile("file", "hero.png", "image/png", png.toByteArray()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin.token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUpdatedAt").exists());
        byte[] served = mockMvc.perform(get("/api/public/landing/hero/image"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        var image = ImageIO.read(new ByteArrayInputStream(served));
        assertThat(image.getWidth()).isEqualTo(1600);
        assertThat(image.getHeight()).isEqualTo(800);
        mockMvc.perform(multipart("/api/admin/landing/hero/image")
                        .file(new MockMultipartFile("file", "hero.png", "image/png", "<svg onload=alert(1)>".getBytes()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin.token))
                .andExpect(jsonPath("$.code").value("LANDING_IMAGE_INVALID"));

        send(admin, delete("/api/admin/landing/hero/image"), null)
                .andExpect(jsonPath("$.imageUpdatedAt").doesNotExist());
        mockMvc.perform(get("/api/public/landing/hero/image")).andExpect(status().isNotFound());
    }

    @Test
    void landingTemplatesPickedByAdmin() throws Exception {
        UUID provider = admin.id;
        jdbc.update("""
                INSERT INTO provider_profiles (user_id, creator_name, status, agreed_terms_at) VALUES (?, 'Kreator', 'active', now())""",
                provider);
        UUID a = template(provider, "Sekolah", "published");
        UUID b = template(provider, "UMKM", "published");
        UUID draft = template(provider, "Draft", "draft");

        send(user, put("/api/admin/landing/templates"), "{\"templateIds\":[\"%s\"]}".formatted(a))
                .andExpect(status().isForbidden());
        send(admin, put("/api/admin/landing/templates"), "{\"templateIds\":[\"%s\",\"%s\"]}".formatted(a, draft))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LANDING_TEMPLATE_INVALID"));
        send(admin, put("/api/admin/landing/templates"), "{\"templateIds\":[\"%s\",\"%s\"]}".formatted(a, a))
                .andExpect(status().isBadRequest());
        String thirteen = String.join(",", Collections.nCopies(13, "\"" + a + "\""));
        send(admin, put("/api/admin/landing/templates"), "{\"templateIds\":[" + thirteen + "]}")
                .andExpect(jsonPath("$.fieldErrors.templateIds").exists());

        // Urutan pilihan admin dipertahankan.
        send(admin, put("/api/admin/landing/templates"), "{\"templateIds\":[\"%s\",\"%s\"]}".formatted(b, a))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        mockMvc.perform(get("/api/public/landing/templates"))
                .andExpect(jsonPath("$[0].name").value("UMKM"))
                .andExpect(jsonPath("$[1].name").value("Sekolah"));

        // Template yang ditarik dari galeri ikut hilang dari landing.
        jdbc.update("UPDATE templates SET status = 'draft' WHERE id = ?", b);
        mockMvc.perform(get("/api/public/landing/templates"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Sekolah"));
        send(admin, put("/api/admin/landing/templates"), "{\"templateIds\":[]}").andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void portalUpdatesWrittenByAdminAndShownWhenPublished() throws Exception {
        jdbc.update("DELETE FROM portal_updates");
        String draft = """
                {"category":"fitur","title":"Preview template","summary":"Lihat template sebelum dipakai.",
                 "content":"## Apa yang baru\\nTombol preview di landing.","imageAlt":"Tangkapan layar","published":false}""";
        send(user, post("/api/admin/updates"), draft).andExpect(status().isForbidden());
        send(admin, post("/api/admin/updates"), draft.replace("fitur", "rahasia"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.category").exists());
        String created = send(admin, post("/api/admin/updates"), draft)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("draft"))
                .andExpect(jsonPath("$.publishedAt").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(created, "$.id");

        // Id bukan UUID = kesalahan pemanggil (400), bukan error server.
        mockMvc.perform(get("/api/public/updates/bukan-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.id").exists());

        // Draft belum tampil di publik.
        mockMvc.perform(get("/api/public/updates")).andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/public/updates/" + id)).andExpect(jsonPath("$.code").value("UPDATE_NOT_FOUND"));

        var png = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(800, 400, BufferedImage.TYPE_INT_RGB), "png", png);
        mockMvc.perform(multipart("/api/admin/updates/" + id + "/image")
                        .file(new MockMultipartFile("file", "info.png", "image/png", png.toByteArray()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin.token))
                .andExpect(jsonPath("$.imageUpdatedAt").exists());
        mockMvc.perform(get("/api/public/updates/" + id + "/image")).andExpect(status().isNotFound());

        String publishedAt = JsonPath.read(send(admin, put("/api/admin/updates/" + id), draft.replace("false", "true"))
                .andExpect(jsonPath("$.status").value("published"))
                .andReturn().getResponse().getContentAsString(), "$.publishedAt");
        mockMvc.perform(get("/api/public/updates"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Preview template"))
                .andExpect(jsonPath("$[0].content").doesNotExist());
        mockMvc.perform(get("/api/public/updates/" + id))
                .andExpect(jsonPath("$.content").value("## Apa yang baru\nTombol preview di landing."));
        mockMvc.perform(get("/api/public/updates/" + id + "/image")).andExpect(status().isOk());

        // Disembunyikan lalu ditampilkan lagi dari tombol daftar: isi & tanggal tayang pertama tetap.
        send(user, put("/api/admin/updates/" + id + "/published"), "{\"published\":false}").andExpect(status().isForbidden());
        send(admin, put("/api/admin/updates/" + id + "/published"), "{\"published\":false}")
                .andExpect(jsonPath("$.status").value("draft"))
                .andExpect(jsonPath("$.title").value("Preview template"));
        mockMvc.perform(get("/api/public/updates")).andExpect(jsonPath("$", hasSize(0)));
        send(admin, put("/api/admin/updates/" + id + "/published"), "{\"published\":true}")
                .andExpect(jsonPath("$.publishedAt").value(publishedAt));
        mockMvc.perform(get("/api/public/updates")).andExpect(jsonPath("$", hasSize(1)));
        send(admin, put("/api/admin/updates/" + UUID.randomUUID() + "/published"), "{\"published\":true}")
                .andExpect(jsonPath("$.code").value("UPDATE_NOT_FOUND"));

        // Disunting ulang: tanggal tayang pertama tetap.
        send(admin, put("/api/admin/updates/" + id), draft.replace("false", "true").replace("Preview template", "Preview v2"))
                .andExpect(jsonPath("$.publishedAt").value(publishedAt));

        send(admin, delete("/api/admin/updates/" + id), null).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/public/updates/" + id + "/image")).andExpect(status().isNotFound());
        send(admin, get("/api/admin/updates/" + id), null).andExpect(jsonPath("$.code").value("UPDATE_NOT_FOUND"));
    }

    @Test
    void portalUpdateHiddenFromPublicAfterDisplayDays() throws Exception {
        jdbc.update("DELETE FROM portal_updates");
        String info = """
                {"category":"optimasi","title":"Landing lebih cepat","summary":"Gambar dikompres.",
                 "content":"Isi lengkap.","published":true,"displayDays":30}""";
        send(admin, post("/api/admin/updates"), info.replace("30", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.displayDays").exists());
        String id = JsonPath.read(send(admin, post("/api/admin/updates"), info)
                .andExpect(jsonPath("$.displayDays").value(30))
                .andExpect(jsonPath("$.expiresAt").exists())
                .andReturn().getResponse().getContentAsString(), "$.id");
        var png = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB), "png", png);
        mockMvc.perform(multipart("/api/admin/updates/" + id + "/image")
                .file(new MockMultipartFile("file", "info.png", "image/png", png.toByteArray()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin.token));

        // Hari ke-29: masih tampil.
        jdbc.update("UPDATE portal_updates SET published_at = now() - interval '29 days' WHERE id = ?::uuid", id);
        mockMvc.perform(get("/api/public/updates")).andExpect(jsonPath("$", hasSize(1)));

        // Hari ke-31: hilang dari landing, detail, dan gambar publik, tapi tetap ada di dashboard.
        jdbc.update("UPDATE portal_updates SET published_at = now() - interval '31 days' WHERE id = ?::uuid", id);
        mockMvc.perform(get("/api/public/updates")).andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/public/updates/" + id)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/public/updates/" + id + "/image")).andExpect(status().isNotFound());
        send(admin, get("/api/admin/updates"), null)
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("published"))
                .andExpect(jsonPath("$[0].expired").value(true));

        // Diperpanjang jadi 60 hari: tampil lagi. Tanpa batas (null) juga tampil.
        send(admin, put("/api/admin/updates/" + id), info.replace("30", "60")).andExpect(jsonPath("$.expired").value(false));
        mockMvc.perform(get("/api/public/updates")).andExpect(jsonPath("$", hasSize(1)));
        send(admin, put("/api/admin/updates/" + id), info.replace("30", "null"))
                .andExpect(jsonPath("$.expiresAt").doesNotExist());
        jdbc.update("UPDATE portal_updates SET published_at = now() - interval '5000 days' WHERE id = ?::uuid", id);
        mockMvc.perform(get("/api/public/updates/" + id)).andExpect(status().isOk());
    }

    private UUID template(UUID providerId, String name, String status) {
        return jdbc.queryForObject("""
                INSERT INTO templates (provider_id, name, category, status, published_at) VALUES (?, ?, 'ukm', ?, now())
                RETURNING id""", UUID.class, providerId, name, status);
    }

    private record Account(UUID id, String token) {
    }

    private Account register(String name) throws Exception {
        String body = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"%s\",\"email\":\"portal-%s@mail.com\",\"password\":\"rahasia123\"}"
                                .formatted(name, UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return new Account(UUID.fromString(JsonPath.read(body, "$.user.id")), JsonPath.read(body, "$.accessToken"));
    }

    private ResultActions send(Account account, MockHttpServletRequestBuilder request, String json) throws Exception {
        request.header(HttpHeaders.AUTHORIZATION, "Bearer " + account.token);
        if (json != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return mockMvc.perform(request);
    }
}
