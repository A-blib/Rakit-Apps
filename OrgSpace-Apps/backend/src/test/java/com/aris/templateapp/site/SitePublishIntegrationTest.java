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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

    @Autowired
    private com.aris.templateapp.project.ProjectRepository projectRepository;

    @Autowired
    private SiteDomainRepository siteDomainRepository;

    @Autowired
    private SiteReleaseRepository siteReleaseRepository;

    @Autowired
    private SiteStorage siteStorage;

    @Autowired
    private com.aris.templateapp.project.ProjectAssetCleanup assetCleanup;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbc;


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
        // Tandaan minimal: publish langsung memvalidasi isi terhadap tandaan template.
        template.setMarking(new com.aris.templateapp.upload.marking.MarkingData(List.of(), List.of(),
                List.of(new com.aris.templateapp.upload.marking.MarkingData.Field("foto_hero", "Foto utama", "image",
                        null, null, false, 1, null, List.of(), null, List.of(), null, null)),
                List.of(new com.aris.templateapp.upload.marking.MarkingData.ThemeVar("--warna-utama", "Warna utama", "color"))));
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
    void formSubmissionsAreOnlyForOwnerAndAdminWithoutVisitorIp() throws Exception {
        mockMvc.perform(post("/api/projects/" + projectId + "/publish").header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"slug\":\"osis-data-masuk\"}"))
                .andExpect(status().isAccepted());
        mockMvc.perform(post("/api/public/sites/osis-data-masuk/forms/pendaftaran").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nama\":\"Rina\",\"whatsapp\":\"081234567890\"}"))
                .andExpect(status().isOk());

        // Editor: anggota yang hanya bertugas mengedit website.
        String editorEmail = "editor-" + UUID.randomUUID().toString().substring(0, 8) + "@orgspace.test";
        String reg = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Editor\",\"email\":\"" + editorEmail + "\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String editorToken = JsonPath.read(reg, "$.accessToken");
        jdbc.update("INSERT INTO organization_members (organization_id, user_id, role) VALUES (?, ?::uuid, 'editor')",
                orgId, JsonPath.read(reg, "$.user.id"));

        // Jalur app.
        mockMvc.perform(get("/api/projects/" + projectId + "/submissions").header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].data.nama").value("Rina"));
        mockMvc.perform(get("/api/projects/" + projectId + "/submissions").header(HttpHeaders.AUTHORIZATION, "Bearer " + editorToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ROLE_NOT_ALLOWED"));

        // Jalur Portal Pengurus.
        String submissions = "/api/public/sites/osis-data-masuk/admin/submissions";
        mockMvc.perform(get(submissions).header(HttpHeaders.AUTHORIZATION, portalToken("osis-data-masuk", creatorEmail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].data.whatsapp").value("081234567890"))
                .andExpect(jsonPath("$[0].ipAddress").doesNotExist())
                .andExpect(jsonPath("$[0].userAgent").doesNotExist());
        mockMvc.perform(get(submissions).header(HttpHeaders.AUTHORIZATION, portalToken("osis-data-masuk", editorEmail)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ROLE_NOT_ALLOWED"));
    }

    @Test
    void portalLoginIsLimitedLikeAppLogin() throws Exception {
        mockMvc.perform(post("/api/projects/" + projectId + "/publish").header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"slug\":\"osis-batas-login\"}"))
                .andExpect(status().isAccepted());
        String wrong = "{\"email\":\"" + creatorEmail + "\",\"password\":\"tebakan-salah\"}";
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/api/public/sites/osis-batas-login/auth/login").contentType(MediaType.APPLICATION_JSON).content(wrong))
                    .andExpect(status().isUnauthorized());
        }
        mockMvc.perform(post("/api/public/sites/osis-batas-login/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + creatorEmail + "\",\"password\":\"password123\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("TOO_MANY_ATTEMPTS"));
        // Hitungan sama dengan login app: menebak lewat Portal juga menahan login app untuk email itu.
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + creatorEmail + "\",\"password\":\"password123\"}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void portalLoginGivesTheSameAnswerWhetherOrNotThePasswordIsRight() throws Exception {
        mockMvc.perform(post("/api/projects/" + projectId + "/publish").header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"slug\":\"osis-tebak\"}"))
                .andExpect(status().isAccepted());
        // Akun OrgSpace yang bukan pengurus website ini.
        String outsider = "luar-" + UUID.randomUUID().toString().substring(0, 8) + "@orgspace.test";
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Luar\",\"email\":\"" + outsider + "\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated());

        String rightPassword = mockMvc.perform(post("/api/public/sites/osis-tebak/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + outsider + "\",\"password\":\"password123\"}"))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        String wrongPassword = mockMvc.perform(post("/api/public/sites/osis-tebak/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + outsider + "\",\"password\":\"salah-total\"}"))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        String unknownEmail = mockMvc.perform(post("/api/public/sites/osis-tebak/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"tidak-ada-" + UUID.randomUUID() + "@orgspace.test\",\"password\":\"password123\"}"))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();

        assertThat(rightPassword).isEqualTo(wrongPassword).isEqualTo(unknownEmail).contains("INVALID_CREDENTIALS");
        // Pengurus asli tetap bisa masuk.
        assertThat(portalToken("osis-tebak", creatorEmail)).startsWith("Bearer ");
    }

    @Test
    void portalAddsManagersOnlyThroughInvitationsTheyAccept() throws Exception {
        mockMvc.perform(post("/api/projects/" + projectId + "/publish").header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"slug\":\"osis-undang\"}"))
                .andExpect(status().isAccepted());
        String owner = portalToken("osis-undang", creatorEmail);
        String guestEmail = "tamu-" + UUID.randomUUID().toString().substring(0, 8) + "@orgspace.test";
        String guestApp = JsonPath.read(mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Tamu\",\"email\":\"" + guestEmail + "\",\"password\":\"password123\"}"))
                .andReturn().getResponse().getContentAsString(), "$.accessToken");

        // Jalur lama "masukkan langsung lewat email" sudah tidak ada.
        mockMvc.perform(post("/api/public/sites/osis-undang/admin/members").header(HttpHeaders.AUTHORIZATION, owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + guestEmail + "\"}"))
                .andExpect(status().isNotFound());

        String created = mockMvc.perform(post("/api/public/sites/osis-undang/admin/invitations").header(HttpHeaders.AUTHORIZATION, owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"editor\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("editor"))
                .andReturn().getResponse().getContentAsString();
        String code = JsonPath.read(created, "$.code");
        String invitationId = JsonPath.read(created, "$.id");

        // Belum menjadi pengurus sampai ia sendiri menerima undangan di app.
        mockMvc.perform(post("/api/public/sites/osis-undang/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + guestEmail + "\",\"password\":\"password123\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/invitations/accept").header(HttpHeaders.AUTHORIZATION, "Bearer " + guestApp)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"" + code + "\"}"))
                .andExpect(status().isOk());
        String guestPortal = portalToken("osis-undang", guestEmail);

        // Editor tidak boleh mengundang; Pemilik melihat dan mencabut undangan, lalu kodenya tidak berlaku lagi.
        mockMvc.perform(post("/api/public/sites/osis-undang/admin/invitations").header(HttpHeaders.AUTHORIZATION, guestPortal)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"editor\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ROLE_NOT_ALLOWED"));
        mockMvc.perform(get("/api/public/sites/osis-undang/admin/invitations").header(HttpHeaders.AUTHORIZATION, owner))
                .andExpect(jsonPath("$[?(@.id == '" + invitationId + "')]").isNotEmpty());
        mockMvc.perform(delete("/api/public/sites/osis-undang/admin/invitations/" + invitationId).header(HttpHeaders.AUTHORIZATION, owner))
                .andExpect(status().isOk());
        String lateApp = JsonPath.read(mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Telat\",\"email\":\"telat-" + UUID.randomUUID() + "@orgspace.test\",\"password\":\"password123\"}"))
                .andReturn().getResponse().getContentAsString(), "$.accessToken");
        mockMvc.perform(post("/api/invitations/accept").header(HttpHeaders.AUTHORIZATION, "Bearer " + lateApp)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"" + code + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVITATION_INVALID"));
    }

    @Test
    void portalNewsReturnsOnlyDisplayFieldsAndRejectsTooLongInput() throws Exception {
        mockMvc.perform(post("/api/projects/" + projectId + "/publish").header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"slug\":\"osis-berita\"}"))
                .andExpect(status().isAccepted());
        String portal = portalToken("osis-berita", creatorEmail);
        String news = "/api/public/sites/osis-berita/admin/news";

        mockMvc.perform(post(news).header(HttpHeaders.AUTHORIZATION, portal).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Pelantikan Pengurus\",\"summary\":\"Ringkas\",\"content\":\"Isi\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Pelantikan Pengurus"))
                .andExpect(jsonPath("$.publishedAt").exists())
                // Field internal entity tidak ikut terkirim.
                .andExpect(jsonPath("$.createdBy").doesNotExist())
                .andExpect(jsonPath("$.organizationId").doesNotExist());
        mockMvc.perform(get(news).header(HttpHeaders.AUTHORIZATION, portal))
                .andExpect(jsonPath("$[0].title").value("Pelantikan Pengurus"))
                .andExpect(jsonPath("$[0].createdBy").doesNotExist())
                .andExpect(jsonPath("$[0].organizationId").doesNotExist());

        // Dulu ditolak database menjadi 500.
        mockMvc.perform(post(news).header(HttpHeaders.AUTHORIZATION, portal).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + "J".repeat(201) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Judul berita maksimal 200 karakter."));
        mockMvc.perform(post(news).header(HttpHeaders.AUTHORIZATION, portal).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Ok\",\"summary\":\"" + "R".repeat(501) + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void portalSummaryCountsSubmissionsNewsAndModuleItems() throws Exception {
        mockMvc.perform(post("/api/projects/" + projectId + "/publish").header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"slug\":\"osis-ringkasan\"}"))
                .andExpect(status().isAccepted());
        String portal = portalToken("osis-ringkasan", creatorEmail);
        for (String nama : List.of("Rina", "Budi")) {
            mockMvc.perform(post("/api/public/sites/osis-ringkasan/forms/pendaftaran").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nama\":\"" + nama + "\"}"))
                    .andExpect(status().isOk());
        }
        for (String judul : List.of("Rapat Pleno", "Class Meeting")) {
            mockMvc.perform(post("/api/public/sites/osis-ringkasan/admin/news").header(HttpHeaders.AUTHORIZATION, portal)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"" + judul + "\"}"))
                    .andExpect(status().isOk());
        }
        add("/api/public/sites/osis-ringkasan/admin/collections/prestasi", portal, "{\"judul\":\"Juara 1 Debat\"}");

        mockMvc.perform(get("/api/public/sites/osis-ringkasan/admin/stats").header(HttpHeaders.AUTHORIZATION, portal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submissionsCount").value(2))
                .andExpect(jsonPath("$.newsCount").value(2))
                .andExpect(jsonPath("$.customItemsCount").value(1))
                .andExpect(jsonPath("$.status").value("online"));
    }

    @Test
    void portalMemberChangesUseTheSameRulesAndActivityLogAsTheApp() throws Exception {
        mockMvc.perform(post("/api/projects/" + projectId + "/publish").header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"slug\":\"osis-anggota\"}"))
                .andExpect(status().isAccepted());
        String owner = portalToken("osis-anggota", creatorEmail);
        String ownerId = JsonPath.read(mockMvc.perform(get("/api/public/sites/osis-anggota/auth/me").header(HttpHeaders.AUTHORIZATION, owner))
                .andReturn().getResponse().getContentAsString(), "$.user.id");

        // Dulu Portal mengizinkan ini, sehingga organisasi bisa kehilangan Pemilik terakhirnya.
        mockMvc.perform(put("/api/public/sites/osis-anggota/admin/members/" + ownerId).header(HttpHeaders.AUTHORIZATION, owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"admin\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LAST_OWNER"));

        // Anggota baru lewat undangan, lalu dijadikan Admin dari Portal: tercatat di log aktivitas seperti di app.
        String code = JsonPath.read(mockMvc.perform(post("/api/public/sites/osis-anggota/admin/invitations")
                        .header(HttpHeaders.AUTHORIZATION, owner).contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"editor\"}"))
                .andReturn().getResponse().getContentAsString(), "$.code");
        String reg = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Budi\",\"email\":\"budi-" + UUID.randomUUID() + "@orgspace.test\",\"password\":\"password123\"}"))
                .andReturn().getResponse().getContentAsString();
        String budiId = JsonPath.read(reg, "$.user.id");
        mockMvc.perform(post("/api/invitations/accept").header(HttpHeaders.AUTHORIZATION, "Bearer " + JsonPath.read(reg, "$.accessToken"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"" + code + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/public/sites/osis-anggota/admin/members/" + budiId).header(HttpHeaders.AUTHORIZATION, owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"admin\",\"positionTitle\":\"Sekretaris\"}"))
                .andExpect(status().isOk());
        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM activity_logs WHERE organization_id = ? AND action = 'member.role_changed' AND target_id = ?::uuid""",
                Long.class, orgId, budiId)).isOne();
        // Tanpa isian peran: peran tidak berubah (dulu otomatis diturunkan menjadi Editor).
        mockMvc.perform(put("/api/public/sites/osis-anggota/admin/members/" + budiId).header(HttpHeaders.AUTHORIZATION, owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"positionTitle\":\"Ketua Divisi\"}"))
                .andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT role FROM organization_members WHERE organization_id = ? AND user_id = ?::uuid",
                String.class, orgId, budiId)).isEqualTo("admin");
        // Jabatan terlalu panjang: 400, bukan 500 dari database.
        mockMvc.perform(put("/api/public/sites/osis-anggota/admin/members/" + budiId).header(HttpHeaders.AUTHORIZATION, owner)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"positionTitle\":\"" + "J".repeat(61) + "\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/public/sites/osis-anggota/admin/members/" + budiId).header(HttpHeaders.AUTHORIZATION, owner))
                .andExpect(status().isOk());
        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM activity_logs WHERE organization_id = ? AND action = 'member.removed' AND target_id = ?::uuid""",
                Long.class, orgId, budiId)).isOne();
    }

    private String portalToken(String slug, String email) throws Exception {
        String login = mockMvc.perform(post("/api/public/sites/" + slug + "/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(login, "$.token");
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

        // Foto yang dipasang di item koleksi tidak dianggap yatim oleh pembersih malam, walau sudah lewat 24 jam.
        mockMvc.perform(put(base + "/" + first).header(HttpHeaders.AUTHORIZATION, portal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"judul\":\"Juara 1 Debat Nasional\",\"foto\":\"/osis-portal-modul/_media/" + mediaId + "\"}"))
                .andExpect(status().isOk());
        jdbc.update("UPDATE project_assets SET created_at = now() - interval '2 days'");
        assetCleanup.run();
        mockMvc.perform(get("/api/public/sites/osis-portal-modul/media/" + mediaId)).andExpect(status().isOk());

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

    @Test
    void directPublishRejectsScriptInjectedThroughTheme() throws Exception {
        // jsoup tidak meng-escape isi <style>; tanpa validasi, nilai ini menjadi <script> di website tayang.
        String request = """
                {"name":"Web Jahat","sourceTemplateId":"%s","templateVersion":1,"slug":"web-jahat","organizationId":"%s",
                 "content":{"theme":{"--warna-utama":"red;}</style><script>alert(1)</script><style>"}},"missingCount":0}"""
                .formatted(templateId, orgId);
        mockMvc.perform(post("/api/projects/publish-direct").header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        // Ditolak sebelum apa pun tersimpan: website tidak pernah dibuat.
        mockMvc.perform(get("/sites/web-jahat/index.html")).andExpect(status().isNotFound());
    }

    @Test
    void directPublishWithPhotoMissingOnPhoneFallsBackToTemplateSample() throws Exception {
        String request = """
                {"name":"Web Tanpa Foto","sourceTemplateId":"%s","templateVersion":1,"slug":"osis-tanpa-foto","organizationId":"%s",
                 "content":{"fields":{"foto_hero":{"image":"images/foto_hero-hilang.webp"}},"theme":{"--warna-utama":"#112233"}},
                 "missingCount":0}""".formatted(templateId, orgId);
        mockMvc.perform(post("/api/projects/publish-direct").header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.missingImages[0]").value("foto_hero"));

        String html = mockMvc.perform(get("/sites/osis-tanpa-foto/index.html"))
                .andReturn().getResponse().getContentAsString();
        assertThat(html).contains("src=\"img/hero.png\"").contains("--warna-utama: #112233;");
    }

    @Test
    void buildSkipsUnsafeValuesAlreadyStoredInDatabase() throws Exception {
        // Isi tercemar yang tersimpan sebelum validasi dipasang di semua jalur tulis.
        com.aris.templateapp.project.Project project = projectRepository.findById(projectId).orElseThrow();
        project.setContent(java.util.Map.of(
                "fields", java.util.Map.of("foto_hero", java.util.Map.of("image", "x');background:url('https://evil.example")),
                "styles", java.util.Map.of("foto_hero", java.util.Map.of("color", "red;background:url(https://evil.example)",
                        "font-size", "16;position:fixed")),
                "theme", java.util.Map.of("--warna-utama", "red;}</style><script>alert(1)</script><style>",
                        "--x:0}</style><script>alert(2)</script><style>{--y", "#000000")));
        projectRepository.save(project);

        mockMvc.perform(post("/api/projects/" + projectId + "/publish").header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"slug\":\"osis-tercemar\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("succeeded"));

        String html = mockMvc.perform(get("/sites/osis-tercemar/index.html"))
                .andReturn().getResponse().getContentAsString();
        assertThat(html).doesNotContain("<script>alert").doesNotContain("evil.example").contains("src=\"img/hero.png\"");
    }

    @Test
    void republishSharesUnchangedFilesKeepsThreeReleasesAndServesCustomDomainFromActiveRelease() throws Exception {
        String auth = "Bearer " + creatorToken;
        String first = mockMvc.perform(post("/api/projects/" + projectId + "/publish").header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"slug\":\"osis-hemat\"}"))
                .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();
        UUID siteId = UUID.fromString(JsonPath.read(first, "$.siteId"));
        SiteDomain domain = new SiteDomain(siteId, "osis-hemat.sch.id", "token-uji");
        domain.setStatus("active");
        siteDomainRepository.save(domain);

        for (int i = 0; i < 4; i++) {
            mockMvc.perform(post("/api/projects/" + projectId + "/publish").header(HttpHeaders.AUTHORIZATION, auth)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"slug\":\"osis-hemat\"}"))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.status").value("succeeded"));
        }

        // Keputusan D4: hanya 3 rilis terakhir yang tersisa, di database dan di disk.
        assertThat(siteReleaseRepository.findBySiteIdOrderByVersionDesc(siteId))
                .extracting(SiteRelease::getVersion).containsExactly(5, 4, 3);
        assertThat(Files.exists(siteStorage.releasePath(siteId, 2))).isFalse();
        assertThat(Files.exists(siteStorage.releasePath(siteId, 1))).isFalse();

        // File yang tidak berubah tersimpan sekali di disk, walau tampil di setiap folder rilis.
        Path css5 = siteStorage.releasePath(siteId, 5).resolve("css/style.css");
        Path css4 = siteStorage.releasePath(siteId, 4).resolve("css/style.css");
        assertThat(Files.isSameFile(css5, css4)).isTrue();
        // Rilis tayang tetap bisa dibuka setelah rilis lama yang ia tautkan dihapus.
        mockMvc.perform(get("/sites/osis-hemat/css/style.css")).andExpect(status().isOk());

        // Domain kustom dikenali OrgSpace-Hosting lewat resolve-domain, lalu disajikan dari rilis aktif (tanpa hosts/).
        mockMvc.perform(get("/api/public/sites/resolve-domain").param("domain", "osis-hemat.sch.id"))
                .andExpect(jsonPath("$.slug").value("osis-hemat"));
    }

    @Test
    void newWebsitesWithTheSameNameNeverOverwriteEachOther() throws Exception {
        String auth = "Bearer " + creatorToken;
        String request = """
                {"name":"Web Kembar","sourceTemplateId":"%s","templateVersion":1,"slug":"%s","organizationId":"%s",
                 "content":{"theme":{"--warna-utama":"%s"}},"missingCount":0}""";
        String budi = mockMvc.perform(post("/api/projects/publish-direct").header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON).content(request.formatted(templateId, "kembar-budi", orgId, "#111111")))
                .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();
        // Nama dan template sama, tanpa projectId (website baru dari HP lain): harus website sendiri.
        String rina = mockMvc.perform(post("/api/projects/publish-direct").header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON).content(request.formatted(templateId, "kembar-rina", orgId, "#222222")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.slug").value("kembar-rina"))
                .andReturn().getResponse().getContentAsString();
        assertThat((String) JsonPath.read(rina, "$.projectId")).isNotEqualTo(JsonPath.read(budi, "$.projectId"));

        assertThat(mockMvc.perform(get("/sites/kembar-budi/index.html")).andReturn().getResponse().getContentAsString())
                .contains("#111111").doesNotContain("#222222");
        assertThat(mockMvc.perform(get("/sites/kembar-rina/index.html")).andReturn().getResponse().getContentAsString())
                .contains("#222222");
        assertThat(jdbc.queryForList("SELECT name FROM projects WHERE organization_id = ? AND name LIKE 'Web Kembar%' ORDER BY name",
                String.class, orgId)).containsExactly("Web Kembar", "Web Kembar (2)");
    }

    @Test
    void republishIsRefusedWhileAnotherMemberIsEditing() throws Exception {
        String auth = "Bearer " + creatorToken;
        String first = mockMvc.perform(post("/api/projects/publish-direct").header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Web Dikunci\",\"slug\":\"osis-dikunci\",\"organizationId\":\"" + orgId
                                + "\",\"content\":{},\"missingCount\":0}"))
                .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(first, "$.projectId");
        UUID other = UUID.fromString(JsonPath.read(mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Rina\",\"email\":\"rina-" + UUID.randomUUID() + "@orgspace.test\",\"password\":\"password123\"}"))
                .andReturn().getResponse().getContentAsString(), "$.user.id"));
        jdbc.update("""
                INSERT INTO project_locks (project_id, user_id, device_id, acquired_at, expires_at)
                VALUES (?::uuid, ?, 'hp-rina', now(), now() + interval '2 minutes')""", id, other);

        mockMvc.perform(post("/api/projects/publish-direct").header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Web Dikunci\",\"projectId\":\"" + id + "\",\"content\":{},\"missingCount\":0}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PROJECT_LOCKED"));
    }

    @Test
    void newWebsitesOnlyFromPublishedTemplatesButExistingOnesKeepWorking() throws Exception {
        String auth = "Bearer " + creatorToken;
        String request = """
                {"name":"Web %s","sourceTemplateId":"%s","templateVersion":1,"slug":"%s","organizationId":"%s",
                 "content":{},"missingCount":0%s}""";
        String existing = mockMvc.perform(post("/api/projects/publish-direct").header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON).content(request.formatted("Lama", templateId, "osis-lama", orgId, "")))
                .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();
        String existingId = JsonPath.read(existing, "$.projectId");

        com.aris.templateapp.template.Template template = templateRepository.findById(templateId).orElseThrow();
        for (var status : List.of(com.aris.templateapp.template.TemplateStatus.DISABLED,
                com.aris.templateapp.template.TemplateStatus.DRAFT)) {
            template.setStatus(status);
            templateRepository.save(template);
            // Website BARU dari template yang tidak tayang ditolak, walau paketnya masih tersimpan di server.
            mockMvc.perform(post("/api/projects/publish-direct").header(HttpHeaders.AUTHORIZATION, auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request.formatted("Baru", templateId, "osis-baru-" + status.value(), orgId, "")))
                    .andExpect(status().isNotFound());
        }
        // ID template karangan juga ditolak (dulu diam-diam menjadi halaman bawaan).
        mockMvc.perform(post("/api/projects/publish-direct").header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.formatted("Asal", UUID.randomUUID(), "osis-asal", orgId, "")))
                .andExpect(status().isNotFound());

        // Website yang sudah ada tetap bisa diterbitkan ulang walau templatenya diturunkan (keputusan Aris).
        template.setStatus(com.aris.templateapp.template.TemplateStatus.DISABLED);
        templateRepository.save(template);
        mockMvc.perform(post("/api/projects/publish-direct").header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.formatted("Lama", templateId, "osis-lama", orgId, ",\"projectId\":\"" + existingId + "\"")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.version").value(2));
        // ...tetapi templatenya tidak bisa diganti lewat publish ulang.
        mockMvc.perform(post("/api/projects/publish-direct").header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request.formatted("Lama", UUID.randomUUID(), "osis-lama", orgId, ",\"projectId\":\"" + existingId + "\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void publishingFromAnotherProvidersTemplateCountsAsUsedOnce() throws Exception {
        String providerRes = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Studio Lain\",\"email\":\"studio-" + UUID.randomUUID().toString().substring(0, 8)
                                + "@orgspace.test\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        UUID providerId = UUID.fromString(JsonPath.read(providerRes, "$.user.id"));
        providerProfileRepository.save(new com.aris.templateapp.provider.ProviderProfile(
                providerId, "Studio Lain", java.time.Instant.now()));
        com.aris.templateapp.template.Template other = new com.aris.templateapp.template.Template(
                providerId, "Template Studio Lain",
                com.aris.templateapp.organization.OrgCategory.OSIS_MPK);
        other.setStatus(com.aris.templateapp.template.TemplateStatus.PUBLISHED);
        other.setPackageVersion(1);
        byte[] zipBytes = createTemplateZip();
        other.setPackageSize((long) zipBytes.length);
        other = templateRepository.save(other);
        uploadStorage.writePackage(other.getId(), zipBytes);

        String request = """
                {"name":"Web Galeri","sourceTemplateId":"%s","templateVersion":1,"slug":"osis-galeri","organizationId":"%s",
                 "content":{},"missingCount":0%s}""";
        String first = mockMvc.perform(post("/api/projects/publish-direct").header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON).content(request.formatted(other.getId(), orgId, "")))
                .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();
        // Terbit ulang website yang sama (HP mengirim projectId-nya) tidak menambah angka "dipakai".
        mockMvc.perform(post("/api/projects/publish-direct").header(HttpHeaders.AUTHORIZATION, "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON).content(request.formatted(other.getId(), orgId,
                                ",\"projectId\":\"" + JsonPath.read(first, "$.projectId") + "\"")))
                .andExpect(status().isAccepted());
        mockMvc.perform(get("/api/templates/" + other.getId()))
                .andExpect(jsonPath("$.downloads").value(1));
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
                    "content": {},
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
                    "content": {},
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
        String first = mockMvc.perform(post("/api/projects/publish-direct").header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Profil OSIS Keren\",\"slug\":\"osis-keren-kita\",\"content\":{},\"missingCount\":0}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.slug").value("osis-keren-kita"))
                .andExpect(jsonPath("$.url").value("http://localhost:3001/osis-keren-kita"))
                .andReturn().getResponse().getContentAsString();
        String publishedId = JsonPath.read(first, "$.projectId");

        // Setelah terbit, slug terkunci; terbit ulang dengan slug lain tetap memakai slug lama
        mockMvc.perform(get("/api/projects/publish-direct/slug").header(HttpHeaders.AUTHORIZATION, auth)
                        .param("name", "Profil OSIS Keren").param("projectId", publishedId).param("slug", "nama-lain"))
                .andExpect(jsonPath("$.locked").value(true))
                .andExpect(jsonPath("$.slug").value("osis-keren-kita"));
        mockMvc.perform(post("/api/projects/publish-direct").header(HttpHeaders.AUTHORIZATION, auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Profil OSIS Keren\",\"projectId\":\"" + publishedId
                                + "\",\"slug\":\"nama-lain\",\"content\":{},\"missingCount\":0}"))
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
