package com.aris.templateapp.template;

import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.provider.StatsPeriod;
import com.aris.templateapp.provider.dto.ActionItemResponse;
import com.aris.templateapp.provider.dto.DailyCountResponse;
import com.aris.templateapp.provider.dto.PopularTemplateResponse;
import com.aris.templateapp.template.dto.StatusCountsResponse;
import com.aris.templateapp.template.dto.TemplateSummaryResponse;
import com.aris.templateapp.user.WebsitePurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Query agregat untuk Dashboard Provider: ringkasan, grafik per hari, template populer, daftar dengan filter.
 * <p>
 * Ditulis sebagai SQL biasa (JdbcClient) karena isinya menghitung dan mengelompokkan banyak baris
 * (COUNT, GROUP BY, generate_series) yang jauh lebih jelas di SQL daripada lewat Entity JPA.
 * Bagian SQL yang disambung hanya berasal dari daftar tetap (enum di bawah); nilai dari user selalu
 * dikirim sebagai parameter, jadi aman dari SQL injection.
 */
@Repository
@RequiredArgsConstructor
public class ProviderTemplateQueries {

    /** Filter status di "Template Anda" (bagian 6.3). */
    public enum StatusFilter {
        ALL("all", "TRUE"),
        PUBLISHED("published", "t.status = 'published'"),
        NEEDS_FIX("needs_fix", "(t.status = 'check_failed' OR (t.status = 'published' AND t.warning_count > 0))"),
        CHECKING("checking", "t.status = 'checking'"),
        DRAFT("draft", "t.status = 'draft'"),
        DISABLED("disabled", "t.status = 'disabled'");

        private final String value;
        final String condition;

        StatusFilter(String value, String condition) {
            this.value = value;
            this.condition = condition;
        }

        public static StatusFilter from(String value) {
            for (StatusFilter filter : values()) {
                if (filter.value.equals(value)) {
                    return filter;
                }
            }
            throw new IllegalArgumentException("Filter status tidak dikenal: " + value);
        }
    }

    /** Urutan daftar (bagian 6.3). Urutan kedua menjaga hasil tetap stabil antarhalaman. */
    public enum SortOrder {
        UPDATED("updated", "t.updated_at DESC, t.id"),
        DOWNLOADS("downloads", "downloads DESC, t.updated_at DESC, t.id"),
        VIEWS("views", "views DESC, t.updated_at DESC, t.id"),
        NAME("name", "lower(t.name) ASC, t.id");

        private final String value;
        final String orderBy;

        SortOrder(String value, String orderBy) {
            this.value = value;
            this.orderBy = orderBy;
        }

        public static SortOrder from(String value) {
            for (SortOrder order : values()) {
                if (order.value.equals(value)) {
                    return order;
                }
            }
            throw new IllegalArgumentException("Urutan tidak dikenal: " + value);
        }
    }

    /** Jumlah dilihat & didownload per template (total sejak awal) + jumlah error pengecekan terakhir. */
    private static final String TEMPLATE_WITH_COUNTS = """
            SELECT t.id, t.name, t.category, t.thumbnail_url, t.status, t.warning_count, t.updated_at,
                   COALESCE(ev.views, 0)     AS views,
                   COALESCE(ev.downloads, 0) AS downloads,
                   (SELECT count(*) FROM template_check_issues i
                      JOIN template_checks c ON c.id = i.check_id
                     WHERE c.template_id = t.id AND i.severity = 'error'
                       AND c.version = (SELECT max(c2.version) FROM template_checks c2 WHERE c2.template_id = t.id)
                   ) AS error_count
              FROM templates t
              LEFT JOIN (SELECT template_id,
                                count(*) FILTER (WHERE type = 'view')     AS views,
                                count(*) FILTER (WHERE type = 'download') AS downloads
                           FROM template_events GROUP BY template_id) ev ON ev.template_id = t.id
            """;

    private final JdbcClient jdbc;

    // ---------- Template Anda ----------

    public List<TemplateSummaryResponse> list(UUID providerId, StatusFilter status, WebsitePurpose category,
                                              String search, SortOrder sort, int page, int size) {
        String sql = TEMPLATE_WITH_COUNTS
                + " WHERE t.provider_id = :providerId AND " + status.condition + commonFilters(category, search)
                + " ORDER BY " + sort.orderBy + " LIMIT :limit OFFSET :offset";
        return withCommonParams(jdbc.sql(sql), providerId, category, search)
                .param("limit", size)
                .param("offset", page * size)
                .query((rs, row) -> new TemplateSummaryResponse(
                        rs.getObject("id", UUID.class),
                        rs.getString("name"),
                        PersistableEnum.fromValue(WebsitePurpose.class, rs.getString("category")),
                        rs.getString("thumbnail_url"),
                        PersistableEnum.fromValue(TemplateStatus.class, rs.getString("status")),
                        rs.getInt("error_count"),
                        rs.getInt("warning_count"),
                        rs.getLong("views"),
                        rs.getLong("downloads"),
                        rs.getTimestamp("updated_at").toInstant()))
                .list();
    }

