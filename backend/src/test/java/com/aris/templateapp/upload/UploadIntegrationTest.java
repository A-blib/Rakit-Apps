package com.aris.templateapp.upload;

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

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Alur Upload langkah 1–2 dari ujung ke ujung (alur-fitur-upload.md bagian 3–5): upload per potongan yang bisa
 * dilanjutkan, pengecekan di latar belakang, hasil lolos/gagal, upload perbaikan, laporan keliru, kuota & draft.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UploadIntegrationTest {

    private static final Path FIXTURES = Path.of("src/test/resources/test-fixtures");
    private static final int CHUNK = 65536;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UploadMaintenance maintenance;

    private String token;
    private UUID providerId;

    @BeforeEach
    void registerProvider() throws Exception {
        token = register();
        String me = send(post("/api/users/me/onboarding/provider"), "{\"creatorName\":\"Studio Aris\",\"agreedToTerms\":true}")
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        providerId = UUID.fromString(JsonPath.read(me, "$.id"));
    }

    @Test
    void cleanZipIsUploadedInChunksAndBecomesDraft() throws Exception {
        // ZIP berisi gambar acak ±80 KB agar terkirim dalam dua potongan 64 KB.
        byte[] zip = Files.readAllBytes(FIXTURES.resolve("PAGE_TOO_HEAVY/gagal.zip"));
        assertThat(zip.length).isGreaterThan(CHUNK);
        String sessionId = startSession("toko-kue.zip", zip.length, null);

        sendChunk(sessionId, 0, Arrays.copyOfRange(zip, 0, CHUNK)).andExpect(status().isOk())
                .andExpect(jsonPath("$.receivedSize").value(CHUNK));
        // Seolah sinyal putus: app mengirim ulang dari awal, server menolak dan memberi tahu posisinya.
        sendChunk(sessionId, 0, Arrays.copyOfRange(zip, 0, CHUNK)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("UPLOAD_OFFSET_MISMATCH"));
        send(get("/api/providers/me/uploads/sessions/" + sessionId), null)
                .andExpect(jsonPath("$.receivedSize").value(CHUNK));
        // Selesai sebelum semua potongan diterima ditolak.
        send(post("/api/providers/me/uploads/sessions/" + sessionId + "/complete"), null)
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("UPLOAD_INCOMPLETE"));
        sendChunk(sessionId, CHUNK, Arrays.copyOfRange(zip, CHUNK, zip.length)).andExpect(status().isOk())
                .andExpect(jsonPath("$.receivedSize").value(zip.length));

        String started = send(post("/api/providers/me/uploads/sessions/" + sessionId + "/complete"), null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.checkVersion").value(1))
                .andReturn().getResponse().getContentAsString();
        String templateId = JsonPath.read(started, "$.templateId");

        String result = waitForCheck(templateId);
        assertThat((String) JsonPath.read(result, "$.status")).isEqualTo("draft");
        assertThat((String) JsonPath.read(result, "$.check.status")).isEqualTo("passed");
        assertThat((String) JsonPath.read(result, "$.check.stage")).isEqualTo("done");
        assertThat((Integer) JsonPath.read(result, "$.wizardStep")).isEqualTo(3);
        assertThat((List<String>) JsonPath.read(result, "$.techInfo.pages")).containsExactly("index.html", "tentang.html");

        send(get("/api/providers/me/uploads"), null).andExpect(status().isOk())
                .andExpect(jsonPath("$.drafts", hasSize(1)))
                .andExpect(jsonPath("$.drafts[0].name").value("Toko Kue"))
                .andExpect(jsonPath("$.drafts[0].wizardStep").value(3))
                .andExpect(jsonPath("$.drafts[0].expiringSoon").value(false))
                .andExpect(jsonPath("$.draftCount").value(1))
                .andExpect(jsonPath("$.canStartNew").value(true));
        send(get("/api/notifications"), null)
                .andExpect(jsonPath("$[*].type", hasItem("UPLOAD_CHECK_PASSED")));
        assertThat(Files.exists(Path.of("target/test-uploads/templates", templateId, "source.zip"))).isTrue();
    }

    @Test
    void failedZipShowsErrorsThenFixUploadReplacesIt() throws Exception {
        String templateId = uploadAndWait("toko-kue.zip", FIXTURES.resolve("CASE_MISMATCH/gagal.zip"), null);
        String result = send(get("/api/providers/me/uploads/" + templateId + "/check"), null)
                .andReturn().getResponse().getContentAsString();
        assertThat((String) JsonPath.read(result, "$.status")).isEqualTo("check_failed");
        assertThat((String) JsonPath.read(result, "$.check.errors[0].code")).isEqualTo("CASE_MISMATCH");
        assertThat((String) JsonPath.read(result, "$.check.errors[0].title")).isEqualTo("Huruf besar/kecil nama file tidak cocok");
        assertThat((String) JsonPath.read(result, "$.check.errors[0].file")).isEqualTo("index.html");
        assertThat((Integer) JsonPath.read(result, "$.wizardStep")).isEqualTo(2);

        // Muncul di "Perlu diperbaiki" (nama file ZIP) dan di "Perlu tindakan" Beranda.
        send(get("/api/providers/me/uploads"), null)
                .andExpect(jsonPath("$.needsFix", hasSize(1)))
                .andExpect(jsonPath("$.needsFix[0].fileName").value("toko-kue.zip"))
                .andExpect(jsonPath("$.needsFix[0].errorCount").value(1))
                .andExpect(jsonPath("$.draftCount").value(0));
        send(get("/api/providers/me/dashboard"), null)
                .andExpect(jsonPath("$.actionItems[0].kind").value("TEMPLATE_CHECK_FAILED"))
                .andExpect(jsonPath("$.actionItems[0].templateName").value("toko-kue.zip"));

        // "Ini keliru? Laporkan": sekali per Error.
        String issueId = JsonPath.read(result, "$.check.errors[0].id");
        send(post("/api/providers/me/uploads/issues/" + issueId + "/report"), "{\"reason\":\"Nama file sudah benar\"}")
                .andExpect(status().isCreated());
        send(post("/api/providers/me/uploads/issues/" + issueId + "/report"), "{}")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("ALREADY_REPORTED"));
        send(get("/api/providers/me/uploads/" + templateId + "/check"), null)
                .andExpect(jsonPath("$.check.errors[0].reported").value(true))
                // Laporan tidak membuat template lolos otomatis.
                .andExpect(jsonPath("$.status").value("check_failed"));
        assertThat(jdbc.queryForObject("SELECT rule_code || '|' || location FROM check_reports WHERE issue_id = ?",
                String.class, UUID.fromString(issueId))).isEqualTo("CASE_MISMATCH|index.html baris 12");

        // Upload file perbaikan: template yang sama, versi pengecekan naik, notifikasi gagal dianggap beres.
        uploadAndWait("toko-kue.zip", FIXTURES.resolve("_DASAR/bersih.zip"), templateId);
        send(get("/api/providers/me/uploads/" + templateId + "/check"), null)
                .andExpect(jsonPath("$.status").value("draft"))
                .andExpect(jsonPath("$.check.version").value(2))
                .andExpect(jsonPath("$.check.errors", hasSize(0)));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM templates WHERE provider_id = ?", Integer.class, providerId))
                .isEqualTo(1);
        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM notifications WHERE template_id = ? AND type = 'TEMPLATE_CHECK_FAILED'
                   AND resolved_at IS NULL""", Integer.class, UUID.fromString(templateId))).isZero();
    }

    @Test
    void warningsAreReportedButTemplateStillPasses() throws Exception {
        String templateId = uploadAndWait("tanpa-viewport.zip", FIXTURES.resolve("NO_VIEWPORT/gagal.zip"), null);
        send(get("/api/providers/me/uploads/" + templateId + "/check"), null)
                .andExpect(jsonPath("$.status").value("draft"))
                .andExpect(jsonPath("$.check.errors", hasSize(0)))
                .andExpect(jsonPath("$.check.warnings[*].code", hasItem("NO_VIEWPORT")));
        String issueId = JsonPath.read(send(get("/api/providers/me/uploads/" + templateId + "/check"), null)
                .andReturn().getResponse().getContentAsString(), "$.check.warnings[0].id");
        send(post("/api/providers/me/uploads/issues/" + issueId + "/report"), "{}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void sessionRejectsRarTooLargeAndFullDraftQuota() throws Exception {
        send(post("/api/providers/me/uploads/sessions"), "{\"fileName\":\"toko.rar\",\"totalSize\":100}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("RAR_NOT_SUPPORTED"));
        send(post("/api/providers/me/uploads/sessions"), "{\"fileName\":\"toko.7z\",\"totalSize\":100}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("FILE_NOT_ZIP"));
        send(post("/api/providers/me/uploads/sessions"), "{\"fileName\":\"toko.zip\",\"totalSize\":%d}"
                .formatted(21 * 1024 * 1024)).andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.code").value("UPLOAD_TOO_LARGE"));

        for (int i = 0; i < 5; i++) {
            jdbc.update("INSERT INTO templates (provider_id, name, status) VALUES (?, ?, 'draft')", providerId, "Draft " + i);
        }
        send(get("/api/providers/me/uploads"), null)
                .andExpect(jsonPath("$.draftCount").value(5)).andExpect(jsonPath("$.canStartNew").value(false));
        send(post("/api/providers/me/uploads/sessions"), "{\"fileName\":\"toko.zip\",\"totalSize\":100}")
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("DRAFT_LIMIT_REACHED"));
    }

    @Test
    void draftCanBeDeletedButPublishedTemplateCannot() throws Exception {
        String templateId = uploadAndWait("toko-kue.zip", FIXTURES.resolve("_DASAR/bersih.zip"), null);
        send(delete("/api/providers/me/uploads/" + templateId), null).andExpect(status().isNoContent());
        assertThat(Files.exists(Path.of("target/test-uploads/templates", templateId))).isFalse();
        send(get("/api/providers/me/uploads/" + templateId + "/check"), null).andExpect(status().isNotFound());

        UUID published = jdbc.queryForObject("""
                INSERT INTO templates (provider_id, name, category, status) VALUES (?, 'Tayang', 'umkm', 'published')
                RETURNING id""", UUID.class, providerId);
        send(delete("/api/providers/me/uploads/" + published), null)
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("TEMPLATE_NOT_EDITABLE"));
    }

    @Test
    void untouchedDraftsAreRemindedThenDeleted() {
        Instant now = Instant.now();
        UUID reminded = draftUpdatedAt("Hampir", now.minus(Duration.ofDays(26)));
        UUID deleted = draftUpdatedAt("Lama", now.minus(Duration.ofDays(31)));
        UUID fresh = draftUpdatedAt("Baru", now.minus(Duration.ofDays(2)));

        maintenance.expireDrafts();
        maintenance.expireDrafts(); // pemberitahuan hanya dikirim sekali

        assertThat(jdbc.queryForObject("SELECT count(*) FROM notifications WHERE template_id = ? AND type = 'DRAFT_EXPIRING'",
                Integer.class, reminded)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM templates WHERE id = ?", Integer.class, deleted)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM notifications WHERE user_id = ? AND type = 'DRAFT_DELETED'",
                Integer.class, providerId)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM notifications WHERE template_id = ?", Integer.class, fresh))
                .isZero();
    }

    @Test
    void nonProviderCannotUploadAndHelpArticlesArePublic() throws Exception {
        String creatorToken = register();
        mockMvc.perform(get("/api/providers/me/uploads").header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("PROVIDER_REQUIRED"));

        mockMvc.perform(get("/api/help/articles/case_mismatch")).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CASE_MISMATCH"))
                .andExpect(jsonPath("$.howToFix").isNotEmpty());
        mockMvc.perform(get("/api/help/articles/DOCUMENT_WRITE")).andExpect(status().isNotFound());
    }

    @Test
    void draftInfoThumbnailStepAndDeviceWarnings() throws Exception {
        String templateId = uploadAndWait("toko-kue.zip", FIXTURES.resolve("_DASAR/bersih.zip"), null);
        String base = "/api/providers/me/uploads/" + templateId;

        send(get(base), null).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Toko Kue"))
                .andExpect(jsonPath("$.category").doesNotExist())
                .andExpect(jsonPath("$.techInfo.responsive").value(true));

        // Simpan otomatis per isian: field yang tidak dikirim tidak berubah; kata kunci dirapikan.
        send(patch(base + "/info"), "{\"category\":\"umkm\",\"keywords\":[\" Kue \",\"kue\",\"Kuliner\",\"\"]}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Toko Kue"))
                .andExpect(jsonPath("$.category").value("umkm"))
                .andExpect(jsonPath("$.keywords", contains("kue", "kuliner")));
        send(patch(base + "/info"), "{\"description\":\"Landing page untuk toko kue rumahan.\"}")
                .andExpect(jsonPath("$.description").value("Landing page untuk toko kue rumahan."))
                .andExpect(jsonPath("$.category").value("umkm"));
        send(patch(base + "/info"), "{\"category\":\"toko\"}").andExpect(status().isBadRequest());
        send(patch(base + "/info"), "{\"keywords\":[\"kata-kunci-yang-terlalu-panjang\"]}").andExpect(status().isBadRequest());

        // Nama sama dengan template lain milik provider sendiri = Peringatan.
        jdbc.update("INSERT INTO templates (provider_id, name, category, status) VALUES (?, 'Toko Roti', 'umkm', 'published')", providerId);
        send(patch(base + "/info"), "{\"name\":\"toko roti\"}").andExpect(jsonPath("$.nameDuplicate").value(true));

        send(patch(base + "/step"), "{\"step\":4}").andExpect(jsonPath("$.wizardStep").value(4));
        send(patch(base + "/step"), "{\"step\":9}").andExpect(status().isBadRequest());

        // Thumbnail: gambar sungguhan (dikenali dari isinya), maks 1 MB; draft hanya bisa dilihat pemiliknya.
        byte[] png = java.util.Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");
        String thumb = mockMvc.perform(put(base + "/thumbnail").param("source", "auto").param("view", "mobile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token).contentType(MediaType.IMAGE_PNG).content(png))
                .andExpect(status().isOk()).andExpect(jsonPath("$.thumbnailSource").value("auto"))
                .andReturn().getResponse().getContentAsString();
        String url = JsonPath.read(thumb, "$.thumbnailUrl");
        assertThat(url).startsWith("/api/templates/" + templateId + "/thumbnail?v=");
        send(get("/api/templates/" + templateId + "/thumbnail"), null).andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentType()).isEqualTo("image/png"));
        mockMvc.perform(get("/api/templates/" + templateId + "/thumbnail")).andExpect(status().isNotFound());
        mockMvc.perform(put(base + "/thumbnail").param("source", "auto").param("view", "mobile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token).contentType(MediaType.IMAGE_PNG)
                        .content("bukan gambar sama sekali".getBytes()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("IMAGE_INVALID"));

        // ZIP bisa diunduh lagi untuk ditampilkan di HP.
        send(get(base + "/source"), null).andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentAsByteArray())
                        .isEqualTo(Files.readAllBytes(FIXTURES.resolve("_DASAR/bersih.zip"))));

        // Peringatan tahap C dari WebView: menggantikan hasil HP sebelumnya, bukan menumpuk.
        String warning = "{\"warnings\":[{\"code\":\"JS_RUNTIME_ERROR\",\"message\":\"main.js baris 3: slider tidak ditemukan\","
                + "\"file\":\"js/main.js\",\"line\":3}]}";
        send(put(base + "/device-warnings"), warning).andExpect(status().isOk()).andExpect(jsonPath("$.warningCount").value(1));
        send(put(base + "/device-warnings"), warning).andExpect(jsonPath("$.warningCount").value(1));
        send(get(base + "/check"), null).andExpect(jsonPath("$.check.warnings", hasSize(1)))
                .andExpect(jsonPath("$.check.warnings[0].code").value("JS_RUNTIME_ERROR"));
        send(put(base + "/device-warnings"), "{\"warnings\":[{\"code\":\"BASE_HREF\",\"message\":\"x\"}]}")
                .andExpect(status().isBadRequest());

        send(get("/api/providers/me/uploads/settings"), null)
                .andExpect(jsonPath("$.maxZipBytes").value(20971520))
                .andExpect(jsonPath("$.allowedHosts", hasItem("cdn.jsdelivr.net")))
                .andExpect(jsonPath("$.allowedHosts", hasItem("www.youtube.com")));
    }

    @Test
    void workPackageIsNumberedAndMarkingIsValidatedOnSave() throws Exception {
        String templateId = uploadAndWait("toko-kue.zip", FIXTURES.resolve("_DASAR/bersih.zip"), null);
        String base = "/api/providers/me/uploads/" + templateId;

        byte[] work = send(get(base + "/work-package"), null).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        String index = null;
        try (java.util.zip.ZipInputStream zip = new java.util.zip.ZipInputStream(new java.io.ByteArrayInputStream(work))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().equals("index.html")) {
                    index = new String(zip.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                }
            }
        }
        assertThat(index).contains("data-tpl-id=\"1\"").contains("<h1 data-tpl-id=");

        send(get(base + "/marking"), null).andExpect(status().isNoContent());
        String valid = marking("judul_utama", "Judul utama", 14);
        send(put(base + "/marking"), valid).andExpect(status().isOk());
        send(get(base + "/marking"), null).andExpect(jsonPath("$.fields[0].key").value("judul_utama"));
        send(get("/api/providers/me/uploads"), null).andExpect(jsonPath("$.drafts[0].fieldCount").value(1));

        // Elemen yang tidak ada di HTML asli (dibuat JavaScript), label kosong, dan kunci tidak valid ditolak.
        send(put(base + "/marking"), marking("judul_utama", "Judul utama", 9999))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        send(put(base + "/marking"), marking("judul_utama", " ", 14)).andExpect(status().isBadRequest());
        send(put(base + "/marking"), marking("Judul Utama", "Judul", 14)).andExpect(status().isBadRequest());
    }

    @Test
    void submitPublishesTemplateToGalleryWithMarkingAttributes() throws Exception {
        String templateId = uploadAndWait("toko-kue.zip", FIXTURES.resolve("_DASAR/bersih.zip"), null);
        String base = "/api/providers/me/uploads/" + templateId;

        // Belum siap: info belum lengkap, isian kurang dari 3, hak pakai belum dicentang.
        send(post(base + "/submit"), "{\"agreedAssetRights\":true}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Pilih kategori template."));
        send(patch(base + "/info"), """
                {"name":"Toko Kue Bu Ani","category":"umkm","description":"Landing page toko kue rumahan dengan katalog.",
                 "keywords":["kue","kuliner"]}""").andExpect(status().isOk());
        send(put(base + "/marking"), marking("judul_utama", "Judul utama", 14)).andExpect(status().isOk());
        send(post(base + "/submit"), "{\"agreedAssetRights\":true}")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Tandai minimal 3 isian sebelum mengirim."));
        send(put(base + "/marking"), threeFields()).andExpect(status().isOk());
        send(post(base + "/submit"), "{\"agreedAssetRights\":false}").andExpect(status().isBadRequest());

        send(post(base + "/submit"), "{\"agreedAssetRights\":true}").andExpect(status().isAccepted());
        String result = waitForCheck(templateId);
        assertThat((String) JsonPath.read(result, "$.status")).isEqualTo("published");

        // Paket berisi atribut penandaan, tanpa nomor data-tpl-id.
        String index = null;
        try (java.util.zip.ZipInputStream zip = new java.util.zip.ZipInputStream(
                Files.newInputStream(Path.of("target/test-uploads/templates", templateId, "package.zip")))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().equals("index.html")) {
                    index = new String(zip.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                }
            }
        }
        assertThat(index).contains("data-key=\"judul_utama\"").contains("data-section=\"Hero\"")
                .contains("data-edit=\"image\"").doesNotContain("data-tpl-id");

        // Tayang di galeri dan bisa dicari lewat kata kunci; kartu Upload tidak lagi menampilkannya sebagai draft.
        mockMvc.perform(get("/api/templates").param("q", "kuliner")).andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].name", hasItem("Toko Kue Bu Ani")));
        send(get("/api/providers/me/uploads"), null).andExpect(jsonPath("$.drafts", hasSize(0)));
        send(get("/api/notifications"), null).andExpect(jsonPath("$[*].type", hasItem("TEMPLATE_PUBLISHED")));
    }

    /** Tiga isian di index.html bersih: h1 (14), p (15), img (16). */
    private static String threeFields() {
        return """
                {"pages":[{"file":"index.html","name":"Beranda"},{"file":"tentang.html","name":"Tentang"}],
                 "sections":[{"id":"s1","page":"index.html","name":"Hero","tplId":13}],
                 "fields":[
                   {"key":"judul_utama","label":"Judul utama","type":"text","required":true,"order":1,"sectionId":"s1",
                    "elements":[{"page":"index.html","tplId":14,"visibleIn":["mobile","desktop"]}]},
                   {"key":"deskripsi","label":"Deskripsi","type":"paragraph","required":false,"order":2,"sectionId":"s1",
                    "elements":[{"page":"index.html","tplId":15,"visibleIn":["mobile","desktop"]}]},
                   {"key":"foto_hero","label":"Foto hero","type":"image","aspectRatio":"1:1","required":false,"order":3,
                    "sectionId":"s1","elements":[{"page":"index.html","tplId":16,"visibleIn":["mobile","desktop"]}]}],
                 "theme":[]}""";
    }

    private static String marking(String key, String label, int tplId) {
        return """
                {"pages":[{"file":"index.html","name":"Beranda"},{"file":"tentang.html","name":"Tentang"}],
                 "sections":[{"id":"s1","page":"index.html","name":"Hero","tplId":13}],
                 "fields":[{"key":"%s","label":"%s","type":"text","maxLength":30,"required":true,"order":1,
                            "styles":[{"prop":"color"},{"prop":"font-size","min":24,"max":40,"unit":"px"}],
                            "sectionId":"s1","elements":[{"page":"index.html","tplId":%d,"visibleIn":["mobile","desktop"]}]}],
                 "theme":[{"var":"--primary","label":"Warna utama","type":"color"}]}""".formatted(key, label, tplId);
    }

    // ---------- helper ----------

    private String uploadAndWait(String fileName, Path zipPath, String fixTemplateId) throws Exception {
        byte[] zip = Files.readAllBytes(zipPath);
        String sessionId = startSession(fileName, zip.length, fixTemplateId);
        for (int offset = 0; offset < zip.length; offset += CHUNK) {
            sendChunk(sessionId, offset, Arrays.copyOfRange(zip, offset, Math.min(zip.length, offset + CHUNK)))
                    .andExpect(status().isOk());
        }
        String started = send(post("/api/providers/me/uploads/sessions/" + sessionId + "/complete"), null)
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String templateId = JsonPath.read(started, "$.templateId");
        waitForCheck(templateId);
        return templateId;
    }

    private String startSession(String fileName, long size, String templateId) throws Exception {
        String body = "{\"fileName\":\"%s\",\"totalSize\":%d%s}".formatted(fileName, size,
                templateId == null ? "" : ",\"templateId\":\"" + templateId + "\"");
        String created = send(post("/api/providers/me/uploads/sessions"), body)
                .andExpect(status().isCreated()).andExpect(jsonPath("$.chunkSize").value(CHUNK))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(created, "$.id");
    }

    private ResultActions sendChunk(String sessionId, long offset, byte[] bytes) throws Exception {
        return mockMvc.perform(put("/api/providers/me/uploads/sessions/" + sessionId).param("offset", String.valueOf(offset))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_OCTET_STREAM).content(bytes));
    }

    /** Pengecekan berjalan di thread lain; tunggu sampai status bukan "checking" lagi (maks 15 detik). */
    private String waitForCheck(String templateId) throws Exception {
        long deadline = System.currentTimeMillis() + 15_000;
        while (true) {
            String body = send(get("/api/providers/me/uploads/" + templateId + "/check"), null)
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            if (!"checking".equals(JsonPath.read(body, "$.status"))) {
                return body;
            }
            assertThat(System.currentTimeMillis()).as("pengecekan tidak selesai dalam 15 detik").isLessThan(deadline);
            Thread.sleep(100);
        }
    }

    private UUID draftUpdatedAt(String name, Instant updatedAt) {
        return jdbc.queryForObject("""
                INSERT INTO templates (provider_id, name, status, updated_at) VALUES (?, ?, 'draft', ?) RETURNING id""",
                UUID.class, providerId, name, java.sql.Timestamp.from(updatedAt));
    }

    private String register() throws Exception {
        String body = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Aris\",\"email\":\"upload-%s@mail.com\",\"password\":\"rahasia123\"}"
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
}
