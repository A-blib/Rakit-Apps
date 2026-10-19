package com.aris.templateapp.site;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SitePublishIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FormSubmissionRepository formSubmissionRepository;

    @Autowired
    private com.aris.templateapp.template.TemplateRepository templateRepository;

    @Autowired
    private com.aris.templateapp.provider.ProviderProfileRepository providerProfileRepository;

    @Autowired
    private com.aris.templateapp.upload.UploadStorage uploadStorage;

    private String creatorToken;
    private String creatorEmail;
    private UUID orgId;
    private UUID projectId;
    private UUID templateId;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        creatorEmail = "ketua-" + suffix + "@orgspace.test";
        String regRes = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Ketua OSIS\",\"email\":\"ketua-" + suffix + "@orgspace.test\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        creatorToken = JsonPath.read(regRes, "$.accessToken");
        UUID userId = UUID.fromString(JsonPath.read(regRes, "$.user.id"));

        providerProfileRepository.save(new com.aris.templateapp.provider.ProviderProfile(
                userId, "Studio Organisasi", java.time.Instant.now()));

        String orgRes = mockMvc.perform(post("/api/organizations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"organization\",\"name\":\"OSIS SMAN 1 Bandung\",\"category\":\"osis_mpk\",\"level\":\"sma\",\"institutionName\":\"SMA Negeri 1 Bandung\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        orgId = UUID.fromString(JsonPath.read(orgRes, "$.id"));

        mockMvc.perform(post("/api/users/me/onboarding/creator")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Ketua OSIS\"}"))
                .andExpect(status().isOk());

        // Buat template yang tayang beserta package.zip
        byte[] zipBytes = createTemplateZip();
        com.aris.templateapp.template.Template template = new com.aris.templateapp.template.Template(
                userId, "Template Organisasi Dinamis", com.aris.templateapp.organization.OrgCategory.OSIS_MPK);
        template.setStatus(com.aris.templateapp.template.TemplateStatus.PUBLISHED);
        template.setPackageVersion(1);
        template.setPackageSize((long) zipBytes.length);
        template = templateRepository.save(template);
        templateId = template.getId();
        uploadStorage.writePackage(template.getId(), zipBytes);

        // Buat project website
        String projectJson = String.format("""
                {"sourceTemplateId":"%s","templateVersion":1,"name":"Website Resmi OSIS","deviceId":"device-test-1"}""", template.getId());
        String projectRes = mockMvc.perform(post("/api/organizations/" + orgId + "/projects")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectJson))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        projectId = UUID.fromString(JsonPath.read(projectRes, "$.website.id"));
    }

    @Test
    void publishWebsiteAndAccessLocallyWithDynamicModules() throws Exception {
        // 1. Terbitkan website dengan subdomain osis-smansa
        String publishRes = mockMvc.perform(post("/api/projects/" + projectId + "/publish")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slug\":\"osis-smansa\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("succeeded"))
                .andExpect(jsonPath("$.releaseVersion").value(1))
                .andReturn().getResponse().getContentAsString();

        String siteId = JsonPath.read(publishRes, "$.siteId");
        assertThat(siteId).isNotNull();

        // 2. Akses redirect root website publik (/sites/osis-smansa/)
        mockMvc.perform(get("/sites/osis-smansa/"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/sites/osis-smansa/index.html"));

        // 3. Akses halaman index.html website
        String html = mockMvc.perform(get("/sites/osis-smansa/index.html"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Modul dinamis tidak diisi saat build: atribut data-ws-* tetap ada agar OrgSpace-Hosting mengisinya
        // dengan data terbaru, dan tidak ada berita contoh yang ikut tayang.
        assertThat(html)
                .contains("OSIS SMAN 1 Bandung")
                .contains("Formulir Pendaftaran OSIS SMAN 1 Bandung")
                .contains("/api/public/sites/osis-smansa/forms/pendaftaran")
                .contains("data-ws-list=\"berita\"")
                .contains("data-ws-item")
                .contains("data-ws-form")
                .doesNotContain("Pelantikan Pengurus");

        // 4. Pengunjung mengirim formulir pendaftaran secara publik
        mockMvc.perform(post("/api/public/sites/osis-smansa/forms/pendaftaran")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nama":"Ahmad Fauzi",
                                 "kelas":"X MIPA 2",
                                 "whatsapp":"081298765432",
                                 "pesan":"Ingin bergabung dengan divisi jurnalistik dan media."}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. Verifikasi kiriman formulir tersimpan di database
        List<FormSubmission> submissions = formSubmissionRepository.findBySiteIdOrderByCreatedAtDesc(UUID.fromString(siteId));
        assertThat(submissions).hasSize(1);
        assertThat(submissions.get(0).getFormName()).isEqualTo("pendaftaran");
        assertThat(submissions.get(0).getData()).containsEntry("nama", "Ahmad Fauzi");
    }

    @Test
    void portalEditsReordersAndUploadsPhotoForCustomModule() throws Exception {
        mockMvc.perform(post("/api/projects/" + projectId + "/publish")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slug\":\"osis-portal-modul\"}"))
                .andExpect(status().isAccepted());
        String login = mockMvc.perform(post("/api/public/sites/osis-portal-modul/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + creatorEmail + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String portal = "Bearer " + JsonPath.read(login, "$.token");
        String base = "/api/public/sites/osis-portal-modul/admin/collections/prestasi";

        String first = add(base, portal, "{\"judul\":\"Juara 1 Debat\"}");
        String second = add(base, portal, "{\"judul\":\"Juara 2 Futsal\"}");

        // Token app biasa (bukan token portal) ditolak, walau PUT kini tidak dijaga filter Security.
        mockMvc.perform(put(base + "/" + first).header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"judul\":\"x\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put(base + "/" + first).header(HttpHeaders.AUTHORIZATION, portal)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"judul\":\"Juara 1 Debat Nasional\",\"tahun\":\"2026\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.judul").value("Juara 1 Debat Nasional"));

        mockMvc.perform(put(base + "/order").header(HttpHeaders.AUTHORIZATION, portal)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ids\":[\"" + first + "\",\"" + second + "\"]}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/public/sites/osis-portal-modul/collections/prestasi"))
                .andExpect(jsonPath("$[0].id").value(first))
                .andExpect(jsonPath("$[0].tahun").value("2026"))
                .andExpect(jsonPath("$[1].id").value(second));
        // Daftar yang tidak lengkap (item lain baru ditambah pengurus lain) ditolak agar urutan tidak kacau.
        mockMvc.perform(put(base + "/order").header(HttpHeaders.AUTHORIZATION, portal)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ids\":[\"" + second + "\"]}"))
                .andExpect(status().isBadRequest());

        byte[] png = java.util.Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");
        String media = mockMvc.perform(multipart("/api/public/sites/osis-portal-modul/admin/media")
                        .file(new org.springframework.mock.web.MockMultipartFile("file", "foto.png", "image/png", png))
                        .header(HttpHeaders.AUTHORIZATION, portal))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String mediaId = JsonPath.read(media, "$.id");
        assertThat((String) JsonPath.read(media, "$.url")).isEqualTo("/osis-portal-modul/_media/" + mediaId);
        mockMvc.perform(get("/api/public/sites/osis-portal-modul/media/" + mediaId))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png"));
        mockMvc.perform(get("/api/public/sites/osis-portal-modul/media/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
        // Upload foto tanpa token portal ditolak.
        mockMvc.perform(multipart("/api/public/sites/osis-portal-modul/admin/media")
                        .file(new org.springframework.mock.web.MockMultipartFile("file", "foto.png", "image/png", png)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void directPublishUploadsEditorPhotosAndExposesStats() throws Exception {
        byte[] png = java.util.Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");
        String request = """
                {"name":"Web Foto OSIS","sourceTemplateId":"%s","templateVersion":1,"slug":"osis-foto","organizationId":"%s",
                 "content":{"fields":{"foto_hero":{"image":"images/foto_hero-1.png"}}},"missingCount":0}"""
                .formatted(templateId, orgId);
        mockMvc.perform(multipart("/api/projects/publish-direct")
                        .file(new org.springframework.mock.web.MockMultipartFile("request", "", "application/json",
                                request.getBytes(StandardCharsets.UTF_8)))
                        .file(new org.springframework.mock.web.MockMultipartFile("images", "images/foto_hero-1.png", "image/png", png))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isAccepted());

        // Path lokal HP diganti foto yang tersimpan di server, disajikan di alamat website sendiri.
        String html = mockMvc.perform(get("/sites/osis-foto/index.html"))
                .andReturn().getResponse().getContentAsString();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("src=\"/osis-foto/_media/([0-9a-f-]{36})\"").matcher(html);
        assertThat(m.find()).as(html).isTrue();
        mockMvc.perform(get("/api/public/sites/osis-foto/media/" + m.group(1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/public/sites/osis-foto/forms/pendaftaran").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nama\":\"Rina\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/public/sites/render/osis-foto"))
                .andExpect(jsonPath("$.stats.pengurus").value(1))
                .andExpect(jsonPath("$.stats.berita").value(0))
                .andExpect(jsonPath("$.stats['form.pendaftaran']").value(1))
                .andExpect(jsonPath("$.org.nama").value("OSIS SMAN 1 Bandung"))
                .andExpect(jsonPath("$.org.kategori").exists())
                .andExpect(jsonPath("$.org.email").doesNotExist())
                // Modul di halaman selain beranda ikut terbaca untuk form Portal Pengurus.
                .andExpect(jsonPath("$.modules.struktur.foto").value("gambar"))
                .andExpect(jsonPath("$.modules.struktur.nama").value("teks"));
    }

    private String add(String base, String portal, String json) throws Exception {
        String res = mockMvc.perform(post(base).header(HttpHeaders.AUTHORIZATION, portal)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(res, "$.id");
    }

    private static byte[] createTemplateZip() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("index.html"));
            zos.write("""
                    <!DOCTYPE html>
                    <html lang="id">
                    <head>
                      <meta charset="utf-8">
                      <meta name="viewport" content="width=device-width, initial-scale=1">
                      <title>Template Organisasi</title>
                      <link rel="stylesheet" href="css/style.css">
                    </head>
                    <body>
                      <header><h1>OSIS Website</h1><img data-edit="image" data-key="foto_hero" src="img/hero.png" alt="Hero">
                        <b data-ws-stat="pengurus">0</b> <span data-ws-org="nama">Nama</span></header>
                      <main>
                        <section data-ws-list="berita">
                          <h2>Berita Terbaru</h2>
                          <article data-ws-item>
                            <h3 data-ws-field="judul">Judul Berita</h3>
                            <span data-ws-field="tanggal">01 Jan 2026</span>
                            <p data-ws-field="ringkasan">Ringkasan berita akan tampil di sini.</p>
                            <a data-ws-link="detail" href="#">Baca Selengkapnya</a>
                          </article>
                          <p data-ws-empty>Belum ada berita yang dipublikasikan.</p>
                        </section>
                        <section id="daftar">
                          <h2>Pendaftaran Anggota</h2>
                          <div data-ws-form="pendaftaran"></div>
                        </section>
                      </main>
                      <footer><p>&copy; 2026 OSIS</p></footer>
                    </body>
                    </html>""".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("tentang.html"));
            zos.write("""
                    <!DOCTYPE html><html lang="id"><head><meta charset="utf-8"><title>Tentang</title></head><body>
                    <div data-ws-collection="struktur"><div data-ws-item><img data-ws-field="foto" src="a.png" alt="">
                    <b data-ws-field="nama">Nama</b></div></div></body></html>""".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("css/style.css"));
            zos.write("body { font-family: sans-serif; margin: 0; padding: 1rem; }".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    @Test
    void directPublishToSelectedWorkspace() throws Exception {
        // 1. Buat tempat kerja baru (misal: "Klub Jurnalistik")
        String newOrgRes = mockMvc.perform(post("/api/organizations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"organization\",\"name\":\"Klub Jurnalistik\",\"category\":\"jurnalistik_media\",\"level\":\"sma\",\"institutionName\":\"SMA Negeri 1 Bandung\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        UUID newOrgId = UUID.fromString(JsonPath.read(newOrgRes, "$.id"));

        // 2. Terbitkan website dengan menentukan organizationId ruang kerja baru yang dipilih
        String reqJson = String.format("""
                {
                    "name": "Warta Jurnalistik Smansa",
                    "organizationId": "%s",
                    "content": {
                        "fields": {
                            "judul": {"text": "Selamat Datang di Portal Jurnalistik"}
                        }
                    },
                    "missingCount": 0
                }
                """, newOrgId);

        mockMvc.perform(post("/api/projects/publish-direct")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("online"))
                .andExpect(jsonPath("$.port").value(3001))
                .andExpect(jsonPath("$.organizationId").value(newOrgId.toString()))
                .andExpect(jsonPath("$.organizationName").value("Klub Jurnalistik"))
                .andExpect(jsonPath("$.url").value("http://localhost:3001/warta-jurnalistik-smansa"));

        // 3. Verifikasi website tersimpan dan terdaftar di ruang kerja baru
        mockMvc.perform(get("/api/organizations/" + newOrgId + "/websites")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Warta Jurnalistik Smansa"))
                .andExpect(jsonPath("$.items[0].status").value("online"))
                .andExpect(jsonPath("$.counts.online").value(1));

        // 4. Pastikan website TIDAK terdaftar di ruang kerja lain (orgId awal)
        mockMvc.perform(get("/api/organizations/" + orgId + "/websites")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.name == 'Warta Jurnalistik Smansa')]").isEmpty());

        // 5. Akses root web publiknya
        mockMvc.perform(get("/sites/warta-jurnalistik-smansa/"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/sites/warta-jurnalistik-smansa/index.html"));
    }

    @Test
    void directPublishFallbackToActiveWorkspace() throws Exception {
        // Request tanpa organizationId eksplisit harus otomatis masuk ke ruang kerja aktif terakhir (orgId di setUp)
        String reqJson = """
                {
                    "name": "Web Organisasi Saya",
                    "content": {
                        "fields": {
                            "judul": {"text": "Selamat Datang di OSIS"}
                        }
                    },
                    "missingCount": 0
                }
                """;

        mockMvc.perform(post("/api/projects/publish-direct")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("online"))
                .andExpect(jsonPath("$.port").value(3001))
                .andExpect(jsonPath("$.organizationId").value(orgId.toString()))
                .andExpect(jsonPath("$.organizationName").value("OSIS SMAN 1 Bandung"))
                .andExpect(jsonPath("$.url").value("http://localhost:3001/web-organisasi-saya"));

        mockMvc.perform(get("/sites/web-organisasi-saya/"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/sites/web-organisasi-saya/index.html"));
    }

    @Test
    void slugIsSuggestedCheckedAndLockedAfterFirstPublish() throws Exception {
        String auth = "Bearer " + creatorToken;
        // Saran dari nama project
        mockMvc.perform(get("/api/projects/publish-direct/slug").header(HttpHeaders.AUTHORIZATION, auth)
                        .param("name", "Profil OSIS Keren"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("profil-osis-keren"))
                .andExpect(jsonPath("$.locked").value(false))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.url").value("http://localhost:3001/profil-osis-keren"));

        // Nama pilihan user dipakai saat terbit pertama
        mockMvc.perform(post("/api/projects/publish-direct").header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Profil OSIS Keren\",\"slug\":\"osis-keren-kita\",\"content\":{},\"missingCount\":0}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.slug").value("osis-keren-kita"))
                .andExpect(jsonPath("$.url").value("http://localhost:3001/osis-keren-kita"));

        // Setelah terbit, slug terkunci; terbit ulang dengan slug lain tetap memakai slug lama
        mockMvc.perform(get("/api/projects/publish-direct/slug").header(HttpHeaders.AUTHORIZATION, auth)
                        .param("name", "Profil OSIS Keren").param("slug", "nama-lain"))
                .andExpect(jsonPath("$.locked").value(true))
                .andExpect(jsonPath("$.slug").value("osis-keren-kita"));
        mockMvc.perform(post("/api/projects/publish-direct").header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Profil OSIS Keren\",\"slug\":\"nama-lain\",\"content\":{},\"missingCount\":0}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.slug").value("osis-keren-kita"))
                .andExpect(jsonPath("$.version").value(2));

        // Project lain: slug yang sudah dipakai, dicadangkan, atau formatnya salah ditolak dengan alasan
        for (String bad : List.of("osis-keren-kita", "api", "ab", "Huruf Besar!")) {
            mockMvc.perform(get("/api/projects/publish-direct/slug").header(HttpHeaders.AUTHORIZATION, auth)
                            .param("name", "Website Lain").param("slug", bad))
                    .andExpect(jsonPath("$.available").value(false))
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }
        mockMvc.perform(post("/api/projects/publish-direct").header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Website Lain\",\"slug\":\"osis-keren-kita\",\"content\":{},\"missingCount\":0}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SLUG_TAKEN"));

        // Website asli tidak memuat berita/prestasi contoh
        mockMvc.perform(get("/api/public/sites/render/osis-keren-kita"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.news").isEmpty())
                .andExpect(jsonPath("$.collections.prestasi").doesNotExist());
    }

    /**
     * Bug Aris (19 Oktober 2026): website yang diterbitkan dari ruang kerja BEM malah masuk ke ruang kerja lain.
     * Website yang sudah ada harus selalu terbit di ruang kerjanya sendiri, walau ruang kerja aktif di HP berbeda
     * dan walau namanya diganti di editor.
     */
    @Test
    void existingWebsiteAlwaysPublishesInItsOwnWorkspace() throws Exception {
        String auth = "Bearer " + creatorToken;
        String bemRes = mockMvc.perform(post("/api/organizations").header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"organization\",\"name\":\"BEM Kampus\",\"category\":\"osis_mpk\",\"level\":\"sma\",\"institutionName\":\"SMA Negeri 1 Bandung\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String bemId = JsonPath.read(bemRes, "$.id");

        String first = mockMvc.perform(post("/api/projects/publish-direct").header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Web BEM\",\"organizationId\":\"" + bemId + "\",\"content\":{},\"missingCount\":0}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.organizationId").value(bemId))
                .andReturn().getResponse().getContentAsString();
        String projectId = JsonPath.read(first, "$.projectId");
        String slug = JsonPath.read(first, "$.slug");

        // Ruang kerja aktif pindah ke organisasi lain, lalu website BEM diedit (nama diganti) dan diterbitkan ulang.
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/users/me/last-organization")
                        .header(HttpHeaders.AUTHORIZATION, auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationId\":\"" + orgId + "\"}"))
                .andExpect(status().is2xxSuccessful());
        mockMvc.perform(post("/api/projects/publish-direct").header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Web BEM Baru\",\"projectId\":\"" + projectId + "\",\"organizationId\":\"" + orgId
                                + "\",\"content\":{},\"missingCount\":0}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.organizationId").value(bemId))
                .andExpect(jsonPath("$.projectId").value(projectId))
                .andExpect(jsonPath("$.slug").value(slug))
                .andExpect(jsonPath("$.version").value(2));

        mockMvc.perform(get("/api/organizations/" + orgId + "/websites").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(jsonPath("$.items[?(@.name =~ /Web BEM.*/)]").isEmpty());
        mockMvc.perform(get("/api/organizations/" + bemId + "/websites").header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(jsonPath("$.counts.online").value(1));

        // Nama website tetap terkunci walau nama project diganti
        mockMvc.perform(get("/api/projects/publish-direct/slug").header(HttpHeaders.AUTHORIZATION, auth)
                        .param("name", "Web BEM Baru").param("projectId", projectId))
                .andExpect(jsonPath("$.locked").value(true))
                .andExpect(jsonPath("$.slug").value(slug));

        // Orang luar tidak bisa menerbitkan website BEM lewat projectId
        String outsider = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Orang Luar\",\"email\":\"luar-" + UUID.randomUUID().toString().substring(0, 8)
                                + "@orgspace.test\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        mockMvc.perform(post("/api/projects/publish-direct")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + JsonPath.read(outsider, "$.accessToken"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Web BEM\",\"projectId\":\"" + projectId + "\",\"content\":{},\"missingCount\":0}"))
                .andExpect(status().is4xxClientError());
    }
}