    /** Jumlah per chip status. Filter kategori & pencarian ikut berlaku, filter status tidak (agar semua chip terisi). */
    public StatusCountsResponse countByStatus(UUID providerId, WebsitePurpose category, String search) {
        String sql = """
                SELECT count(*) AS all_count,
                       count(*) FILTER (WHERE %s) AS published,
                       count(*) FILTER (WHERE %s) AS needs_fix,
                       count(*) FILTER (WHERE %s) AS checking,
                       count(*) FILTER (WHERE %s) AS draft,
                       count(*) FILTER (WHERE %s) AS disabled
                  FROM templates t
                 WHERE t.provider_id = :providerId
                """.formatted(StatusFilter.PUBLISHED.condition, StatusFilter.NEEDS_FIX.condition,
                StatusFilter.CHECKING.condition, StatusFilter.DRAFT.condition, StatusFilter.DISABLED.condition)
                + commonFilters(category, search);
        return withCommonParams(jdbc.sql(sql), providerId, category, search)
                .query((rs, row) -> new StatusCountsResponse(rs.getLong("all_count"), rs.getLong("published"),
                        rs.getLong("needs_fix"), rs.getLong("checking"), rs.getLong("draft"), rs.getLong("disabled")))
                .single();
    }

    /** Total dilihat & didownload satu template (untuk layar detail): [views, downloads]. */
    public long[] totals(UUID templateId) {
        return jdbc.sql("""
                        SELECT count(*) FILTER (WHERE type = 'view') AS views,
                               count(*) FILTER (WHERE type = 'download') AS downloads
                          FROM template_events WHERE template_id = :templateId""")
                .param("templateId", templateId)
                .query((rs, row) -> new long[]{rs.getLong("views"), rs.getLong("downloads")})
                .single();
    }

    // ---------- Beranda ----------

    public long countPublished(UUID providerId) {
        return jdbc.sql("SELECT count(*) FROM templates WHERE provider_id = :providerId AND status = 'published'")
                .param("providerId", providerId)
                .query(Long.class).single();
    }

    /** Jumlah event jenis tertentu untuk semua template milik provider sejak {@code from}. */
    public long countEvents(UUID providerId, TemplateEventType type, Instant from) {
        return jdbc.sql("""
                        SELECT count(*) FROM template_events e JOIN templates t ON t.id = e.template_id
                         WHERE t.provider_id = :providerId AND e.type = :type AND e.occurred_at >= :from""")
                .param("providerId", providerId)
                .param("type", type.value())
                .param("from", Timestamp.from(from))
                .query(Long.class).single();
    }

    /**
     * Download per hari selama periode. {@code generate_series} membuat satu baris untuk SETIAP tanggal,
     * lalu digabung dengan hitungan; tanggal tanpa download menjadi 0 (bukan dilewati), agar bentuk tren jujur.
     */
    public List<DailyCountResponse> downloadTrend(UUID providerId, StatsPeriod period, ZoneId zone) {
        return jdbc.sql("""
                        SELECT d::date AS day, COALESCE(c.cnt, 0) AS cnt
                          FROM generate_series(CAST(:firstDay AS date), CAST(:lastDay AS date), interval '1 day') d
                          LEFT JOIN (SELECT (e.occurred_at AT TIME ZONE :zone)::date AS day, count(*) AS cnt
                                       FROM template_events e JOIN templates t ON t.id = e.template_id
                                      WHERE t.provider_id = :providerId AND e.type = 'download'
                                        AND e.occurred_at >= :from
                                      GROUP BY 1) c ON c.day = d::date
                         ORDER BY 1""")
                .param("firstDay", Date.valueOf(period.firstDay()))
                .param("lastDay", Date.valueOf(period.lastDay()))
                .param("zone", zone.getId())
                .param("providerId", providerId)
                .param("from", Timestamp.from(period.from()))
                .query((rs, row) -> new DailyCountResponse(rs.getDate("day").toLocalDate(), rs.getLong("cnt")))
                .list();
    }

