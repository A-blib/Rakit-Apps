package com.aris.templateapp.portal;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.common.image.SquareImages;
import com.aris.templateapp.portal.dto.PortalUpdateRequest;
import com.aris.templateapp.portal.dto.PortalUpdateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Info terbaru OrgSpace untuk portal web: ditulis tim internal, ringkasannya tampil di landing page. */
@Service
public class PortalUpdateService {

    /** Kartu di landing: cukup sedikit, sisanya tetap bisa dibuka lewat link detail. */
    static final int PUBLIC_MAX = 12;
    static final long IMAGE_MAX_BYTES = LandingHeroService.IMAGE_MAX_BYTES;
    static final int IMAGE_MAX_SIDE_PX = LandingHeroService.IMAGE_MAX_SIDE_PX;

    private static final String EXPIRES_AT = "published_at + make_interval(days => display_days)";

    /** Kolom daftar tanpa {@code content}, agar landing tidak memuat isi panjang. */
    private static final String EXPIRED = "COALESCE(" + EXPIRES_AT + " <= :now, false) AS expired";
    private static final String SUMMARY_COLUMNS = "id, category, status, title, summary, NULL AS content, image_alt, "
            + "image_updated_at, published_at, display_days, " + EXPIRES_AT + " AS expires_at, " + EXPIRED + ", updated_at";
    private static final String DETAIL_COLUMNS = "*, " + EXPIRES_AT + " AS expires_at, " + EXPIRED;

    /** Yang boleh dilihat publik: tayang dan belum lewat masa tampilnya. */
    private static final String VISIBLE = "status = 'published' AND (display_days IS NULL OR " + EXPIRES_AT + " > :now)";

    private final JdbcClient jdbc;
    private final Clock clock;
    private final Path imageDir;

    public PortalUpdateService(JdbcClient jdbc, Clock clock,
                               @Value("${app.landing.image-dir:./data/landing}") String landingDir) {
        this.jdbc = jdbc;
        this.clock = clock;
        this.imageDir = Path.of(landingDir).toAbsolutePath().normalize().resolve("updates");
    }

    @Transactional(readOnly = true)
    public List<PortalUpdateResponse> listPublished(int limit) {
        return jdbc.sql("SELECT " + SUMMARY_COLUMNS + " FROM portal_updates WHERE " + VISIBLE
                        + " ORDER BY published_at DESC, id LIMIT :limit")
                .param("limit", Math.clamp(limit, 1, PUBLIC_MAX)).param("now", Timestamp.from(clock.instant()))
                .query(PortalUpdateService::map).list();
    }

    @Transactional(readOnly = true)
    public PortalUpdateResponse getPublished(UUID id) {
        return jdbc.sql("SELECT " + DETAIL_COLUMNS + " FROM portal_updates WHERE id = :id AND " + VISIBLE)
                .param("id", id).param("now", Timestamp.from(clock.instant())).query(PortalUpdateService::map).optional()
                .orElseThrow(() -> new ApiException(ErrorCode.UPDATE_NOT_FOUND));
    }

    /** Semua info (draft & tayang) untuk dashboard, yang terakhir diubah dulu. */
    // shortcut: tanpa halaman (pagination); jumlah info diperkirakan puluhan, tambah page/size jika sudah ratusan.
    @Transactional(readOnly = true)
    public List<PortalUpdateResponse> listAll() {
        return jdbc.sql("SELECT " + SUMMARY_COLUMNS + " FROM portal_updates ORDER BY updated_at DESC, id")
                .param("now", Timestamp.from(clock.instant())).query(PortalUpdateService::map).list();
    }

    @Transactional(readOnly = true)
    public PortalUpdateResponse get(UUID id) {
        return jdbc.sql("SELECT " + DETAIL_COLUMNS + " FROM portal_updates WHERE id = :id")
                .param("id", id).param("now", Timestamp.from(clock.instant())).query(PortalUpdateService::map).optional()
                .orElseThrow(() -> new ApiException(ErrorCode.UPDATE_NOT_FOUND));
    }

    @Transactional
    public PortalUpdateResponse create(UUID adminId, PortalUpdateRequest request) {
        Timestamp now = Timestamp.from(clock.instant());
        UUID id = jdbc.sql("""
                        INSERT INTO portal_updates (category, title, summary, content, status, image_alt, display_days,
                                                    published_at, created_by, updated_by, created_at, updated_at)
                        VALUES (:category, :title, :summary, :content, :status, :imageAlt, :displayDays,
                                CASE WHEN :published THEN CAST(:now AS timestamptz) END, :admin, :admin, :now, :now)
                        RETURNING id""")
                .params(params(request)).param("published", request.published())
                .param("admin", adminId).param("now", now)
                .query(UUID.class).single();
        return get(id);
    }

