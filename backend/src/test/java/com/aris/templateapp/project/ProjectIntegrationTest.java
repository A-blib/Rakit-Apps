package com.aris.templateapp.project;

import com.aris.templateapp.TestcontainersConfiguration;
import com.aris.templateapp.organization.OrgCategory;
import com.aris.templateapp.seed.DemoPackages;
import com.aris.templateapp.template.Template;
import com.aris.templateapp.template.TemplateRepository;
import com.aris.templateapp.template.TemplateStatus;
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
import org.springframework.transaction.support.TransactionTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Website di cloud (alur-buat-website-via-template.md 13, fase T2): buat → isi dengan baseVersion → konflik versi →
 * kunci, ambil alih, kunci kedaluwarsa diperpanjang → cadangan & pulihkan → gambar & batas penyimpanan.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "app.projects.storage-max-mb-per-org=1")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectIntegrationTest {

    private static final String ORG = """
            {"type":"organization","name":"OSIS SMA Negeri 1 Bandung","shortName":"OSIS Smansa","level":"sma",
             "category":"osis_mpk","institutionName":"SMA Negeri 1 Bandung"}""";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private TemplateRepository templateRepository;
    @Autowired private DemoPackages demoPackages;
    @Autowired private TransactionTemplate tx;

    private Account owner;
    private Account editor;
    private String orgId;
    private UUID templateId;

    @BeforeEach
    void setUp() throws Exception {
        Account provider = register("Studio");
        send(provider, post("/api/users/me/onboarding/provider"), "{\"creatorName\":\"Studio\",\"agreedToTerms\":true}")
                .andExpect(status().isOk());
        templateId = tx.execute(s -> {
            Template template = templateRepository.save(new Template(provider.id, "UMKM Kuliner", OrgCategory.LAINNYA));
            template.setStatus(TemplateStatus.PUBLISHED);
            template.setPublishedAt(Instant.now());
            demoPackages.attach(template, DemoPackages.KULINER, Instant.now());
            return template.getId();
        });

        owner = register("Rina");
        editor = register("Budi");
        orgId = JsonPath.read(send(owner, post("/api/organizations"), ORG).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");
        jdbc.update("INSERT INTO organization_members (organization_id, user_id, role) VALUES (?::uuid, ?, 'editor')",
                orgId, editor.id);
    }

    @Test
    void createFillsProfileFieldsAndSyncsWithOptimisticLocking() throws Exception {
        // Instagram sudah ada di profil; kolom profil lain yang kosong dilewati.
        jdbc.update("UPDATE organizations SET instagram = 'osis.smansa' WHERE id = ?::uuid", orgId);

        String created = send(owner, post("/api/organizations/" + orgId + "/projects"), create("hp-a"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.website.name").value("UMKM Kuliner"))
                .andExpect(jsonPath("$.contentVersion").value(0))
                .andExpect(jsonPath("$.templateVersion").value(1))
                .andExpect(jsonPath("$.lock.mine").value(true))
                .andExpect(jsonPath("$.content.fields.nama_toko.text").value("OSIS Smansa"))
                .andExpect(jsonPath("$.content.fields.nama_toko.fromProfile").value(true))
                .andExpect(jsonPath("$.content.fields.link_instagram.href").value("https://instagram.com/osis.smansa"))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(created, "$.website.id");
        send(owner, post("/api/organizations/" + orgId + "/projects"), create("hp-a"))
                .andExpect(jsonPath("$.website.name").value("UMKM Kuliner (2)"));

        String content = "{\"fields\":{\"judul_hero\":{\"text\":\"Kue enak\"}},\"styles\":{},\"theme\":{}}";
        send(owner, put("/api/projects/" + id + "/content"), body(0, content, 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentVersion").value(1));
        // HP lain dengan versi lama ditolak.
        send(owner, put("/api/projects/" + id + "/content"), body(0, content, 2))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("VERSION_CONFLICT"))
                .andExpect(jsonPath("$.details.contentVersion").value(1));
        send(owner, get("/api/projects/" + id), null)
                .andExpect(jsonPath("$.content.fields.judul_hero.text").value("Kue enak"))
                .andExpect(jsonPath("$.website.missingCount").value(2));

        // Isi tidak sesuai tandaan template ditolak server.
        send(owner, put("/api/projects/" + id + "/content"),
                body(1, "{\"fields\":{\"tidak_ada\":{\"text\":\"x\"}}}", 0)).andExpect(status().isBadRequest());
        send(owner, put("/api/projects/" + id + "/content"),
                body(1, "{\"fields\":{\"link_instagram\":{\"href\":\"javascript:alert(1)\"}}}", 0))
                .andExpect(status().isBadRequest());
        send(owner, put("/api/projects/" + id + "/content"),
                body(1, "{\"fields\":{\"foto_hero\":{\"image\":\"local:abc\"}}}", 0)).andExpect(status().isBadRequest());
    }

    @Test
    void lockBlocksOthersAndCanBeTakenOverOrExpire() throws Exception {
        String id = JsonPath.read(send(editor, post("/api/organizations/" + orgId + "/projects"), create("hp-budi"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.website.id");

        // Pemilik membuka website yang sedang diedit Budi → mode baca.
        send(owner, post("/api/projects/" + id + "/lock"), "{\"deviceId\":\"hp-rina\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PROJECT_LOCKED"))
                .andExpect(jsonPath("$.details.holderName").value("Budi"));
        send(owner, get("/api/projects/" + id), null)
                .andExpect(jsonPath("$.lock.holderName").value("Budi"))
                .andExpect(jsonPath("$.lock.mine").value(false));

        // Budi offline lama: kuncinya kedaluwarsa tetapi belum diambil orang → tetap boleh menyinkron.
        jdbc.update("UPDATE project_locks SET expires_at = now() - interval '5 minutes' WHERE project_id = ?::uuid", id);
        send(editor, put("/api/projects/" + id + "/content"), body(0, "{\"fields\":{}}", 0)).andExpect(status().isOk());
        send(editor, post("/api/projects/" + id + "/lock"), "{\"deviceId\":\"hp-budi\"}").andExpect(status().isOk());

        // Editor tidak boleh mengambil alih; Pemilik boleh.
        send(editor, post("/api/projects/" + id + "/lock/takeover"), "{\"deviceId\":\"hp-budi\"}")
                .andExpect(status().isForbidden());
        send(owner, post("/api/projects/" + id + "/lock/takeover"), "{\"deviceId\":\"hp-rina\"}").andExpect(status().isOk());
        send(editor, put("/api/projects/" + id + "/content"), body(1, "{\"fields\":{}}", 0))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LOCK_LOST"))
                .andExpect(jsonPath("$.details.holderName").value("Rina"));

        // Perubahan Budi yang tergeser disimpan sebagai cadangan, lalu Pemilik memulihkannya.
        String backup = JsonPath.read(send(editor, post("/api/projects/" + id + "/backups"),
                        "{\"content\":{\"fields\":{\"judul_hero\":{\"text\":\"Versi Budi\"}}},\"reason\":\"lock_lost\"}")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.id");
        send(owner, get("/api/projects/" + id + "/backups"), null)
                .andExpect(jsonPath("$[0].reason").value("lock_lost"))
                .andExpect(jsonPath("$[0].createdByName").value("Budi"));
        send(owner, post("/api/projects/" + id + "/backups/" + backup + "/restore"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentVersion").value(2));
        send(owner, get("/api/projects/" + id), null)
                .andExpect(jsonPath("$.content.fields.judul_hero.text").value("Versi Budi"));

        // Kunci kedaluwarsa milik orang lain boleh diambil langsung; menutup editor melepas kunci.
        jdbc.update("UPDATE project_locks SET expires_at = now() - interval '1 minute' WHERE project_id = ?::uuid", id);
        send(editor, post("/api/projects/" + id + "/lock"), "{\"deviceId\":\"hp-budi\"}").andExpect(status().isOk());
        send(editor, delete("/api/projects/" + id + "/lock"), null).andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM project_locks WHERE project_id = ?::uuid", Long.class, id))
                .isZero();
    }

    @Test
    void imagesAreCheckedOwnedAndLimitedPerWorkspace() throws Exception {
        String id = JsonPath.read(send(owner, post("/api/organizations/" + orgId + "/projects"), create("hp-a"))
                .andReturn().getResponse().getContentAsString(), "$.website.id");
        byte[] png = png();

        String asset = JsonPath.read(upload(owner, "/api/projects/" + id + "/assets", new MockMultipartFile("file", "foto.png", "image/png", png))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.width").value(40))
                .andReturn().getResponse().getContentAsString(), "$.assetId");
        // Unggahan ulang gambar yang sama tidak menambah aset.
        upload(owner, "/api/projects/" + id + "/assets", new MockMultipartFile("file", "f.png", "image/png", png))
                .andExpect(jsonPath("$.assetId").value(asset));
        upload(owner, "/api/projects/" + id + "/assets", new MockMultipartFile("file", "virus.png", "image/png", "bukan gambar".getBytes()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ASSET_INVALID"));

        send(owner, put("/api/projects/" + id + "/content"),
                body(0, "{\"fields\":{\"foto_hero\":{\"image\":\"asset:" + asset + "\"}}}", 0)).andExpect(status().isOk());
        send(owner, get("/api/assets/" + asset), null).andExpect(status().isOk());
        send(register("Asing"), get("/api/assets/" + asset), null).andExpect(status().isForbidden());

        // Aset website lain tidak boleh dirujuk.
        String other = JsonPath.read(send(owner, post("/api/organizations/" + orgId + "/projects"), create("hp-a"))
                .andReturn().getResponse().getContentAsString(), "$.website.id");
        send(owner, put("/api/projects/" + other + "/content"),
                body(0, "{\"fields\":{\"foto_hero\":{\"image\":\"asset:" + asset + "\"}}}", 0))
                .andExpect(status().isBadRequest());

        // Batas penyimpanan ruang kerja (1 MB di test ini).
        jdbc.update("""
                INSERT INTO project_assets (organization_id, project_id, sha256, mime_type, width, height, size_bytes,
                                            storage_path) VALUES (?::uuid, ?::uuid, 'besar', 'image/webp', 1, 1, 1048570, 'x')""",
                orgId, id);
        upload(owner, "/api/projects/" + id + "/assets", new MockMultipartFile("file", "lain.png", "image/png",
                png(41)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STORAGE_LIMIT_REACHED"));
    }

    // ---------- pembantu ----------

    private record Account(UUID id, String token) {
    }

    private Account register(String name) throws Exception {
        String body = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"%s\",\"email\":\"prj-%s@mail.com\",\"password\":\"rahasia123\"}"
                                .formatted(name, UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return new Account(UUID.fromString(JsonPath.read(body, "$.user.id")), JsonPath.read(body, "$.accessToken"));
    }

    private String create(String deviceId) {
        return "{\"sourceTemplateId\":\"%s\",\"templateVersion\":1,\"deviceId\":\"%s\"}".formatted(templateId, deviceId);
    }

    private static String body(int baseVersion, String content, int missing) {
        return "{\"baseVersion\":%d,\"content\":%s,\"missingCount\":%d}".formatted(baseVersion, content, missing);
    }

    private static byte[] png() throws Exception {
        return png(40);
    }

    private static byte[] png(int size) throws Exception {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    private ResultActions upload(Account account, String url, MockMultipartFile file) throws Exception {
        return mockMvc.perform(multipart(url).file(file).header(HttpHeaders.AUTHORIZATION, "Bearer " + account.token));
    }

    private ResultActions send(Account account, MockHttpServletRequestBuilder request, String json) throws Exception {
        request.header(HttpHeaders.AUTHORIZATION, "Bearer " + account.token);
        if (json != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return mockMvc.perform(request);
    }
}
