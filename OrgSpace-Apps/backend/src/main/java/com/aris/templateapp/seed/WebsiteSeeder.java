package com.aris.templateapp.seed;

import com.aris.templateapp.user.User;
import com.aris.templateapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Website contoh di organisasi dummy (alur-pembuatan-website.md bagian 10), HANYA jika tabel projects kosong:
 * OSIS Smansa berisi semua status (draft, online, online + ada perubahan, dimatikan, ditangguhkan) dan satu website
 * yang sedang dikunci anggota lain; BEM UNU dua website; Pramuka sengaja kosong (state "Belum ada website").
 * <p>
 * Ini data tampilan saja: isinya kosong dan belum ada file hasil terbit. Situs & rilis dimasukkan langsung lewat SQL
 * karena alur terbit sungguhan baru dibangun di fase publish.
 */
@Slf4j
@Component
@Profile("dev")
@Order(4)
@RequiredArgsConstructor
public class WebsiteSeeder implements ApplicationRunner {

    private final JdbcClient jdbc;
    private final UserRepository userRepository;
    private final Clock clock;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (jdbc.sql("SELECT count(*) FROM projects").query(Long.class).single() > 0) {
            log.info("Seeder website dilewati: tabel projects sudah berisi data");
            return;
        }
        Optional<UUID> osis = organization("OSIS SMA Negeri 1 Bandung");
        Optional<UUID> bem = organization("BEM Universitas Nusantara");
        if (osis.isEmpty() || bem.isEmpty()) {
            log.info("Seeder website dilewati: organisasi dummy belum ada");
            return;
        }
        Instant now = clock.instant();
        UUID ketua = user(3);
        UUID sekretaris = user(4);
        UUID humas = user(5);

        UUID web2026 = project(osis.get(), "Website OSIS 2026", ketua, humas, 0, now.minus(Duration.ofHours(2)));
        site(web2026, osis.get(), "osis-smansa", "online", ketua, now.minus(Duration.ofDays(3)));

        UUID profil = project(osis.get(), "Profil OSIS Smansa", sekretaris, sekretaris, 0, now.minus(Duration.ofDays(5)));
        site(profil, osis.get(), "profil-osis-smansa", "online", sekretaris, now.minus(Duration.ofDays(4)));

        UUID pensi = project(osis.get(), "Pensi 2026", humas, humas, 3, now.minus(Duration.ofMinutes(30)));
        // Kunci panjang khusus data contoh, supaya "Sedang diedit oleh ..." bisa dilihat kapan saja.
        jdbc.sql("""
                        INSERT INTO project_locks (project_id, user_id, device_id, expires_at)
                        VALUES (:project, :user, 'seeder', :expires)
                        """)
                .param("project", pensi).param("user", humas)
                .param("expires", Timestamp.from(now.plus(Duration.ofDays(30))))
                .update();

        project(osis.get(), "Lomba Kreativitas", sekretaris, ketua, 5, now.minus(Duration.ofDays(1)));

        UUID arsip = project(osis.get(), "Website OSIS 2025", ketua, ketua, 0, now.minus(Duration.ofDays(40)));
        site(arsip, osis.get(), "osis-smansa-2025", "offline", ketua, now.minus(Duration.ofDays(41)));

        UUID pengumuman = project(osis.get(), "Pengumuman MPLS", sekretaris, sekretaris, 0, now.minus(Duration.ofDays(9)));
        site(pengumuman, osis.get(), "mpls-smansa", "suspended", sekretaris, now.minus(Duration.ofDays(10)));
        jdbc.sql("UPDATE sites SET suspension_reason = 'Contoh data: laporan konten menyesatkan' WHERE project_id = :id")
                .param("id", pengumuman)
                .update();

        UUID bemSite = project(bem.get(), "Website BEM UNU", user(7), user(8), 0, now.minus(Duration.ofDays(2)));
        site(bemSite, bem.get(), "bem-unu", "online", user(7), now.minus(Duration.ofDays(6)));
        project(bem.get(), "Dies Natalis", user(8), user(8), 2, now.minus(Duration.ofHours(20)));

        log.info("Seeder website: 8 website contoh di OSIS Smansa dan BEM UNU");
    }

    /**
     * @param updatedAt waktu "Diubah ... lalu"; isi editor dianggap berubah pada waktu yang sama
     */
    private UUID project(UUID organizationId, String name, UUID createdBy, UUID updatedBy, int missing,
                         Instant updatedAt) {
        Timestamp time = Timestamp.from(updatedAt);
        return jdbc.sql("""
                        INSERT INTO projects (organization_id, name, mode, content_version, content_updated_at,
                                              missing_count, created_by, updated_by, created_at, updated_at)
                        VALUES (:org, :name, 'template', 1, :time, :missing, :created, :updated, :time, :time)
                        RETURNING id
                        """)
                .param("org", organizationId).param("name", name).param("time", time).param("missing", missing)
                .param("created", createdBy).param("updated", updatedBy)
                .query(UUID.class)
                .single();
    }

    /**
     * Situs + satu rilis. Jika rilis dibuat sebelum isi terakhir diubah, website tampil "Online · ada perubahan".
     */
    private void site(UUID projectId, UUID organizationId, String slug, String status, UUID publishedBy,
                      Instant releasedAt) {
        Timestamp time = Timestamp.from(releasedAt);
        UUID siteId = jdbc.sql("""
                        INSERT INTO sites (project_id, organization_id, slug, status, created_at, updated_at, published_at)
                        VALUES (:project, :org, :slug, :status, :time, :time, :time)
                        RETURNING id
                        """)
                .param("project", projectId).param("org", organizationId).param("slug", slug)
                .param("status", status).param("time", time)
                .query(UUID.class)
                .single();
        UUID releaseId = jdbc.sql("""
                        INSERT INTO site_releases (site_id, version, project_snapshot, kind, published_by, created_at)
                        VALUES (:site, 1, '{}'::jsonb, 'publish', :user, :time)
                        RETURNING id
                        """)
                .param("site", siteId).param("user", publishedBy).param("time", time)
                .query(UUID.class)
                .single();
        jdbc.sql("UPDATE sites SET current_release_id = :release WHERE id = :site")
                .param("release", releaseId).param("site", siteId)
                .update();
    }

    private Optional<UUID> organization(String name) {
        return jdbc.sql("SELECT id FROM organizations WHERE name = :name AND type = 'organization'")
                .param("name", name)
                .query(UUID.class)
                .optional();
    }

    private UUID user(int number) {
        return userRepository.findByEmailIgnoreCase("dummy" + number + "@templateapp.test")
                .map(User::getId)
                .orElseThrow();
    }
}