    @Transactional
    public PortalUpdateResponse update(UUID adminId, UUID id, PortalUpdateRequest request) {
        int changed = jdbc.sql("""
                        UPDATE portal_updates SET category = :category, title = :title, summary = :summary,
                               content = :content, status = :status, image_alt = :imageAlt, display_days = :displayDays,
                               published_at = CASE WHEN :published THEN COALESCE(published_at, :now) ELSE published_at END,
                               updated_by = :admin, updated_at = :now
                         WHERE id = :id""")
                .params(params(request)).param("published", request.published())
                .param("admin", adminId).param("now", Timestamp.from(clock.instant())).param("id", id)
                .update();
        if (changed == 0) {
            throw new ApiException(ErrorCode.UPDATE_NOT_FOUND);
        }
        return get(id);
    }

    /** Tampilkan/sembunyikan dari landing tanpa mengubah isi. Tanggal tayang pertama tetap. */
    @Transactional
    public PortalUpdateResponse setPublished(UUID adminId, UUID id, boolean published) {
        int changed = jdbc.sql("""
                        UPDATE portal_updates SET status = :status,
                               published_at = CASE WHEN :published THEN COALESCE(published_at, :now) ELSE published_at END,
                               updated_by = :admin, updated_at = :now
                         WHERE id = :id""")
                .param("status", published ? "published" : "draft").param("published", published)
                .param("admin", adminId).param("now", Timestamp.from(clock.instant())).param("id", id)
                .update();
        if (changed == 0) {
            throw new ApiException(ErrorCode.UPDATE_NOT_FOUND);
        }
        return get(id);
    }

    @Transactional
    public void delete(UUID id) {
        if (jdbc.sql("DELETE FROM portal_updates WHERE id = :id").param("id", id).update() == 0) {
            throw new ApiException(ErrorCode.UPDATE_NOT_FOUND);
        }
        deleteFile(id);
    }

    /** Gambar ditulis ulang sebagai PNG (isi tersembunyi ikut terbuang, lihat {@link SquareImages}), maks 1600 px. */
    @Transactional
    public PortalUpdateResponse uploadImage(UUID adminId, UUID id, byte[] bytes) {
        get(id);
        var source = SquareImages.decode(bytes, IMAGE_MAX_BYTES, ErrorCode.UPDATE_IMAGE_INVALID,
                "Gambar info maksimal " + (IMAGE_MAX_BYTES / 1024 / 1024) + " MB. Pilih gambar yang lebih kecil.");
        SquareImages.writePng(SquareImages.shrink(source, IMAGE_MAX_SIDE_PX), imagePath(id));
        return touchImage(adminId, id, clock.instant());
    }

    @Transactional
    public PortalUpdateResponse deleteImage(UUID adminId, UUID id) {
        get(id);
        deleteFile(id);
        return touchImage(adminId, id, null);
    }

    /** Gambar info yang tampil di publik; draft dan yang sudah lewat masanya tidak punya gambar publik. */
    public Path publishedImage(UUID id) {
        Path image = imagePath(id);
        if (getPublished(id).imageUpdatedAt() == null || !Files.isRegularFile(image)) {
            throw new ApiException(ErrorCode.NOT_FOUND);
        }
        return image;
    }

    private PortalUpdateResponse touchImage(UUID adminId, UUID id, Instant imageUpdatedAt) {
        jdbc.sql("UPDATE portal_updates SET image_updated_at = :image, updated_by = :admin, updated_at = :now WHERE id = :id")
                .param("image", imageUpdatedAt == null ? null : Timestamp.from(imageUpdatedAt))
                .param("admin", adminId).param("now", Timestamp.from(clock.instant())).param("id", id)
                .update();
        return get(id);
    }

    private Path imagePath(UUID id) {
        return imageDir.resolve(id + ".png");
    }

    private void deleteFile(UUID id) {
        try {
            Files.deleteIfExists(imagePath(id));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Map<String, Object> params(PortalUpdateRequest request) {
        String alt = request.imageAlt() == null || request.imageAlt().isBlank() ? null : request.imageAlt().trim();
        Map<String, Object> params = new HashMap<>();
        params.put("category", request.category());
        params.put("title", request.title().trim());
        params.put("summary", request.summary().trim());
        params.put("content", request.content().strip());
        params.put("status", request.published() ? "published" : "draft");
        params.put("imageAlt", alt);
        params.put("displayDays", request.displayDays());
        return params;
    }

    private static PortalUpdateResponse map(ResultSet rs, int row) throws SQLException {
        return new PortalUpdateResponse(rs.getObject("id", UUID.class), rs.getString("category"), rs.getString("status"),
                rs.getString("title"), rs.getString("summary"), rs.getString("content"), rs.getString("image_alt"),
                instant(rs.getTimestamp("image_updated_at")), instant(rs.getTimestamp("published_at")),
                rs.getObject("display_days", Integer.class), instant(rs.getTimestamp("expires_at")),
                rs.getBoolean("expired"), instant(rs.getTimestamp("updated_at")));
    }

    private static Instant instant(Timestamp value) {
        return value == null ? null : value.toInstant();
    }
}
