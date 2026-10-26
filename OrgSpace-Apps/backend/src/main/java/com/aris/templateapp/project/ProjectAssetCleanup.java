package com.aris.templateapp.project;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Pembersih foto website yang tidak dipakai lagi ("foto yatim"), berjalan tiap malam pukul 03.30 (setengah jam
 * setelah UploadMaintenance agar tidak berbarengan). Foto lama tidak pernah dihapus saat user menggantinya, padahal
 * ikut dihitung kuota penyimpanan ruang kerja.
 * <ol>
 *   <li>Baris {@code project_assets} dihapus jika tidak dirujuk di mana pun (lihat {@link #ORPHAN_ROWS}).</li>
 *   <li>File di disk yang tidak dirujuk baris mana pun dihapus. Satu file bisa dipakai beberapa baris (foto sama di
 *       beberapa website satu organisasi), jadi file baru hilang setelah baris terakhirnya hilang.</li>
 * </ol>
 * Keduanya memberi jeda {@link #GRACE}: foto diunggah lebih dulu daripada isi yang merujuknya disimpan (editor,
 * Portal), jadi foto yang baru diunggah belum boleh dianggap yatim.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectAssetCleanup {

    static final Duration GRACE = Duration.ofHours(24);

    /**
     * Semua tempat yang bisa merujuk sebuah foto. Rujukan di teks berbentuk {@code asset:{id}} (isi project, cadangan,
     * snapshot rilis) atau {@code /{slug}/_media/{id}} (Portal: koleksi, berita); keduanya memuat id-nya.
     * Wajib diperbarui jika ada tempat baru yang menyimpan id/URL foto, kalau tidak foto yang masih tampil ikut terhapus.
     * <p>
     * shortcut: rujukan dicari dengan LIKE pada teks JSON per foto (tanpa indeks); cukup untuk ribuan foto, ganti
     * dengan tabel rujukan jika job ini mulai lambat.
     */
    static final String ORPHAN_ROWS = """
            DELETE FROM project_assets a
             WHERE a.created_at < :cutoff
               AND NOT EXISTS (SELECT 1 FROM projects p
                                WHERE p.thumbnail_asset_id = a.id
                                   OR (p.id = a.project_id AND p.content::text LIKE '%' || a.id || '%'))
               AND NOT EXISTS (SELECT 1 FROM sites s WHERE s.favicon_asset_id = a.id OR s.share_image_asset_id = a.id)
               AND NOT EXISTS (SELECT 1 FROM project_content_backups b
                                WHERE b.project_id = a.project_id AND b.content::text LIKE '%' || a.id || '%')
               AND NOT EXISTS (SELECT 1 FROM sites s JOIN site_releases r ON r.site_id = s.id
                                WHERE s.project_id = a.project_id AND r.project_snapshot::text LIKE '%' || a.id || '%')
               AND NOT EXISTS (SELECT 1 FROM sites s JOIN site_custom_items i ON i.site_id = s.id
                                WHERE s.project_id = a.project_id AND i.data::text LIKE '%' || a.id || '%')
               AND NOT EXISTS (SELECT 1 FROM sites s JOIN site_news n ON n.organization_id = s.organization_id
                                WHERE s.project_id = a.project_id
                                  AND (coalesce(n.image_url, '') || ' ' || coalesce(n.content, '')) LIKE '%' || a.id || '%')
            """;

    private final JdbcClient jdbc;
    private final ProjectAssetStorage storage;
    private final Clock clock;

    public record Result(int rows, int files) {
    }

    @Scheduled(cron = "0 30 3 * * *", zone = "${app.timezone}")
    public void nightly() {
        Result result = run();
        log.info("Pembersihan foto yatim: {} baris dan {} file dihapus", result.rows(), result.files());
    }

    public Result run() {
        Instant cutoff = clock.instant().minus(GRACE);
        // Satu pernyataan DELETE: syarat "tidak dirujuk" diperiksa saat baris dihapus, bukan dari daftar yang dibaca lebih dulu.
        int rows = jdbc.sql(ORPHAN_ROWS).param("cutoff", Timestamp.from(cutoff)).update();
        Set<String> used = new HashSet<>(jdbc.sql("SELECT DISTINCT storage_path FROM project_assets").query(String.class).list());
        int files = storage.deleteFilesNotIn(used, cutoff);
        return new Result(rows, files);
    }
}
