package com.aris.templateapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Mengecek hasil migrasi V1–V6 langsung lewat SQL, termasuk aturan yang dijaga database
 * (CHECK constraint dan index unik), karena aturan itu tidak terlihat dari kode Java.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
// Setiap test di-rollback, jadi data dari satu test tidak memengaruhi test lain.
@Transactional
class FlywayMigrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void allTablesAreCreated() {
        List<String> tables = jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'", String.class);

        assertThat(tables).contains("users", "user_identities", "refresh_tokens",
                "auth_tickets", "creator_profiles", "provider_profiles");
    }

    @Test
    void newUserGetsDefaultModeAndOnboardingFlag() {
        jdbc.update("INSERT INTO users (display_name, email) VALUES ('Aris', 'aris@mail.com')");

        var row = jdbc.queryForMap("SELECT active_mode, onboarding_completed FROM users WHERE email = 'aris@mail.com'");

        assertThat(row.get("active_mode")).isEqualTo("creator");
        assertThat(row.get("onboarding_completed")).isEqualTo(false);
    }

    @Test
    void emailIsUniqueIgnoringCase() {
        jdbc.update("INSERT INTO users (display_name, email) VALUES ('Aris', 'aris@mail.com')");

        assertThatThrownBy(() -> jdbc.update("INSERT INTO users (display_name, email) VALUES ('Aris 2', 'ARIS@Mail.com')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void unknownActiveModeIsRejected() {
        assertThatThrownBy(() -> jdbc.update("INSERT INTO users (display_name, active_mode) VALUES ('Aris', 'admin')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void socialIdentityCannotHavePassword() {
        jdbc.update("INSERT INTO users (display_name) VALUES ('Aris')");

        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO user_identities (user_id, provider, provider_user_id, password_hash)
                SELECT id, 'google', 'g-123', 'hash' FROM users""")).isInstanceOf(DataIntegrityViolationException.class);
    }
}
