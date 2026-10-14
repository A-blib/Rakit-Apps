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
    private UUID orgId;
    private UUID projectId;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
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

        // Verifikasi konten organisasi dan modul dinamis hadir di HTML
        assertThat(html)
                .contains("OSIS SMAN 1 Bandung")
                .contains("Pelantikan Pengurus OSIS SMAN 1 Bandung")
                .contains("Formulir Pendaftaran OSIS SMAN 1 Bandung")
                .contains("/api/public/sites/osis-smansa/forms/pendaftaran")
                .doesNotContain("data-ws-list")
                .doesNotContain("data-ws-item")
                .doesNotContain("data-ws-form");

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
                      <header><h1>OSIS Website</h1></header>
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

            zos.putNextEntry(new ZipEntry("css/style.css"));
            zos.write("body { font-family: sans-serif; margin: 0; padding: 1rem; }".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    @Test
    void directPublishFromEditorToPersonalSpace() throws Exception {
        String reqJson = """
                {
                    "name": "Web Organisasi Saya",
                    "content": {
                        "fields": {
                            "judul": {"text": "Selamat Datang di Web Pribadi"}
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
                .andExpect(jsonPath("$.port").value(8080))
                .andExpect(jsonPath("$.organizationName").value(org.hamcrest.Matchers.containsString("Ruang Pribadi")))
                .andExpect(jsonPath("$.url").value(org.hamcrest.Matchers.containsString("/sites/web-organisasi-saya/")));

        mockMvc.perform(get("/sites/web-organisasi-saya/"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/sites/web-organisasi-saya/index.html"));
    }
}
