package com.aris.templateapp.seed;

import com.aris.templateapp.TestcontainersConfiguration;
import com.aris.templateapp.auth.UserIdentityRepository;
import com.aris.templateapp.provider.ProviderProfileRepository;
import com.aris.templateapp.user.CreatorProfileRepository;
import com.aris.templateapp.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
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
}
