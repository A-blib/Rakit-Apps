package com.aris.templateapp.upload;

import com.aris.templateapp.TestcontainersConfiguration;
import com.aris.templateapp.organization.OrgCategory;
import com.aris.templateapp.upload.check.TechInfo;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Uji mandiri alur upload & publikasi template statis dan dinamis (penyempurnaanSystem_instruksi.md Bagian 4).
 * Memastikan:
 * 1. Template statis dapat di-upload, lolos cek, diisi info, ditandai, dan diterbitkan ke galeri.
 * 2. Template dinamis (dengan penanda data-ws-list, data-ws-item, data-ws-field, data-ws-form) dikenali,
 *    divalidasi kontraknya, info modul dinamis tercatat di TechInfo, dan berhasil diterbitkan ke galeri.
 * 3. Template dinamis dengan penanda rusak ditolak (DYNAMIC_MODULE_INVALID).
 * 4. Pembuat website dapat membuat project di cloud baik dari template statis maupun template dinamis.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DynamicAndStaticTemplateUploadIntegrationTest {

    private static final byte[] TINY_PNG = java.util.Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

    @Autowired
    private MockMvc mockMvc;

    private String providerToken;
    private String creatorToken;
    private UUID orgId;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        // Registrasi provider
        providerToken = registerUser("provider-" + suffix + "@orgspace.test");
        sendWithToken(post("/api/users/me/onboarding/provider"),
                "{\"creatorName\":\"Studio Sahabat\",\"agreedToTerms\":true}", providerToken)
                .andExpect(status().isOk());

        // Registrasi creator & organisasi untuk pengujian pembuatan website
        creatorToken = registerUser("organizer-" + suffix + "@orgspace.test");
        String orgRes = sendWithToken(post("/api/organizations"),
                "{\"type\":\"organization\",\"name\":\"OSIS SMA 1\",\"category\":\"osis_mpk\",\"level\":\"sma\",\"institutionName\":\"SMA Negeri 1\"}",
                creatorToken).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        orgId = UUID.fromString(JsonPath.read(orgRes, "$.id"));

        sendWithToken(post("/api/users/me/onboarding/creator"),
                "{\"displayName\":\"Budi Santoso\"}", creatorToken)
                .andExpect(status().isOk());
    }

    @Test
    void staticTemplateEndToEndUploadAndPublish() throws Exception {
        byte[] staticZip = createStaticZip();
        String templateId = uploadZip(staticZip, "osis-lentera.zip");

        // Verifikasi hasil cek: lolos & modul dinamis kosong
        String checkRes = waitForCheck(templateId);
        assertThat((String) JsonPath.read(checkRes, "$.status")).isEqualTo("draft");
        assertThat((String) JsonPath.read(checkRes, "$.check.status")).isEqualTo("passed");
        List<String> modules = JsonPath.read(checkRes, "$.techInfo.dynamicModules");
        assertThat(modules).isEmpty();

        String base = "/api/providers/me/uploads/" + templateId;

        // Langkah 3: Info template
        sendWithToken(patch(base + "/info"), """
                {"name":"Lentera Organisasi",
                 "category":"osis_mpk",
                 "description":"Template statis bersih untuk profil dan proker OSIS/MPK sekolah.",
                 "keywords":["osis","sekolah","proker"],
                 "contentTags":["profil","kontak","proker"]}""", providerToken)
                .andExpect(status().isOk());

        // Thumbnail
        sendThumbnail(base, TINY_PNG, providerToken);

        // Langkah 4: Tandai bagian (minimal 3 isian)
        String markingJson = """
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
        sendWithToken(put(base + "/marking"), markingJson, providerToken).andExpect(status().isOk());

        // Langkah 6: Kirim & Terbitkan
        sendWithToken(post(base + "/submit"), "{\"agreedAssetRights\":true}", providerToken)
                .andExpect(status().isAccepted());

        String submitCheck = waitForCheck(templateId);
        assertThat((String) JsonPath.read(submitCheck, "$.status")).isEqualTo("published");

        // Verifikasi detail publik di galeri
        String detailRes = mockMvc.perform(get("/api/templates/" + templateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lentera Organisasi"))
                .andExpect(jsonPath("$.category").value("osis_mpk"))
                .andExpect(jsonPath("$.dynamicModules").isEmpty())
                .andReturn().getResponse().getContentAsString();

        // Download paket dan pastikan manifest ada
        byte[] packageZip = mockMvc.perform(get("/api/templates/" + templateId + "/package"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(hasZipEntry(packageZip, "manifest.json")).isTrue();

        // Pembuat website membuat project dari template statis ini
        String createProjectJson = String.format("""
                {"sourceTemplateId":"%s","templateVersion":1,"name":"Website OSIS Lentera","deviceId":"device-unit-test-1"}""", templateId);
        sendWithToken(post("/api/organizations/" + orgId + "/projects"), createProjectJson, creatorToken)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.website.name").value("Website OSIS Lentera"));
    }

    @Test
    void dynamicTemplateEndToEndUploadAndPublish() throws Exception {
        byte[] dynamicZip = createDynamicZip();
        String templateId = uploadZip(dynamicZip, "bem-dinamis.zip");

        // Verifikasi hasil cek: lolos & modul dinamis terdeteksi ("berita" dan "pendaftaran")
        String checkRes = waitForCheck(templateId);
        assertThat((String) JsonPath.read(checkRes, "$.status")).isEqualTo("draft");
        assertThat((String) JsonPath.read(checkRes, "$.check.status")).isEqualTo("passed");
        List<String> modules = JsonPath.read(checkRes, "$.techInfo.dynamicModules");
        assertThat(modules).containsExactlyInAnyOrder("berita", "pendaftaran");

        String base = "/api/providers/me/uploads/" + templateId;

        // Langkah 3: Info template
        sendWithToken(patch(base + "/info"), """
                {"name":"BEM Kampus Inspiratif",
                 "category":"bem_dema_dpm",
                 "description":"Template dinamis dengan modul Berita kegiatan dan Open Recruitment.",
                 "keywords":["bem","kampus","dinamis"],
                 "contentTags":["profil","berita","kontak","oprec"]}""", providerToken)
                .andExpect(status().isOk());

        // Thumbnail
        sendThumbnail(base, TINY_PNG, providerToken);

        // Langkah 4: Tandai bagian
        String markingJson = """
                {"pages":[{"file":"index.html","name":"Beranda"}],
                 "sections":[{"id":"s1","page":"index.html","name":"Hero","tplId":13}],
                 "fields":[
                   {"key":"judul_organisasi","label":"Nama BEM","type":"text","required":true,"order":1,"sectionId":"s1",
                    "elements":[{"page":"index.html","tplId":14,"visibleIn":["mobile","desktop"]}]},
                   {"key":"sambutan","label":"Kata Sambutan","type":"paragraph","required":false,"order":2,"sectionId":"s1",
                    "elements":[{"page":"index.html","tplId":15,"visibleIn":["mobile","desktop"]}]},
                   {"key":"logo_bem","label":"Logo BEM","type":"image","aspectRatio":"1:1","required":false,"order":3,
                    "sectionId":"s1","elements":[{"page":"index.html","tplId":16,"visibleIn":["mobile","desktop"]}]}],
                 "theme":[]}""";
        sendWithToken(put(base + "/marking"), markingJson, providerToken).andExpect(status().isOk());

        // Langkah 6: Kirim & Terbitkan
        sendWithToken(post(base + "/submit"), "{\"agreedAssetRights\":true}", providerToken)
                .andExpect(status().isAccepted());

        String submitCheck = waitForCheck(templateId);
        assertThat((String) JsonPath.read(submitCheck, "$.status")).isEqualTo("published");

        // Verifikasi detail publik di galeri: dynamicModules muncul
        mockMvc.perform(get("/api/templates/" + templateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("BEM Kampus Inspiratif"))
                .andExpect(jsonPath("$.category").value("bem_dema_dpm"))
                .andExpect(jsonPath("$.dynamicModules", containsInAnyOrder("berita", "pendaftaran")));

        // Download paket dan verifikasi penanda data-ws-* tetap utuh di HTML paket
        byte[] packageZip = mockMvc.perform(get("/api/templates/" + templateId + "/package"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        String indexHtml = extractZipEntry(packageZip, "index.html");
        assertThat(indexHtml).contains("data-ws-list=\"berita\"")
                .contains("data-ws-item")
                .contains("data-ws-field=\"judul\"")
                .contains("data-ws-form=\"pendaftaran\"");

        // Pembuat website membuat project dari template dinamis ini
        String createProjectJson = String.format("""
                {"sourceTemplateId":"%s","templateVersion":1,"name":"Website BEM Inspirasi","deviceId":"device-unit-test-1"}""", templateId);
        sendWithToken(post("/api/organizations/" + orgId + "/projects"), createProjectJson, creatorToken)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.website.name").value("Website BEM Inspirasi"));
    }

    @Test
    void customCollectionAndAuthTemplatePassesCheckAndPublishes() throws Exception {
        java.nio.file.Path zipPath = java.nio.file.Path.of("../template-organisasi-portal.zip");
        byte[] zipBytes = java.nio.file.Files.readAllBytes(zipPath);
        String templateId = uploadZip(zipBytes, "template-organisasi-portal.zip");

        // Verifikasi hasil cek: lolos & modul dinamis terdeteksi ("auth", "berita", "prestasi", "pendaftaran")
        String checkRes = waitForCheck(templateId);
        assertThat((String) JsonPath.read(checkRes, "$.status")).isEqualTo("draft");
        assertThat((String) JsonPath.read(checkRes, "$.check.status")).isEqualTo("passed");
        List<String> modules = JsonPath.read(checkRes, "$.techInfo.dynamicModules");
        assertThat(modules).contains("auth", "berita", "prestasi", "pendaftaran");

        String base = "/api/providers/me/uploads/" + templateId;

        // Info template
        sendWithToken(patch(base + "/info"), """
                {"name":"Organisasi Kreatif Portal",
                 "category":"lainnya",
                 "description":"Template organisasi dengan modul berita, prestasi custom, auth pengurus, dan form.",
                 "keywords":["organisasi","prestasi","portal"],
                 "contentTags":["profil","berita","oprec","kontak"]}""", providerToken)
                .andExpect(status().isOk());

        // Thumbnail
        sendThumbnail(base, TINY_PNG, providerToken);

        // Tandai bagian (minimal 3 isian)
        String markingJson = """
                {"pages":[{"file":"index.html","name":"Beranda"}],
                 "sections":[{"id":"s1","page":"index.html","name":"Hero","tplId":20}],
                 "fields":[
                   {"key":"hero_tag","label":"Tag Hero","type":"text","required":false,"order":1,"sectionId":"s1",
                    "elements":[{"page":"index.html","tplId":22,"visibleIn":["mobile","desktop"]}]},
                   {"key":"hero_title","label":"Judul Hero","type":"text","required":true,"order":2,"sectionId":"s1",
                    "elements":[{"page":"index.html","tplId":23,"visibleIn":["mobile","desktop"]}]},
                   {"key":"hero_desc","label":"Deskripsi Hero","type":"paragraph","required":false,"order":3,"sectionId":"s1",
                    "elements":[{"page":"index.html","tplId":24,"visibleIn":["mobile","desktop"]}]}],
                 "theme":[]}""";
        sendWithToken(put(base + "/marking"), markingJson, providerToken).andExpect(status().isOk());

        // Kirim & Terbitkan
        sendWithToken(post(base + "/submit"), "{\"agreedAssetRights\":true}", providerToken)
                .andExpect(status().isAccepted());

        String submitCheck = waitForCheck(templateId);
        assertThat((String) JsonPath.read(submitCheck, "$.status")).isEqualTo("published");

        // Detail publik di galeri
        mockMvc.perform(get("/api/templates/" + templateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Organisasi Kreatif Portal"))
                .andExpect(jsonPath("$.dynamicModules", hasItem("prestasi")))
                .andExpect(jsonPath("$.dynamicModules", hasItem("auth")));
    }

    @Test
    void malformedDynamicTemplateIsRejectedDuringCheck() throws Exception {
        // ZIP dengan data-ws-list tanpa data-ws-item
        byte[] brokenZip = createBrokenDynamicZip();
        String templateId = uploadZip(brokenZip, "broken-dynamic.zip");

        String checkRes = waitForCheck(templateId);
        assertThat((String) JsonPath.read(checkRes, "$.status")).isEqualTo("check_failed");
        assertThat((String) JsonPath.read(checkRes, "$.check.status")).isEqualTo("failed");
        List<String> errorCodes = JsonPath.read(checkRes, "$.check.errors[*].code");
        assertThat(errorCodes).contains("DYNAMIC_MODULE_INVALID");
    }

    // ---------- Helper Methods ----------

    private String uploadZip(byte[] zipBytes, String filename) throws Exception {
        String sessionRes = sendWithToken(post("/api/providers/me/uploads/sessions"),
                String.format("{\"fileName\":\"%s\",\"totalSize\":%d}", filename, zipBytes.length),
                providerToken).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String sessionId = JsonPath.read(sessionRes, "$.id");

        sendChunkWithToken(sessionId, 0, zipBytes, providerToken).andExpect(status().isOk());
        String completed = sendWithToken(post("/api/providers/me/uploads/sessions/" + sessionId + "/complete"),
                null, providerToken).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(completed, "$.templateId");
    }

    private void sendThumbnail(String base, byte[] png, String token) throws Exception {
        mockMvc.perform(put(base + "/thumbnail").param("source", "auto").param("view", "mobile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.IMAGE_PNG).content(png))
                .andExpect(status().isOk());
    }

    private String waitForCheck(String templateId) throws Exception {
        for (int i = 0; i < 40; i++) {
            String body = sendWithToken(get("/api/providers/me/uploads/" + templateId + "/check"), null, providerToken)
                    .andReturn().getResponse().getContentAsString();
            String stage = JsonPath.read(body, "$.check.stage");
            if ("done".equals(stage)) {
                return body;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Pengecekan tidak selesai dalam 4 detik");
    }

    private ResultActions sendWithToken(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder,
                                        String body, String token) throws Exception {
        if (body != null) {
            builder.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mockMvc.perform(builder.header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    private ResultActions sendChunkWithToken(String sessionId, long offset, byte[] bytes, String token) throws Exception {
        return mockMvc.perform(put("/api/providers/me/uploads/sessions/" + sessionId)
                .param("offset", String.valueOf(offset))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(bytes)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    private String registerUser(String email) throws Exception {
        String res = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Pengguna\",\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(res, "$.accessToken");
    }

    private static byte[] createStaticZip() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            addZipEntry(zos, "index.html", """
                    <!DOCTYPE html>
                    <html lang="id">
                    <head>
                      <meta charset="utf-8">
                      <meta name="viewport" content="width=device-width, initial-scale=1">
                      <title>OSIS SMA Lentera Bangsa</title>
                      <link rel="stylesheet" href="css/style.css">
                    </head>
                    <body>
                      <header><nav><a href="index.html">Beranda</a> <a href="tentang.html">Tentang</a></nav></header>
                      <main>
                        <section id="hero">
                          <h1>OSIS SMA Lentera Bangsa</h1>
                          <p>Wadah kreativitas, kepemimpinan, dan karya nyata siswa-siswi SMA Lentera Bangsa.</p>
                          <img src="img/hero.png" alt="Logo OSIS">
                          <a href="#kontak">Hubungi Kami</a>
                        </section>
                        <section id="kontak">
                          <h2>Kontak</h2>
                          <p>Hubungi sekretariat OSIS.</p>
                        </section>
                      </main>
                      <footer><p>OSIS Lentera Bangsa</p></footer>
                      <script src="js/main.js"></script>
                    </body>
                    </html>""");
            addZipEntry(zos, "tentang.html", """
                    <!DOCTYPE html>
                    <html lang="id">
                    <head>
                      <meta charset="utf-8">
                      <meta name="viewport" content="width=device-width, initial-scale=1">
                      <title>Tentang Kami</title>
                      <link rel="stylesheet" href="css/style.css">
                    </head>
                    <body>
                      <header><nav><a href="index.html">Beranda</a> <a href="tentang.html">Tentang</a></nav></header>
                      <main><section id="tentang"><h1>Tentang OSIS</h1><p>Visi dan misi kepengurusan.</p></section></main>
                      <script src="js/main.js"></script>
                    </body>
                    </html>""");
            addZipEntry(zos, "css/style.css", """
                    :root { --primary: #3B5BFD; --radius: 12px; }
                    body { margin: 0; font-family: sans-serif; }
                    @media (max-width: 768px) { body { font-size: 14px; } }
                    """);
            addZipEntry(zos, "js/main.js", "console.log('OSIS Static Ready');");
            addZipBinary(zos, "img/hero.png", TINY_PNG);
        }
        return baos.toByteArray();
    }

    private static byte[] createDynamicZip() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            addZipEntry(zos, "index.html", """
                    <!DOCTYPE html>
                    <html lang="id">
                    <head>
                      <meta charset="utf-8">
                      <meta name="viewport" content="width=device-width, initial-scale=1">
                      <title>BEM Kampus Inspiratif</title>
                      <link rel="stylesheet" href="css/style.css">
                    </head>
                    <body>
                      <header><nav><a href="index.html">Beranda</a></nav></header>
                      <main>
                        <section id="hero">
                          <h1>BEM Inspirasi Muda</h1>
                          <p>Bergerak bersama, berdampak nyata bagi seluruh mahasiswa.</p>
                          <img src="img/logo.png" alt="Logo BEM">
                        </section>
                        <section data-ws-list="berita" data-ws-limit="6">
                          <h2>Berita Kegiatan</h2>
                          <article data-ws-item>
                            <img data-ws-field="foto" src="img/logo.png" alt="Foto Berita">
                            <span data-ws-field="tanggal">14 Okt 2026</span>
                            <h3 data-ws-field="judul">Pelantikan Pengurus Baru</h3>
                            <p data-ws-field="ringkasan">Rangkaian acara pelantikan berlangsung tertib.</p>
                            <a data-ws-link="detail" href="index.html">Baca</a>
                          </article>
                          <p data-ws-empty>Belum ada berita kegiatan.</p>
                        </section>
                        <section id="oprec">
                          <h2>Pendaftaran Anggota</h2>
                          <div data-ws-form="pendaftaran"></div>
                        </section>
                      </main>
                      <footer><p>BEM Inspirasi</p></footer>
                      <script src="js/main.js"></script>
                    </body>
                    </html>""");
            addZipEntry(zos, "css/style.css", """
                    :root { --primary: #3B5BFD; --radius: 12px; }
                    body { margin: 0; font-family: sans-serif; }
                    @media (max-width: 768px) { body { font-size: 14px; } }
                    """);
            addZipEntry(zos, "js/main.js", "console.log('BEM Dynamic Ready');");
            addZipBinary(zos, "img/logo.png", TINY_PNG);
        }
        return baos.toByteArray();
    }

    private static byte[] createBrokenDynamicZip() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            addZipEntry(zos, "index.html", """
                    <!DOCTYPE html>
                    <html lang="id">
                    <head>
                      <meta charset="utf-8">
                      <meta name="viewport" content="width=device-width, initial-scale=1">
                      <title>Template Rusak</title>
                      <link rel="stylesheet" href="css/style.css">
                    </head>
                    <body>
                      <header><nav><a href="index.html">Beranda</a></nav></header>
                      <main>
                        <!-- Rusak: data-ws-list berita tanpa data-ws-item -->
                        <section data-ws-list="berita">
                          <h2>Berita Kegiatan</h2>
                          <p>Tidak ada kartu data-ws-item sama sekali</p>
                        </section>
                      </main>
                      <footer><p>Footer</p></footer>
                      <script src="js/main.js"></script>
                    </body>
                    </html>""");
            addZipEntry(zos, "css/style.css", "body { margin: 0; } @media (max-width: 768px) { body { font-size: 14px; } }");
            addZipEntry(zos, "js/main.js", "console.log('Broken');");
        }
        return baos.toByteArray();
    }

    private static void addZipEntry(ZipOutputStream zos, String name, String text) throws Exception {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(text.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private static void addZipBinary(ZipOutputStream zos, String name, byte[] bytes) throws Exception {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(bytes);
        zos.closeEntry();
    }

    private static boolean hasZipEntry(byte[] zipBytes, String entryName) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(entryName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String extractZipEntry(byte[] zipBytes, String entryName) throws Exception {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(entryName)) {
                    return new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }
        return null;
    }
}
