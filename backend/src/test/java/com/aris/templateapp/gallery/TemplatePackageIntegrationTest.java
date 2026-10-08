package com.aris.templateapp.gallery;

import com.aris.templateapp.TestcontainersConfiguration;
import com.aris.templateapp.seed.DemoPackages;
import com.aris.templateapp.template.Template;
import com.aris.templateapp.template.TemplateRepository;
import com.aris.templateapp.template.TemplateStatus;
import com.aris.templateapp.upload.UploadStorage;
import com.aris.templateapp.upload.check.TemplateChecker;
import com.aris.templateapp.upload.publish.ManifestBuilder;
import com.aris.templateapp.upload.publish.PackageManifestBackfill;
import com.aris.templateapp.upload.publish.TemplateManifest;
import com.aris.templateapp.user.WebsitePurpose;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Detail, paket, dan event template untuk pembuat website (alur-buat-website-via-template.md bagian 12), memakai
 * paket contoh seeder ({@link DemoPackages}) yang dibuat lewat jalur Kirim yang sama.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TemplatePackageIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private TemplateRepository templateRepository;
    @Autowired private DemoPackages demoPackages;
    @Autowired private TemplateChecker checker;
    @Autowired private TransactionTemplate tx;
    @Autowired private UploadStorage storage;
    @Autowired private PackageManifestBackfill backfill;

    private UUID providerId;
    private String providerToken;

    @BeforeEach
    void setUp() throws Exception {
        jdbc.update("DELETE FROM templates");
        providerToken = register();
        String me = mockMvc.perform(post("/api/users/me/onboarding/provider")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + providerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"creatorName\":\"Rina Studio\",\"agreedToTerms\":true}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        providerId = UUID.fromString(JsonPath.read(me, "$.id"));
    }

    @Test
    void guestReadsDetailOfPublishedTemplate() throws Exception {
        UUID id = publish(DemoPackages.SEKOLAH, "Profil Sekolah", WebsitePurpose.SEKOLAH);

        mockMvc.perform(get("/api/templates/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Profil Sekolah"))
                .andExpect(jsonPath("$.creatorName").value("Rina Studio"))
                .andExpect(jsonPath("$.category").value("sekolah"))
                .andExpect(jsonPath("$.pages", contains("Beranda", "Tentang", "Kontak")))
                .andExpect(jsonPath("$.responsive").value(true))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.packageSizeBytes").isNumber())
                .andExpect(jsonPath("$.thumbnailUrl").isString());
    }

    @Test
    void packageContainsManifestAndMarkedHtml() throws Exception {
        UUID id = publish(DemoPackages.SEKOLAH, "Profil Sekolah", WebsitePurpose.SEKOLAH);

        MockHttpServletResponse response = mockMvc.perform(get("/api/templates/" + id + "/package"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Template-Version", "1"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/zip"))
                .andReturn().getResponse();
        byte[] zip = response.getContentAsByteArray();
        // Content-Length dikirim agar HP bisa menghitung progres unduhan.
        assertThat(response.getContentLengthLong()).isEqualTo(zip.length);

        Map<String, String> files = unzip(zip);
        assertThat(files).containsKeys("manifest.json", "index.html", "tentang.html", "kontak.html", "css/style.css");
        assertThat(files.get("index.html")).contains("data-key=\"nama_sekolah\"").doesNotContain("data-tpl-id");

        TemplateManifest manifest = ManifestBuilder.read(zip);
        assertThat(manifest.templateId()).isEqualTo(id);
        assertThat(manifest.pages()).extracting(TemplateManifest.Page::name).containsExactly("Beranda", "Tentang", "Kontak");
        // Isian terhubung lintas halaman.
        TemplateManifest.Field nama = field(manifest, "nama_sekolah");
        assertThat(nama.pages()).containsExactly("index.html", "tentang.html", "kontak.html");
        assertThat(nama.sample()).isEqualTo("SMA Nusantara");
        assertThat(field(manifest, "foto_gedung").sample()).isEqualTo("img/gedung.jpg");
        assertThat(field(manifest, "foto_gedung").aspectRatio()).isEqualTo("16:9");
        assertThat(field(manifest, "whatsapp").sampleHref()).isEqualTo("https://wa.me/6281234567890");
        // Nilai bawaan tema diambil dari :root CSS template.
        assertThat(manifest.theme()).extracting(TemplateManifest.ThemeVar::defaultValue).containsExactly("#1e3a8a", "#f59e0b", "8px");
        // Urutan isian mengikuti urutan halaman lalu posisi elemen.
        assertThat(manifest.fields().get(0).key()).isEqualTo("nama_sekolah");
        assertThat(manifest.fields()).extracting(TemplateManifest.Field::order).startsWith(1, 2, 3);
    }

    @Test
    void allDemoPackagesPassTheChecker() {
        for (String slug : new String[] {DemoPackages.KULINER, DemoPackages.SEKOLAH, DemoPackages.PORTOFOLIO}) {
            Template template = tx.execute(s -> templateRepository.save(new Template(providerId, slug, WebsitePurpose.LAINNYA)));
            DemoPackages.Built built = demoPackages.build(slug, template);
            TemplateChecker.Result result = checker.check(built.sourceZip(), slug + ".zip", stage -> { });
            assertThat(result.passed()).as(slug + ": " + result.findings()).isTrue();
            assertThat(built.marking().fields()).hasSizeGreaterThanOrEqualTo(3);
        }
    }

    @Test
    void hiddenTemplatesAreNotFound() throws Exception {
        UUID id = publish(DemoPackages.KULINER, "UMKM Kuliner", WebsitePurpose.UMKM);

        jdbc.update("UPDATE templates SET status = 'disabled' WHERE id = ?", id);
        mockMvc.perform(get("/api/templates/" + id)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/templates/" + id + "/package")).andExpect(status().isNotFound());

        jdbc.update("UPDATE templates SET status = 'published' WHERE id = ?", id);
        jdbc.update("UPDATE provider_profiles SET status = 'suspended' WHERE user_id = ?", providerId);
        mockMvc.perform(get("/api/templates/" + id)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/templates/" + UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test
    void templateWithoutPackageHasNoSizeAndNoPackage() throws Exception {
        UUID id = jdbc.queryForObject("""
                INSERT INTO templates (provider_id, name, category, status, published_at)
                VALUES (?, 'Lama', 'umkm', 'published', now()) RETURNING id""", UUID.class, providerId);

        mockMvc.perform(get("/api/templates/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.packageSizeBytes").doesNotExist());
        mockMvc.perform(get("/api/templates/" + id + "/package")).andExpect(status().isNotFound());
    }

    @Test
    void guestViewAndDownloadEventsAreCountedOncePerProject() throws Exception {
        UUID id = publish(DemoPackages.KULINER, "UMKM Kuliner", WebsitePurpose.UMKM);
        String view = "{\"type\":\"view\",\"installId\":\"hp-1\",\"occurredAt\":\"%s\"}".formatted(Instant.now());
        String download = "{\"type\":\"download\",\"projectId\":\"p-1\",\"installId\":\"hp-1\",\"occurredAt\":\"%s\"}"
                .formatted(Instant.now());

        mockMvc.perform(post("/api/templates/" + id + "/events").contentType(MediaType.APPLICATION_JSON).content(view))
                .andExpect(status().isAccepted());
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/templates/" + id + "/events").contentType(MediaType.APPLICATION_JSON).content(download))
                    .andExpect(status().isAccepted());
        }
        // Event dari pemilik template diabaikan.
        mockMvc.perform(post("/api/templates/" + id + "/events").header(HttpHeaders.AUTHORIZATION, "Bearer " + providerToken)
                        .contentType(MediaType.APPLICATION_JSON).content(view))
                .andExpect(status().isAccepted());

        assertThat(jdbc.queryForObject("SELECT count(*) FROM template_events WHERE template_id = ? AND type = 'view'",
                Integer.class, id)).isEqualTo(1);
        mockMvc.perform(get("/api/templates/" + id)).andExpect(jsonPath("$.downloads").value(1));
    }

    @Test
    void packagesPublishedBeforeManifestExistedAreCompletedAtStartup() throws Exception {
        UUID id = publish(DemoPackages.KULINER, "UMKM Kuliner", WebsitePurpose.UMKM);
        // Tiru paket Fase 18: tanpa manifest.json dan tanpa ukuran tercatat.
        Map<String, String> files = unzip(Files.readAllBytes(storage.packageZipPath(id)));
        files.remove(ManifestBuilder.FILE_NAME);
        storage.writePackage(id, zip(files));
        jdbc.update("UPDATE templates SET package_size = NULL WHERE id = ?", id);
        mockMvc.perform(get("/api/templates/" + id + "/package")).andExpect(status().isNotFound());

        backfill.run(null);

        byte[] zip = mockMvc.perform(get("/api/templates/" + id + "/package"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(ManifestBuilder.read(zip).fields()).extracting(TemplateManifest.Field::key).contains("nama_toko", "foto_hero");
    }

    private static byte[] zip(Map<String, String> files) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zip = new java.util.zip.ZipOutputStream(out)) {
            for (Map.Entry<String, String> file : files.entrySet()) {
                zip.putNextEntry(new ZipEntry(file.getKey()));
                zip.write(file.getValue().getBytes(StandardCharsets.ISO_8859_1));
                zip.closeEntry();
            }
        }
        return out.toByteArray();
    }

    private UUID publish(String slug, String name, WebsitePurpose category) {
        return tx.execute(s -> {
            Template template = templateRepository.save(new Template(providerId, name, category));
            template.setStatus(TemplateStatus.PUBLISHED);
            template.setPublishedAt(Instant.now());
            demoPackages.attach(template, slug, Instant.now());
            return template.getId();
        });
    }

    private static TemplateManifest.Field field(TemplateManifest manifest, String key) {
        return manifest.fields().stream().filter(f -> f.key().equals(key)).findFirst().orElseThrow();
    }

    /** ISO-8859-1 agar byte gambar tidak berubah saat dibaca sebagai teks lalu ditulis lagi. */
    private static Map<String, String> unzip(byte[] zip) throws Exception {
        Map<String, String> files = new HashMap<>();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                files.put(entry.getName(), new String(in.readAllBytes(), StandardCharsets.ISO_8859_1));
            }
        }
        return files;
    }

    private String register() throws Exception {
        String body = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Aris\",\"email\":\"user-%s@mail.com\",\"password\":\"rahasia123\"}"
                                .formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }
}
