package com.aris.templateapp.user;

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

import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Onboarding pembuat website & provider, serta perpindahan mode (bagian 6.6). */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OnboardingIntegrationTest {

    private static final String VALID_PROVIDER = """
            {"creatorName":"Studio Aris","bio":"Template sekolah","portfolioUrl":"https://github.com/aris",
             "specialties":["Sekolah","UMKM"],"agreedToTerms":true}""";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    private String email;
    private String token;

    @BeforeEach
    void registerFreshUser() throws Exception {
        email = "user-" + UUID.randomUUID() + "@mail.com";
        String body = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Aris\",\"email\":\"%s\",\"password\":\"rahasia123\"}".formatted(email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        token = JsonPath.read(body, "$.accessToken");
    }

    @Test
    void creatorOnboardingNeedsWorkspaceFirst() throws Exception {
        // Pembuat website selalu bekerja di ruang kerja (revisi-fondasi-orgspace.md bagian 5).
        send(post("/api/users/me/onboarding/creator"), "{\"displayName\":\"Aris\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ONBOARDING_NEEDS_SPACE"));
    }

    @Test
    void creatorOnboardingCompletesAfterCreatingOrganization() throws Exception {
        String orgId = JsonPath.read(send(post("/api/organizations"), """
                {"type":"organization","name":"OSIS SMA Negeri 1","shortName":"OSIS Smansa","level":"sma",
                 "category":"osis_mpk","institutionName":"SMA Negeri 1","positionTitle":"Ketua"}""")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.id");

        send(post("/api/users/me/onboarding/creator"), "{\"displayName\":\"Aris Muslimin\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Aris Muslimin"))
                .andExpect(jsonPath("$.onboardingCompleted").value(true))
                .andExpect(jsonPath("$.activeMode").value("creator"))
                .andExpect(jsonPath("$.roles", contains("creator")))
                .andExpect(jsonPath("$.lastOrganizationId").value(orgId))
                .andExpect(jsonPath("$.creatorProfile").doesNotExist());
    }

    @Test
    void personalSpaceIsEnoughForOnboarding() throws Exception {
        createPersonalSpace();
        send(post("/api/users/me/onboarding/creator"), "{\"displayName\":\"Aris\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onboardingCompleted").value(true));
    }

    @Test
    void providerOnboardingIsActiveImmediatelyInProviderMode() throws Exception {
        // Dari menu "Jadi penyedia template", tanpa memilih peran; provider langsung aktif (tanpa verifikasi).
        send(post("/api/users/me/onboarding/provider"), VALID_PROVIDER)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeMode").value("provider"))
                .andExpect(jsonPath("$.onboardingCompleted").value(true))
                .andExpect(jsonPath("$.providerStatus").value("active"))
                .andExpect(jsonPath("$.roles", contains("provider")));
    }

    @Test
    void providerCanOnlyRegisterOnce() throws Exception {
        send(post("/api/users/me/onboarding/provider"), VALID_PROVIDER).andExpect(status().isOk());

        send(post("/api/users/me/onboarding/provider"), VALID_PROVIDER)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PROVIDER_PROFILE_EXISTS"));
    }

    @Test
    void providerFormIsValidated() throws Exception {
        send(post("/api/users/me/onboarding/provider"), """
                {"creatorName":"","bio":"%s","portfolioUrl":"github.com/aris","agreedToTerms":false}""".formatted("x".repeat(301)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.creatorName").exists())
                .andExpect(jsonPath("$.fieldErrors.bio").value("Bio maksimal 300 karakter"))
                .andExpect(jsonPath("$.fieldErrors.portfolioUrl").exists())
                .andExpect(jsonPath("$.fieldErrors.agreedToTerms").value("Kamu harus menyetujui aturan provider"));
    }

    @Test
    void switchingModesWithBothRoles() throws Exception {
        createPersonalSpace();
        send(post("/api/users/me/onboarding/creator"), "{\"displayName\":\"Aris\"}");
        send(post("/api/users/me/onboarding/provider"), VALID_PROVIDER)
                .andExpect(jsonPath("$.roles", containsInAnyOrder("creator", "provider")));

        changeMode("creator").andExpect(status().isOk()).andExpect(jsonPath("$.activeMode").value("creator"));
        changeMode("provider").andExpect(status().isOk()).andExpect(jsonPath("$.activeMode").value("provider"));

        // Skenario 10: mode terakhir tersimpan di server, jadi ikut terbaca saat app dibuka lagi.
        mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(jsonPath("$.activeMode").value("provider"));
    }

    @Test
    void providerModeNeedsProviderProfile() throws Exception {
        changeMode("provider")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("MODE_NOT_ALLOWED"));
    }

    @Test
    void suspendedProviderCannotOpenProviderMode() throws Exception {
        send(post("/api/users/me/onboarding/provider"), VALID_PROVIDER);
        // Skenario 11: status diubah manual lewat SQL (belum ada panel admin).
        jdbc.update("""
                UPDATE provider_profiles SET status = 'suspended'
                WHERE user_id = (SELECT id FROM users WHERE email = ?)""", email);

        changeMode("provider")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("MODE_NOT_ALLOWED"));
        changeMode("creator").andExpect(status().isOk());
    }

    @Test
    void unknownModeIsRejected() throws Exception {
        changeMode("admin")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void onboardingNeedsLogin() throws Exception {
        mockMvc.perform(post("/api/users/me/onboarding/creator").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Aris\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void avatarCanBeUploadedServedPubliclyAndRemoved() throws Exception {
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(900, 600, BufferedImage.TYPE_INT_RGB), "png", png);

        String avatarUrl = JsonPath.read(mockMvc.perform(multipart("/api/users/me/avatar")
                        .file(new MockMultipartFile("file", "avatar.png", "image/png", png.toByteArray()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), "$.avatarUrl");
        assertThat(avatarUrl).startsWith("/api/users/").contains("/avatar?v=");

        // Publik (tanpa token), dipotong persegi dan diperkecil maks 512 px.
        byte[] served = mockMvc.perform(get(avatarUrl)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(served));
        assertThat(image.getWidth()).isEqualTo(512);
        assertThat(image.getHeight()).isEqualTo(512);

        mockMvc.perform(multipart("/api/users/me/avatar")
                        .file(new MockMultipartFile("file", "avatar.png", "image/png", "<svg onload=alert(1)>".getBytes()))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AVATAR_INVALID"));

        send(delete("/api/users/me/avatar"), "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").doesNotExist());
        mockMvc.perform(get(avatarUrl)).andExpect(status().isNotFound());
    }

    private void createPersonalSpace() throws Exception {
        send(post("/api/organizations"), "{\"type\":\"personal\",\"name\":\"Ruang pribadi Aris\"}")
                .andExpect(status().isCreated());
    }

    private ResultActions changeMode(String mode) throws Exception {
        return send(patch("/api/users/me/active-mode"), "{\"mode\":\"%s\"}".formatted(mode));
    }

    private ResultActions send(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
                               String json) throws Exception {
        return mockMvc.perform(request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