    /**
     * Template populer (bagian 3.8): hanya yang tayang, punya download di periode, diurutkan berdasarkan
     * download lalu dilihat.
     */
    public List<PopularTemplateResponse> popular(UUID providerId, Instant from, int limit) {
        return jdbc.sql("""
                        SELECT t.id, t.name, t.thumbnail_url,
                               count(*) FILTER (WHERE e.type = 'download') AS downloads,
                               count(*) FILTER (WHERE e.type = 'view')     AS views
                          FROM templates t JOIN template_events e ON e.template_id = t.id AND e.occurred_at >= :from
                         WHERE t.provider_id = :providerId AND t.status = 'published'
                         GROUP BY t.id, t.name, t.thumbnail_url
                        HAVING count(*) FILTER (WHERE e.type = 'download') > 0
                         ORDER BY downloads DESC, views DESC, lower(t.name)
                         LIMIT :limit""")
                .param("from", Timestamp.from(from))
                .param("providerId", providerId)
                .param("limit", limit)
                .query((rs, row) -> new PopularTemplateResponse(row + 1, rs.getObject("id", UUID.class),
                        rs.getString("name"), rs.getString("thumbnail_url"),
                        rs.getLong("downloads"), rs.getLong("views")))
                .list();
    }

    /** Kartu "Perlu tindakan" dari template: tidak lolos, tayang dengan peringatan, dan draft. */
    public List<ActionItemResponse> templateActionItems(UUID providerId) {
        return jdbc.sql(TEMPLATE_WITH_COUNTS + """
                         WHERE t.provider_id = :providerId
                           AND (t.status IN ('check_failed', 'draft') OR (t.status = 'published' AND t.warning_count > 0))
                         ORDER BY CASE t.status WHEN 'check_failed' THEN 0 WHEN 'published' THEN 1 ELSE 2 END,
                                  t.updated_at DESC""")
                .param("providerId", providerId)
                .query((rs, row) -> new ActionItemResponse(kindOf(rs.getString("status")),
                        rs.getObject("id", UUID.class), rs.getString("name"),
                        rs.getInt("error_count"), rs.getInt("warning_count")))
                .list();
    }

    public long totalDownloads(UUID providerId) {
        return jdbc.sql("""
                        SELECT count(*) FROM template_events e JOIN templates t ON t.id = e.template_id
                         WHERE t.provider_id = :providerId AND e.type = 'download'""")
                .param("providerId", providerId)
                .query(Long.class).single();
    }

    // ---------- event ----------

    /**
     * Menyimpan event. Download ganda untuk project yang sama diabaikan oleh database
     * (ON CONFLICT DO NOTHING pada index unik project + jenis), bukan menimbulkan error.
     *
     * @return true jika event benar-benar tersimpan
     */
    public boolean insertEvent(UUID templateId, TemplateEventType type, String projectId, String installId,
                               UUID userId, Instant occurredAt) {
        int rows = jdbc.sql("""
                        INSERT INTO template_events (template_id, type, project_id, install_id, user_id, occurred_at)
                        VALUES (:templateId, :type, :projectId, :installId, :userId, :occurredAt)
                        ON CONFLICT (project_id, type) WHERE project_id IS NOT NULL DO NOTHING""")
                .param("templateId", templateId)
                .param("type", type.value())
                .param("projectId", projectId)
                .param("installId", installId)
                .param("userId", userId)
                .param("occurredAt", Timestamp.from(occurredAt))
                .update();
        return rows > 0;
    }

    private static String kindOf(String status) {
        return switch (status) {
            case "check_failed" -> ActionItemResponse.CHECK_FAILED;
            case "draft" -> ActionItemResponse.DRAFT;
            default -> ActionItemResponse.WARNING;
        };
    }

    private static String commonFilters(WebsitePurpose category, String search) {
        return (category == null ? "" : " AND t.category = :category")
                + (search == null ? "" : " AND lower(t.name) LIKE :search ESCAPE '\\'");
    }

    private static JdbcClient.StatementSpec withCommonParams(JdbcClient.StatementSpec spec, UUID providerId,
                                                             WebsitePurpose category, String search) {
        spec = spec.param("providerId", providerId);
        if (category != null) {
            spec = spec.param("category", category.value());
        }
        if (search != null) {
            // % dan _ dari user di-escape agar dicari sebagai huruf biasa, bukan wildcard LIKE.
            String escaped = search.toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            spec = spec.param("search", "%" + escaped + "%");
        }
        return spec;
    }
}
