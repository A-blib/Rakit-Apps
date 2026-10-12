package com.aris.templateapp.organization;

import com.aris.templateapp.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Alur ruang kerja end-to-end (revisi-fondasi-orgspace.md 4.5): undang → terima → peran → serah terima → keluar. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "app.org.max-members=3")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrganizationIntegrationTest {

    private static final String OSIS = """
            {"type":"organization","name":"OSIS SMA Negeri 1 Bandung","shortName":"OSIS Smansa","level":"sma",
             "category":"osis_mpk","institutionName":"SMA Negeri 1 Bandung","city":"Bandung",
             "periodStartYear":2026,"periodEndYear":2027,"positionTitle":"Ketua"}""";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void fullMembershipLifecycle() throws Exception {
        Account ketua = register("Rina");
        Account sekretaris = register("Budi");
        String orgId = createOsis(ketua);

        // Pemilik membuat undangan; kode hanya dikirim sekali.
        String code = JsonPath.read(send(ketua, post("/api/organizations/" + orgId + "/invitations"), "{\"role\":\"editor\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").isString())
                .andReturn().getResponse().getContentAsString(), "$.code");
        send(ketua, get("/api/organizations/" + orgId + "/invitations"), null)
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code").doesNotExist());

        // Pratinjau lalu gabung, kode boleh diketik huruf kecil tanpa tanda hubung.
        send(sekretaris, post("/api/invitations/preview"), "{\"code\":\"%s\"}".formatted(code.toLowerCase().replace("-", "")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationName").value("OSIS SMA Negeri 1 Bandung"))
                .andExpect(jsonPath("$.role").value("editor"));
        send(sekretaris, post("/api/invitations/accept"), "{\"code\":\"%s\",\"positionTitle\":\"Sekretaris\"}".formatted(code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myRole").value("editor"))
                .andExpect(jsonPath("$.memberCount").value(2));
        send(sekretaris, post("/api/invitations/accept"), "{\"code\":\"%s\"}".formatted(code))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALREADY_MEMBER"));
        send(sekretaris, get("/api/users/me"), null).andExpect(jsonPath("$.lastOrganizationId").value(orgId));

        // Editor bisa melihat profil tapi tidak bisa mengubah (skenario 9).
        send(sekretaris, get("/api/organizations/" + orgId), null).andExpect(status().isOk());
        send(sekretaris, patch("/api/organizations/" + orgId), "{\"name\":\"Ganti\"}")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ROLE_NOT_ALLOWED"));

        // Pemilik menaikkan Editor menjadi Admin; Admin tidak boleh mengubah Pemilik.
        send(ketua, patch("/api/organizations/" + orgId + "/members/" + sekretaris.id), "{\"role\":\"admin\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("admin"));
        send(sekretaris, patch("/api/organizations/" + orgId + "/members/" + ketua.id), "{\"role\":\"editor\"}")
                .andExpect(status().isForbidden());
        send(ketua, patch("/api/organizations/" + orgId + "/members/" + sekretaris.id), "{\"role\":\"owner\"}")
                .andExpect(status().isForbidden());

        // Pemilik terakhir tidak bisa keluar (skenario 11).
        send(ketua, post("/api/organizations/" + orgId + "/leave"), null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LAST_OWNER"));

        // Serah terima lalu Pemilik lama keluar (skenario 10).
        send(ketua, post("/api/organizations/" + orgId + "/transfer-ownership"),
                "{\"userId\":\"%s\",\"keepMeAs\":\"leave\"}".formatted(sekretaris.id))
                .andExpect(status().isNoContent());
        send(sekretaris, get("/api/organizations/" + orgId + "/members"), null)
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].role").value("owner"));
        send(ketua, get("/api/organizations/" + orgId), null)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("NOT_ORG_MEMBER"));
        send(ketua, get("/api/users/me"), null).andExpect(jsonPath("$.lastOrganizationId").doesNotExist());

        send(sekretaris, get("/api/organizations/" + orgId + "/activities"), null)
                .andExpect(jsonPath("$.items[0].action").value("member.left"))
                .andExpect(jsonPath("$.items[1].action").value("org.ownership_transferred"))
                .andExpect(jsonPath("$.items[1].actorName").value("Rina"));
    }

    @Test
    void revokedExpiredAndWrongCodesAreRejectedTheSameWay() throws Exception {
        Account owner = register("Rina");
        Account other = register("Andi");
        String orgId = createOsis(owner);
        String body = send(owner, post("/api/organizations/" + orgId + "/invitations"), "{\"role\":\"editor\"}")
                .andReturn().getResponse().getContentAsString();
        String code = JsonPath.read(body, "$.code");
        String invitationId = JsonPath.read(body, "$.id");

        send(owner, delete("/api/organizations/" + orgId + "/invitations/" + invitationId), null)
                .andExpect(status().isNoContent());
        send(other, post("/api/invitations/accept"), "{\"code\":\"%s\"}".formatted(code))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVITATION_INVALID"));

        String expired = JsonPath.read(send(owner, post("/api/organizations/" + orgId + "/invitations"), "{\"role\":\"editor\"}")
                .andReturn().getResponse().getContentAsString(), "$.code");
        jdbc.update("UPDATE organization_invitations SET expires_at = now() - interval '1 minute' WHERE revoked_at IS NULL");
        send(other, post("/api/invitations/preview"), "{\"code\":\"%s\"}".formatted(expired))
                .andExpect(jsonPath("$.code").value("INVITATION_INVALID"));
        send(other, post("/api/invitations/preview"), "{\"code\":\"ZZZZ-ZZZZ\"}")
                .andExpect(jsonPath("$.code").value("INVITATION_INVALID"));
    }

    @Test
    void memberLimitIsEnforced() throws Exception {
        // Batas diturunkan menjadi 3 lewat properti test.
        Account owner = register("Rina");
        String orgId = createOsis(owner);
        String code = JsonPath.read(send(owner, post("/api/organizations/" + orgId + "/invitations"), "{\"role\":\"editor\"}")
                .andReturn().getResponse().getContentAsString(), "$.code");
        for (int i = 0; i < 2; i++) {
            send(register("Anggota " + i), post("/api/invitations/accept"), "{\"code\":\"%s\"}".formatted(code))
                    .andExpect(status().isOk());
        }
        send(register("Kelebihan"), post("/api/invitations/accept"), "{\"code\":\"%s\"}".formatted(code))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORG_MEMBER_LIMIT"));
    }

    @Test
    void personalSpaceRules() throws Exception {
        Account user = register("Aris");
        send(user, post("/api/organizations"), "{\"type\":\"personal\",\"name\":\"Ruang pribadi Aris\",\"category\":\"ukm\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("personal"))
                .andExpect(jsonPath("$.category").doesNotExist())
                .andExpect(jsonPath("$.myRole").value("owner"));
        // Skenario 4: ruang pribadi kedua ditolak.
        send(user, post("/api/organizations"), "{\"type\":\"personal\",\"name\":\"Lagi\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PERSONAL_SPACE_EXISTS"));
        String spaceId = JsonPath.read(send(user, get("/api/organizations"), null)
                .andExpect(jsonPath("$", hasSize(1)))
                .andReturn().getResponse().getContentAsString(), "$[0].id");
        send(user, post("/api/organizations/" + spaceId + "/invitations"), "{\"role\":\"editor\"}")
                .andExpect(status().isForbidden());
    }

    @Test
    void categoryMustMatchLevel() throws Exception {
        // Skenario 2: jenjang SMA dengan kategori BEM.
        send(register("Aris"), post("/api/organizations"), """
                {"type":"organization","name":"BEM?","level":"sma","category":"bem_dema_dpm","institutionName":"SMA 1"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CATEGORY_LEVEL_MISMATCH"));
        send(register("Aris"), post("/api/organizations"), """
                {"type":"organization","name":"Tanpa jenjang","category":"pramuka","institutionName":"SMA 1"}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.level").exists());
    }

    @Test
    void categoriesAreServedPublicly() throws Exception {
        mockMvc.perform(get("/api/meta/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.levels", hasSize(4)))
                .andExpect(jsonPath("$.categories[?(@.value == 'bem_dema_dpm')].levels[*]", contains("perguruan_tinggi")))
                .andExpect(jsonPath("$.contentTags", hasSize(9)))
                .andExpect(jsonPath("$.contentTags[?(@.core == true)].value", contains("profil", "struktur", "kontak")));
    }

    @Test
    void profileUpdateNormalizesContacts() throws Exception {
        Account owner = register("Rina");
        String orgId = createOsis(owner);
        send(owner, patch("/api/organizations/" + orgId), """
                {"name":"OSIS SMA Negeri 1 Bandung","level":"sma","category":"osis_mpk","institutionName":"SMAN 1",
                 "whatsapp":"0812-3456-7890","instagram":"https://instagram.com/osis.smansa/","tiktok":"@osis",
                 "youtube":"https://youtube.com/@osis","email":"OSIS@Mail.com"}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.whatsapp").value("6281234567890"))
                .andExpect(jsonPath("$.instagram").value("osis.smansa"))
                .andExpect(jsonPath("$.tiktok").value("osis"))
                .andExpect(jsonPath("$.youtube").value("@osis"))
                .andExpect(jsonPath("$.email").value("osis@mail.com"));
    }

    @Test
    void logoIsCroppedShrunkAndServedPublicly() throws Exception {
        Account owner = register("Rina");
        String orgId = createOsis(owner);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(1200, 800, BufferedImage.TYPE_INT_RGB), "png", png);

        String logoUrl = JsonPath.read(mockMvc.perform(multipart("/api/organizations/" + orgId + "/logo")
                        .file(new MockMultipartFile("file", "logo.png", "image/png", png.toByteArray()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + owner.token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), "$.logoUrl");

        byte[] served = mockMvc.perform(get(logoUrl)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(served));
        assertThat(image.getWidth()).isEqualTo(512);
        assertThat(image.getHeight()).isEqualTo(512);

        // Skenario 12: file bukan gambar ditolak dengan pesan jelas.
        mockMvc.perform(multipart("/api/organizations/" + orgId + "/logo")
                        .file(new MockMultipartFile("file", "logo.png", "image/png", "<svg onload=alert(1)>".getBytes()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + owner.token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LOGO_INVALID"));
    }

    @Test
    void deleteNeedsOwnerAndMatchingName() throws Exception {
        Account owner = register("Rina");
        String orgId = createOsis(owner);
        send(owner, delete("/api/organizations/" + orgId), "{\"confirmName\":\"OSIS lain\"}")
                .andExpect(jsonPath("$.code").value("CONFIRM_NAME_MISMATCH"));
        send(owner, delete("/api/organizations/" + orgId), "{\"confirmName\":\"osis sma negeri 1 bandung\"}")
                .andExpect(status().isNoContent());
        send(owner, get("/api/organizations/" + orgId), null).andExpect(jsonPath("$.code").value("ORG_NOT_FOUND"));
    }

    @Test
    void invitationAttemptsAreRateLimited() throws Exception {
        Account guesser = register("Penebak");
        for (int i = 0; i < AttemptLimiter.MAX_ATTEMPTS; i++) {
            send(guesser, post("/api/invitations/preview"), "{\"code\":\"ZZZZ-ZZZ" + (i % 8 + 2) + "\"}")
                    .andExpect(jsonPath("$.code").value("INVITATION_INVALID"));
        }
        send(guesser, post("/api/invitations/preview"), "{\"code\":\"ZZZZ-ZZZZ\"}")
                .andExpect(status().isTooManyRequests());
    }

    // ---------- pembantu ----------

    private record Account(String id, String token) {
    }

    private Account register(String name) throws Exception {
        String body = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"%s\",\"email\":\"org-%s@mail.com\",\"password\":\"rahasia123\"}"
                                .formatted(name, UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return new Account(JsonPath.read(body, "$.user.id"), JsonPath.read(body, "$.accessToken"));
    }

    private String createOsis(Account owner) throws Exception {
        return JsonPath.read(send(owner, post("/api/organizations"), OSIS)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.myRole").value("owner"))
                .andExpect(jsonPath("$.maxMembers").value(3))
                .andReturn().getResponse().getContentAsString(), "$.id");
    }

    private ResultActions send(Account account, MockHttpServletRequestBuilder request, String json) throws Exception {
        request.header(HttpHeaders.AUTHORIZATION, "Bearer " + account.token);
        if (json != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return mockMvc.perform(request);
    }
}
