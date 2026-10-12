package com.aris.templateapp.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * Data contoh untuk tim portal web (docs/rancangan/portal-web.md), HANYA di profile dev:
 * <ul>
 *   <li>{@code dummy1@templateapp.test} dijadikan admin jika belum ada admin sama sekali (login dashboard).</li>
 *   <li>Kritik &amp; saran contoh dengan berbagai status dan balasan, jika tabel feedback kosong.</li>
 *   <li>Riwayat pengguna aktif &amp; lalu lintas API 30 hari terakhir, jika tabelnya kosong, agar grafik dashboard
 *       langsung terisi. Angkanya acak tetapi tetap (seed tetap), jadi tampilannya sama di setiap laptop.</li>
 * </ul>
 */
@Slf4j
@Component
@Profile("dev")
@Order(5)
@RequiredArgsConstructor
public class PortalDemoSeeder implements ApplicationRunner {

    static final String ADMIN_EMAIL = "dummy1@templateapp.test";

    private record Route(String method, String route, int weight) {
    }

    private static final List<Route> ROUTES = List.of(
            new Route("GET", "/api/users/me", 30),
            new Route("POST", "/api/auth/refresh", 12),
            new Route("GET", "/api/templates", 18),
            new Route("GET", "/api/organizations/{orgId}/websites", 14),
            new Route("GET", "/api/projects/{id}", 10),
            new Route("PUT", "/api/projects/{id}/content", 9),
            new Route("POST", "/api/projects/{id}/lock", 8),
            new Route("POST", "/api/templates/{id}/events", 6),
            new Route("POST", "/api/auth/login", 3),
            new Route("POST", "/api/projects/{id}/assets", 2));

