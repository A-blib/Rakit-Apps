package com.aris.templateapp.seed;

import com.aris.templateapp.TestcontainersConfiguration;
import com.aris.templateapp.auth.UserIdentityRepository;
import com.aris.templateapp.notification.NotificationService;
import com.aris.templateapp.provider.ProviderProfileRepository;
import com.aris.templateapp.template.ProviderTemplateQueries;
import com.aris.templateapp.template.TemplateCheckIssueRepository;
import com.aris.templateapp.template.TemplateCheckRepository;
import com.aris.templateapp.template.TemplateRepository;
import com.aris.templateapp.user.CreatorProfileRepository;
import com.aris.templateapp.user.UserRepository;
import com.jayway.jsonpath.JsonPath;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Seeder demo hanya aktif di profile dev dan jika app.seed.demo-templates=true, jadi di sini dibuat manual.
 * Properti berbeda → context & database container tersendiri.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "test.context=demo-seeder")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DemoTemplateSeederTest {

    @Autowired private UserRepository userRepository;
    @Autowired private UserIdentityRepository identityRepository;
    @Autowired private CreatorProfileRepository creatorProfileRepository;
    @Autowired private ProviderProfileRepository providerProfileRepository;
    @Autowired private TemplateRepository templateRepository;
    @Autowired private TemplateCheckRepository checkRepository;
    @Autowired private TemplateCheckIssueRepository issueRepository;
    @Autowired private ProviderTemplateQueries queries;
    @Autowired private NotificationService notificationService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private Clock clock;
    @Autowired private TransactionTemplate transactionTemplate;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private MockMvc mockMvc;

    @Test
    void seedsDemoProviderOnceAndCanBeRemoved() throws Exception {
        DemoTemplateSeeder seeder = new DemoTemplateSeeder(userRepository, identityRepository, creatorProfileRepository,
                providerProfileRepository, templateRepository, checkRepository, issueRepository, queries,
                notificationService, passwordEncoder, clock);
        transactionTemplate.executeWithoutResult(tx -> seeder.run(null));
        transactionTemplate.executeWithoutResult(tx -> seeder.run(null));

        assertThat(jdbc.queryForObject("SELECT count(*) FROM templates", Integer.class)).isEqualTo(7);

        String login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(DemoTemplateSeeder.DEMO_EMAIL,
                                DummyDataSeeder.PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.activeMode").value("provider"))
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(login, "$.accessToken");

        mockMvc.perform(get("/api/providers/me/dashboard?period=30d").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(jsonPath("$.hasTemplates").value(true))
                .andExpect(jsonPath("$.profileComplete").value(true))
                .andExpect(jsonPath("$.summary.active").value(3))
                .andExpect(jsonPath("$.actionItems[*].kind", contains("TEMPLATE_CHECK_FAILED", "TEMPLATE_WARNING", "DRAFT")))
                .andExpect(jsonPath("$.popular[*].name", contains("Profil Sekolah", "UMKM Kuliner", "Portofolio Minimal")));
        mockMvc.perform(get("/api/notifications/unread-count").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(jsonPath("$.count").value(3));

        // Sama dengan DevDemoController: hapus akun demo → semua data demo ikut terhapus (ON DELETE CASCADE).
        transactionTemplate.executeWithoutResult(tx -> userRepository.findByEmailIgnoreCase(DemoTemplateSeeder.DEMO_EMAIL)
                .ifPresent(userRepository::delete));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM templates", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM template_events", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM notifications", Integer.class)).isZero();
    }
}
