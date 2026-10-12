package com.aris.templateapp.seed;

import com.aris.templateapp.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import com.aris.templateapp.auth.UserIdentityRepository;
import com.aris.templateapp.organization.ActivityService;
import com.aris.templateapp.organization.OrganizationInvitationRepository;
import com.aris.templateapp.organization.OrganizationMemberRepository;
import com.aris.templateapp.organization.OrganizationRepository;
import com.aris.templateapp.provider.ProviderProfileRepository;
import com.aris.templateapp.security.TokenGenerator;
import com.aris.templateapp.user.CreatorProfileRepository;
import com.aris.templateapp.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Seeder hanya aktif di profile dev, jadi di test ia dibuat manual lalu dijalankan di dalam transaksi.
 * {@code properties} yang berbeda membuat Spring menyiapkan context (dan database container) tersendiri,
 * sehingga tabel users dijamin kosong di awal test ini.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "test.context=seeder")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DummyDataSeederTest {

    @Autowired private UserRepository userRepository;
    @Autowired private UserIdentityRepository identityRepository;
    @Autowired private CreatorProfileRepository creatorProfileRepository;
    @Autowired private ProviderProfileRepository providerProfileRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private Clock clock;
    @Autowired private TransactionTemplate transactionTemplate;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private MockMvc mockMvc;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMemberRepository memberRepository;
    @Autowired private OrganizationInvitationRepository invitationRepository;
    @Autowired private ActivityService activityService;
    @Autowired private TokenGenerator tokenGenerator;

    @Test
    void seedsOnceWithLoginableUsersAndProviders() throws Exception {
        DummyDataSeeder seeder = new DummyDataSeeder(userRepository, identityRepository, creatorProfileRepository,
                providerProfileRepository, passwordEncoder, clock);

        transactionTemplate.executeWithoutResult(tx -> seeder.run(null));
        // Dijalankan kedua kali: tidak boleh menambah data karena tabel users sudah berisi.
        transactionTemplate.executeWithoutResult(tx -> seeder.run(null));

        assertThat(userRepository.count()).isEqualTo(DummyDataSeeder.USER_COUNT);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM creator_profiles", Integer.class)).isEqualTo(8);
        List<String> statuses = jdbc.queryForList("SELECT status FROM provider_profiles ORDER BY status", String.class);
        assertThat(statuses).containsExactly("active", "active", "suspended");

        // User dummy bisa login dengan password dari README.
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"dummy1@templateapp.test\",\"password\":\"%s\"}".formatted(DummyDataSeeder.PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.onboardingCompleted").value(false));
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"dummy9@templateapp.test\",\"password\":\"%s\"}".formatted(DummyDataSeeder.PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.activeMode").value("provider"))
                .andExpect(jsonPath("$.user.providerStatus").value("active"));
        // dummy10 ditangguhkan: app mengunci mode provider dan mengarahkan ke mode pembuat website.
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"dummy10@templateapp.test\",\"password\":\"%s\"}".formatted(DummyDataSeeder.PASSWORD)))
                .andExpect(jsonPath("$.user.providerStatus").value("suspended"));
    }

    @Test
    void organizationSeederFillsWorkspacesWithWorkingInviteCodes() throws Exception {
        DummyDataSeeder users = new DummyDataSeeder(userRepository, identityRepository, creatorProfileRepository,
                providerProfileRepository, passwordEncoder, clock);
        OrganizationSeeder organizationSeeder = new OrganizationSeeder(userRepository, organizationRepository,
                memberRepository, invitationRepository, activityService, tokenGenerator, clock);
        transactionTemplate.executeWithoutResult(tx -> users.run(null));
        transactionTemplate.executeWithoutResult(tx -> organizationSeeder.run(null));
        transactionTemplate.executeWithoutResult(tx -> organizationSeeder.run(null));

        assertThat(jdbc.queryForList("SELECT type FROM organizations ORDER BY type", String.class))
                .containsExactly("organization", "organization", "organization", "personal", "personal");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM organization_invitations", Integer.class)).isEqualTo(3);

        String token = JsonPath.read(mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"dummy1@templateapp.test\",\"password\":\"%s\"}".formatted(DummyDataSeeder.PASSWORD)))
                .andReturn().getResponse().getContentAsString(), "$.accessToken");
        mockMvc.perform(post("/api/invitations/preview").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"%s\"}".formatted(OrganizationSeeder.OSIS_INVITE_CODE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationName").value("OSIS SMA Negeri 1 Bandung"));
        mockMvc.perform(post("/api/invitations/preview").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"%s\"}".formatted(OrganizationSeeder.EXPIRED_INVITE_CODE)))
                .andExpect(jsonPath("$.code").value("INVITATION_INVALID"));
    }
}