    private final JdbcClient jdbc;
    private final Clock clock;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        makeAdmin();
        seedFeedback();
        seedUsage();
    }

    private void makeAdmin() {
        boolean anyAdmin = jdbc.sql("SELECT EXISTS (SELECT 1 FROM users WHERE is_admin)").query(Boolean.class).single();
        if (!anyAdmin && jdbc.sql("UPDATE users SET is_admin = true WHERE lower(email) = :email")
                .param("email", ADMIN_EMAIL).update() > 0) {
            log.info("Seeder portal: {} dijadikan admin dashboard", ADMIN_EMAIL);
        }
    }

    private void seedFeedback() {
        if (jdbc.sql("SELECT count(*) FROM feedback").query(Long.class).single() > 0) {
            return;
        }
        Optional<UUID> admin = user(1);
        Optional<UUID> creator = user(3);
        Optional<UUID> provider = providerUser();
        if (admin.isEmpty() || creator.isEmpty()) {
            return;
        }
        Instant now = clock.instant();
        feedback(creator.get(), "creator", "bug", "Foto tidak muncul setelah diganti",
                "Saya ganti foto utama di editor, di HP saya muncul tapi di HP teman tidak muncul.", 3, "new",
                now.minus(Duration.ofHours(3)));
        UUID inProgress = feedback(creator.get(), "creator", "saran", "Tambah template untuk acara pensi",
                "Kalau bisa ada template khusus acara pentas seni sekolah, lengkap dengan jadwal dan galeri.", 5,
                "in_progress", now.minus(Duration.ofDays(2)));
        message(inProgress, admin.get(), true, "Terima kasih idenya! Kami teruskan ke para kreator template.",
                now.minus(Duration.ofDays(1)));
        UUID resolved = feedback(creator.get(), "creator", "pertanyaan", "Cara mengundang anggota",
                "Bagaimana cara mengundang pengurus OSIS lain ke ruang kerja?", null, "resolved",
                now.minus(Duration.ofDays(6)));
        message(resolved, admin.get(), true, "Buka Profil → Anggota → Undang, lalu bagikan kodenya. Berlaku 7 hari.",
                now.minus(Duration.ofDays(5)));
        message(resolved, creator.get(), false, "Sudah berhasil, terima kasih!", now.minus(Duration.ofDays(5)));
        provider.ifPresent(id -> feedback(id, "provider", "keluhan", "Pengecekan upload terlalu ketat",
                "Template saya ditolak karena memakai library slider yang belum ada di daftar.", 2, "new",
                now.minus(Duration.ofHours(20))));
        feedback(creator.get(), "creator", "lainnya", "Aplikasinya keren", "Sangat membantu OSIS kami. Semangat!", 5,
                "closed", now.minus(Duration.ofDays(12)));
        log.info("Seeder portal: kritik & saran contoh dibuat");
    }

    private UUID feedback(UUID userId, String role, String category, String subject, String message, Integer rating,
                          String status, Instant at) {
        Timestamp time = Timestamp.from(at);
        return jdbc.sql("""
                        INSERT INTO feedback (user_id, sender_role, category, subject, message, rating, status,
                                              app_version, device_info, staff_unread, last_message_at, created_at,
                                              updated_at)
                        VALUES (:user, :role, :category, :subject, :message, :rating, :status, '1.0.0-dev',
                                'Contoh data seeder', :unread, :time, :time, :time)
                        RETURNING id""")
                .param("user", userId).param("role", role).param("category", category).param("subject", subject)
                .param("message", message).param("rating", rating).param("status", status)
                .param("unread", "new".equals(status)).param("time", time)
                .query(UUID.class).single();
    }

    private void message(UUID feedbackId, UUID authorId, boolean fromStaff, String body, Instant at) {
        Timestamp time = Timestamp.from(at);
        jdbc.sql("""
                        INSERT INTO feedback_messages (feedback_id, author_id, from_staff, body, created_at)
                        VALUES (:feedback, :author, :staff, :body, :time)""")
                .param("feedback", feedbackId).param("author", authorId).param("staff", fromStaff)
                .param("body", body).param("time", time).update();
        jdbc.sql("UPDATE feedback SET last_message_at = :time, user_unread = :staff WHERE id = :id")
                .param("time", time).param("staff", fromStaff).param("id", feedbackId).update();
    }

    private void seedUsage() {
        boolean empty = jdbc.sql("SELECT NOT EXISTS (SELECT 1 FROM user_daily_activity)"
                + " AND NOT EXISTS (SELECT 1 FROM api_traffic_daily)").query(Boolean.class).single();
        if (!empty) {
            return;
        }
        List<UUID> users = jdbc.sql("SELECT id FROM users ORDER BY email").query(UUID.class).list();
        if (users.isEmpty()) {
            return;
        }
        Random random = new Random(42);
        LocalDate today = LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
        for (int ago = 29; ago >= 0; ago--) {
            LocalDate day = today.minusDays(ago);
            Instant noon = day.atTime(12, 0).toInstant(ZoneOffset.UTC);
            int activeUsers = 0;
            for (UUID user : users) {
                if (random.nextInt(100) < 45) {
                    activeUsers++;
                    String platform = random.nextInt(100) < 85 ? "android" : "web";
                    jdbc.sql("""
                                    INSERT INTO user_daily_activity (user_id, day, platform, request_count,
                                                                     first_seen_at, last_seen_at)
                                    VALUES (:user, :day, :platform, :count, :first, :last)""")
                            .param("user", user).param("day", Date.valueOf(day)).param("platform", platform)
                            .param("count", 5 + random.nextInt(80))
                            .param("first", Timestamp.from(noon.minus(Duration.ofHours(3))))
                            .param("last", Timestamp.from(noon.plus(Duration.ofHours(2))))
                            .update();
                }
            }
            for (Route route : ROUTES) {
                long requests = (long) route.weight() * (2 + activeUsers) + random.nextInt(20);
                long errors = random.nextInt(100) < 15 ? 1 + random.nextInt(3) : 0;
                long clientErrors = requests / 40 + random.nextInt(3);
                int avgMs = 20 + random.nextInt(120);
                jdbc.sql("""
                                INSERT INTO api_traffic_daily (day, method, route, request_count, error_count,
                                                               client_error_count, total_ms, max_ms)
                                VALUES (:day, :method, :route, :requests, :errors, :clientErrors, :totalMs, :maxMs)""")
                        .param("day", Date.valueOf(day)).param("method", route.method()).param("route", route.route())
                        .param("requests", requests).param("errors", errors).param("clientErrors", clientErrors)
                        .param("totalMs", requests * avgMs).param("maxMs", avgMs * 6)
                        .update();
            }
        }
        log.info("Seeder portal: riwayat pengguna aktif & lalu lintas API 30 hari dibuat");
    }

    private Optional<UUID> user(int number) {
        return jdbc.sql("SELECT id FROM users WHERE lower(email) = :email")
                .param("email", "dummy" + number + "@templateapp.test")
                .query(UUID.class).optional();
    }

    private Optional<UUID> providerUser() {
        return jdbc.sql("SELECT user_id FROM provider_profiles ORDER BY created_at LIMIT 1").query(UUID.class).optional();
    }
}
