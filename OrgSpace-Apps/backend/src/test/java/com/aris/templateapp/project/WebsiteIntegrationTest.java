package com.aris.templateapp.project;

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
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Tab Website & halaman kelola (alur-pembuatan-website.md 4–5, 10): daftar, status, hak per peran, aksi kartu. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {"app.sites.domain=orgspace.site", "app.sites.url-scheme=https", "app.sites.hosting-url=https://orgspace.app"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebsiteIntegrationTest {

    private static final String ORG = """
            {"type":"organization","name":"OSIS SMA Negeri 1 Bandung","shortName":"OSIS Smansa","level":"sma",
             "category":"osis_mpk","institutionName":"SMA Negeri 1 Bandung"}""";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;

    private Account owner;
    private Account admin;
    private Account editor;
    private String orgId;

    @BeforeEach
    void setUp() throws Exception {
        owner = register("Rina");
        admin = register("Budi");
        editor = register("Sari");
        orgId = JsonPath.read(send(owner, post("/api/organizations"), ORG).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");
        addMember(admin, "admin");
        addMember(editor, "editor");
    }

    @Test
    void listShowsServerStatusCountsAndLock() throws Exception {
        Instant now = Instant.now();
        UUID draft = project("Pensi 2026", editor, now.minus(Duration.ofMinutes(5)));
        UUID changed = project("Website OSIS 2026", owner, now.minus(Duration.ofHours(1)));
        site(changed, "osis-smansa", "online", now.minus(Duration.ofDays(1)));
        UUID online = project("Profil OSIS", owner, now.minus(Duration.ofDays(3)));
        site(online, "profil-osis", "online", now.minus(Duration.ofDays(2)));
        UUID offline = project("Arsip 2025", owner, now.minus(Duration.ofDays(30)));
        site(offline, "arsip-osis", "offline", now.minus(Duration.ofDays(29)));
        lock(draft, editor);

        send(admin, get("/api/organizations/" + orgId + "/websites"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].name").value(contains("Pensi 2026", "Website OSIS 2026", "Profil OSIS",
                        "Arsip 2025")))
                .andExpect(jsonPath("$.items[0].status").value("draft"))
                .andExpect(jsonPath("$.items[0].siteUrl").doesNotExist())
                .andExpect(jsonPath("$.items[0].lockedBy.name").value("Sari"))
                .andExpect(jsonPath("$.items[0].updatedByName").value("Sari"))
                .andExpect(jsonPath("$.items[1].status").value("online"))
                .andExpect(jsonPath("$.items[1].hasUnpublishedChanges").value(true))
                .andExpect(jsonPath("$.items[1].siteUrl").value("https://orgspace.app/osis-smansa"))
                .andExpect(jsonPath("$.items[2].hasUnpublishedChanges").value(false))
                .andExpect(jsonPath("$.items[3].status").value("offline"))
                .andExpect(jsonPath("$.counts.all").value(4))
                .andExpect(jsonPath("$.counts.draft").value(1))
                .andExpect(jsonPath("$.counts.online").value(2))
                .andExpect(jsonPath("$.counts.changed").value(1))
                .andExpect(jsonPath("$.sitesUsed").value(3))
                .andExpect(jsonPath("$.sitesLimit").value(5))
                .andExpect(jsonPath("$.hasMore").value(false));

        // Kunci milik sendiri tidak ditampilkan sebagai "Sedang diedit oleh ...".
        send(editor, get("/api/organizations/" + orgId + "/websites?status=draft"), null)
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].lockedBy").doesNotExist());

        send(owner, get("/api/organizations/" + orgId + "/websites?status=changed"), null)
                .andExpect(jsonPath("$.items[*].name").value(contains("Website OSIS 2026")));
        send(owner, get("/api/organizations/" + orgId + "/websites?q=osis&sort=name"), null)
                .andExpect(jsonPath("$.items[*].name").value(contains("Profil OSIS", "Website OSIS 2026")))
                .andExpect(jsonPath("$.counts.all").value(2));
        send(owner, get("/api/organizations/" + orgId + "/websites?size=2"), null)
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.hasMore").value(true));
        send(owner, get("/api/organizations/" + orgId + "/websites?status=aneh"), null)
                .andExpect(status().isBadRequest());

        // Bukan anggota tidak boleh melihat.
        Account stranger = register("Asing");
        send(stranger, get("/api/organizations/" + orgId + "/websites"), null)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_ORG_MEMBER"));
        send(stranger, get("/api/projects/" + draft), null)
                .andExpect(jsonPath("$.code").value("NOT_ORG_MEMBER"));
        send(owner, get("/api/projects/" + UUID.randomUUID()), null)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROJECT_NOT_FOUND"));
    }

    @Test
    void renameDuplicateAndDeleteFollowRoles() throws Exception {
        UUID website = project("Aksara", owner, Instant.now());
        UUID asset = UUID.fromString(jdbc.queryForObject("""
                INSERT INTO project_assets (organization_id, project_id, sha256, mime_type, width, height, size_bytes,
                                            storage_path)
                VALUES (?, ?, 'abc', 'image/webp', 10, 10, 100, 'x/y.webp') RETURNING id::text
                """, String.class, UUID.fromString(orgId), website));
        jdbc.update("UPDATE projects SET content = ?::jsonb WHERE id = ?",
                "{\"fields\":{\"foto\":{\"image\":\"asset:" + asset + "\"}}}", website);

        send(editor, patch("/api/projects/" + website), "{\"name\":\"  Aksara Baru  \"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.website.name").value("Aksara Baru"))
                .andExpect(jsonPath("$.website.updatedByName").value("Sari"));

        String copy = JsonPath.read(send(editor, post("/api/projects/" + website + "/duplicate"), null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.website.name").value("Aksara Baru (2)"))
                .andExpect(jsonPath("$.website.status").value("draft"))
                .andReturn().getResponse().getContentAsString(), "$.website.id");
        send(editor, post("/api/projects/" + website + "/duplicate"), "{}")
                .andExpect(jsonPath("$.website.name").value("Aksara Baru (3)"));
        // Rujukan gambar di salinan menunjuk aset milik salinan itu sendiri.
        String copiedRef = jdbc.queryForObject("SELECT content->'fields'->'foto'->>'image' FROM projects WHERE id = ?",
                String.class, UUID.fromString(copy));
        String copiedAsset = jdbc.queryForObject("SELECT id::text FROM project_assets WHERE project_id = ?",
                String.class, UUID.fromString(copy));
        assertThat(copiedRef).isEqualTo("asset:" + copiedAsset).isNotEqualTo("asset:" + asset);

        // Editor tidak boleh menghapus website orang lain; Admin juga tidak (hanya Pemilik).
        send(editor, delete("/api/projects/" + website), null).andExpect(status().isForbidden());
        send(admin, delete("/api/projects/" + website), null)
                .andExpect(jsonPath("$.code").value("ROLE_NOT_ALLOWED"));

        // Website yang pernah terbit wajib mengetik ulang namanya.
        site(website, "aksara", "online", Instant.now());
        send(owner, delete("/api/projects/" + website), "{\"confirmName\":\"Aksara\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CONFIRM_NAME_MISMATCH"));
        send(owner, delete("/api/projects/" + website), "{\"confirmName\":\"aksara baru\"}")
                .andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM sites WHERE slug = 'aksara'", Long.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM activity_logs WHERE action = 'website.deleted'",
                Long.class)).isEqualTo(1);

        // Pembuat boleh menghapus draft barunya yang belum pernah diubah (website kosong).
        UUID empty = project("Kosong", editor, Instant.now());
        jdbc.update("UPDATE projects SET content_version = 0 WHERE id = ?", empty);
        send(admin, delete("/api/projects/" + empty), null).andExpect(status().isForbidden());
        send(editor, delete("/api/projects/" + empty), null).andExpect(status().isNoContent());
    }

    @Test
    void publishRequestsAreOnePerWebsiteAndOnlyAdminsSeeThem() throws Exception {
        UUID website = project("Website OSIS 2026", owner, Instant.now());

        send(editor, post("/api/projects/" + website + "/publish-requests"), "{\"note\":\"Sudah siap\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.requestedByName").value("Sari"))
                .andExpect(jsonPath("$.note").value("Sudah siap"));
        send(editor, post("/api/projects/" + website + "/publish-requests"), "{\"note\":\"Tolong cek foto\"}")
                .andExpect(status().isCreated());

        String requestId = JsonPath.read(send(admin, get("/api/organizations/" + orgId + "/publish-requests"), null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].projectName").value("Website OSIS 2026"))
                .andExpect(jsonPath("$[0].note").value("Tolong cek foto"))
                .andReturn().getResponse().getContentAsString(), "$[0].id");
        send(owner, get("/api/projects/" + website), null)
                .andExpect(jsonPath("$.openRequest.id").value(requestId));

        send(editor, get("/api/organizations/" + orgId + "/publish-requests"), null)
                .andExpect(jsonPath("$.code").value("ROLE_NOT_ALLOWED"));
        send(editor, post("/api/publish-requests/" + requestId + "/dismiss"), null).andExpect(status().isForbidden());
        send(admin, post("/api/publish-requests/" + requestId + "/dismiss"), null).andExpect(status().isNoContent());
        send(admin, get("/api/organizations/" + orgId + "/publish-requests"), null)
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ---------- pembantu ----------

    private record Account(UUID id, String token, String name) {
    }

    private Account register(String name) throws Exception {
        String body = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"%s\",\"email\":\"web-%s@mail.com\",\"password\":\"rahasia123\"}"
                                .formatted(name, UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return new Account(UUID.fromString(JsonPath.read(body, "$.user.id")), JsonPath.read(body, "$.accessToken"), name);
    }

    private void addMember(Account account, String role) {
        jdbc.update("INSERT INTO organization_members (organization_id, user_id, role) VALUES (?, ?, ?)",
                UUID.fromString(orgId), account.id, role);
    }

    private UUID project(String name, Account by, Instant updatedAt) {
        Timestamp time = Timestamp.from(updatedAt);
        return UUID.fromString(jdbc.queryForObject("""
                INSERT INTO projects (organization_id, name, mode, content_version, content_updated_at, created_by,
                                      updated_by, created_at, updated_at)
                VALUES (?, ?, 'template', 1, ?, ?, ?, ?, ?) RETURNING id::text
                """, String.class, UUID.fromString(orgId), name, time, by.id, by.id, time, time));
    }

    private void site(UUID projectId, String slug, String status, Instant releasedAt) {
        Timestamp time = Timestamp.from(releasedAt);
        UUID siteId = UUID.fromString(jdbc.queryForObject("""
                INSERT INTO sites (project_id, organization_id, slug, status, published_at) VALUES (?, ?, ?, ?, ?)
                RETURNING id::text
                """, String.class, projectId, UUID.fromString(orgId), slug, status, time));
        UUID releaseId = UUID.fromString(jdbc.queryForObject("""
                INSERT INTO site_releases (site_id, version, project_snapshot, kind, created_at)
                VALUES (?, 1, '{}'::jsonb, 'publish', ?) RETURNING id::text
                """, String.class, siteId, time));
        jdbc.update("UPDATE sites SET current_release_id = ? WHERE id = ?", releaseId, siteId);
    }

    private void lock(UUID projectId, Account holder) {
        jdbc.update("INSERT INTO project_locks (project_id, user_id, device_id, expires_at) VALUES (?, ?, 'hp', ?)",
                projectId, holder.id, Timestamp.from(Instant.now().plus(Duration.ofMinutes(2))));
    }

    private ResultActions send(Account account, MockHttpServletRequestBuilder request, String json) throws Exception {
        request.header(HttpHeaders.AUTHORIZATION, "Bearer " + account.token);
        if (json != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return mockMvc.perform(request);
    }
}
