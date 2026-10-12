package com.aris.templateapp.project;

import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.project.dto.PublishRequestResponse;
import com.aris.templateapp.project.dto.WebsiteListResponse;
import com.aris.templateapp.project.dto.WebsiteSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL tab Website. Status dihitung di sini dari {@code sites} + rilis aktif, bukan di HP, sehingga semua anggota
 * melihat status yang sama (alur-pembuatan-website.md 4.2).
 * <p>
 * Seperti {@code GalleryQueries}, bagian SQL yang disambung hanya dari enum tetap; nilai dari user selalu
 * menjadi parameter.
 */
@Repository
@RequiredArgsConstructor
public class WebsiteQueries {

    /** Chip filter tab Website. "Dimatikan" dan "Ditangguhkan" hanya ikut di "Semua". */
    public enum Filter {
        ALL("all", ""),
        DRAFT("draft", " AND s.id IS NULL"),
        ONLINE("online", " AND s.status = 'online'"),
        // Isi editor lebih baru dari snapshot rilis yang sedang online.
        CHANGED("changed", " AND s.status = 'online' AND p.content_updated_at > r.created_at");

        private final String value;
        final String where;

        Filter(String value, String where) {
            this.value = value;
            this.where = where;
        }

        public static Filter from(String value) {
            for (Filter filter : values()) {
                if (filter.value.equals(value)) {
                    return filter;
                }
            }
            throw new IllegalArgumentException("Filter tidak dikenal: " + value);
        }
    }

    /** Urutan tab Website (default: terakhir diubah, G2). Urutan kedua menjaga hasil stabil antarhalaman. */
    public enum Sort {
        UPDATED("updated", "p.updated_at DESC, p.id"),
        NEWEST("newest", "p.created_at DESC, p.id"),
        NAME("name", "lower(p.name), p.id");

        private final String value;
        final String orderBy;

        Sort(String value, String orderBy) {
            this.value = value;
            this.orderBy = orderBy;
        }

        public static Sort from(String value) {
            for (Sort sort : values()) {
                if (sort.value.equals(value)) {
                    return sort;
                }
            }
            throw new IllegalArgumentException("Urutan tidak dikenal: " + value);
        }
    }

    private static final String FROM = """
              FROM projects p
              LEFT JOIN users u ON u.id = p.updated_by
              LEFT JOIN sites s ON s.project_id = p.id
              LEFT JOIN site_releases r ON r.id = s.current_release_id
              LEFT JOIN project_locks l ON l.project_id = p.id AND l.expires_at > now()
              LEFT JOIN users lu ON lu.id = l.user_id
            """;

    private static final String COLUMNS = """
            SELECT p.id, p.name, p.mode, p.missing_count, p.thumbnail_asset_id, p.source_template_id,
                   p.created_at, p.updated_at, u.display_name AS updated_by_name,
                   s.slug, s.status AS site_status,
                   (s.id IS NOT NULL AND r.id IS NOT NULL AND p.content_updated_at > r.created_at) AS changed,
                   l.user_id AS lock_user_id, l.expires_at AS lock_expires_at, lu.display_name AS lock_name
            """;

    private final JdbcClient jdbc;
    private final AppProperties properties;

    public List<WebsiteSummaryResponse> list(UUID organizationId, UUID viewerId, Filter filter, String search,
                                             Sort sort, int page, int size) {
        String sql = COLUMNS + FROM + " WHERE p.organization_id = :org" + filter.where + searchSql(search)
                + " ORDER BY " + sort.orderBy + " LIMIT :limit OFFSET :offset";
        return withSearch(jdbc.sql(sql), search)
                .param("org", organizationId)
                .param("limit", size)
                .param("offset", page * size)
                .query((rs, row) -> summary(rs, viewerId))
                .list();
    }

    public Optional<WebsiteSummaryResponse> find(UUID projectId, UUID viewerId) {
        return jdbc.sql(COLUMNS + FROM + " WHERE p.id = :id")
                .param("id", projectId)
                .query((rs, row) -> summary(rs, viewerId))
                .optional();
    }

    public WebsiteListResponse.Counts counts(UUID organizationId, String search) {
        String sql = """
                SELECT count(*) AS all_count,
                       count(*) FILTER (WHERE s.id IS NULL) AS draft_count,
                       count(*) FILTER (WHERE s.status = 'online') AS online_count,
                       count(*) FILTER (WHERE s.status = 'online' AND p.content_updated_at > r.created_at)
                           AS changed_count
                """ + FROM + " WHERE p.organization_id = :org" + searchSql(search);
        return withSearch(jdbc.sql(sql), search)
                .param("org", organizationId)
                .query((rs, row) -> new WebsiteListResponse.Counts(rs.getLong("all_count"),
                        rs.getLong("draft_count"), rs.getLong("online_count"), rs.getLong("changed_count")))
                .single();
    }

    /** Website yang punya alamat (online, dimatikan, ditangguhkan) dihitung untuk batas D1. */
    public int sitesUsed(UUID organizationId) {
        return jdbc.sql("SELECT count(*) FROM sites WHERE organization_id = :org")
                .param("org", organizationId)
                .query(Integer.class)
                .single();
    }

    /** Info terbit untuk halaman kelola; kosong jika website belum pernah diterbitkan. */
    public Optional<SiteInfo> siteInfo(UUID projectId) {
        return jdbc.sql("SELECT published_at, suspension_reason FROM sites WHERE project_id = :id")
                .param("id", projectId)
                .query((rs, row) -> new SiteInfo(toInstant(rs.getTimestamp("published_at")),
                        rs.getString("suspension_reason")))
                .optional();
    }

    public record SiteInfo(Instant publishedAt, String suspensionReason) {
    }

    public boolean hasSite(UUID projectId) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM sites WHERE project_id = :id)")
                .param("id", projectId)
                .query(Boolean.class)
                .single();
    }

    /** Nama website yang sudah dipakai di ruang kerja, untuk membuat nama unik "Nama (2)". */
    public List<String> namesStartingWith(UUID organizationId, String base) {
        return jdbc.sql("SELECT name FROM projects WHERE organization_id = :org AND lower(name) LIKE :prefix ESCAPE '\\'")
                .param("org", organizationId)
                .param("prefix", escapeLike(base.toLowerCase()) + "%")
                .query(String.class)
                .list();
    }

    public List<PublishRequestResponse> openRequests(UUID organizationId) {
        return jdbc.sql(REQUEST_COLUMNS + " WHERE pr.organization_id = :org AND pr.status = 'open'"
                        + " ORDER BY pr.created_at DESC")
                .param("org", organizationId)
                .query((rs, row) -> request(rs))
                .list();
    }

    public Optional<PublishRequestResponse> openRequest(UUID projectId) {
        return jdbc.sql(REQUEST_COLUMNS + " WHERE pr.project_id = :id AND pr.status = 'open'")
                .param("id", projectId)
                .query((rs, row) -> request(rs))
                .optional();
    }

    private static final String REQUEST_COLUMNS = """
            SELECT pr.id, pr.project_id, p.name AS project_name, u.display_name AS requested_by_name,
                   pr.note, pr.created_at
              FROM publish_requests pr
              JOIN projects p ON p.id = pr.project_id
              LEFT JOIN users u ON u.id = pr.requested_by
            """;

    private static PublishRequestResponse request(ResultSet rs) throws SQLException {
        return new PublishRequestResponse(rs.getObject("id", UUID.class), rs.getObject("project_id", UUID.class),
                rs.getString("project_name"), rs.getString("requested_by_name"), rs.getString("note"),
                toInstant(rs.getTimestamp("created_at")));
    }

    private WebsiteSummaryResponse summary(ResultSet rs, UUID viewerId) throws SQLException {
        String slug = rs.getString("slug");
        UUID thumbnail = rs.getObject("thumbnail_asset_id", UUID.class);
        UUID lockUser = rs.getObject("lock_user_id", UUID.class);
        // Kunci milik sendiri tidak ditampilkan sebagai "Sedang diedit oleh ...".
        WebsiteSummaryResponse.LockHolder lock = lockUser == null || lockUser.equals(viewerId) ? null
                : new WebsiteSummaryResponse.LockHolder(rs.getString("lock_name"),
                        toInstant(rs.getTimestamp("lock_expires_at")));
        return new WebsiteSummaryResponse(
                rs.getObject("id", UUID.class),
                rs.getString("name"),
                PersistableEnum.fromValue(ProjectMode.class, rs.getString("mode")),
                WebsiteStatus.fromSiteStatus(rs.getString("site_status")),
                rs.getBoolean("changed"),
                slug == null ? null : properties.sites().urlOf(slug),
                rs.getInt("missing_count"),
                thumbnail == null ? null : "/api/assets/" + thumbnail,
                rs.getObject("source_template_id", UUID.class),
                toInstant(rs.getTimestamp("created_at")),
                toInstant(rs.getTimestamp("updated_at")),
                rs.getString("updated_by_name"),
                lock);
    }

    private static String searchSql(String search) {
        return search == null || search.isBlank() ? "" : " AND lower(p.name) LIKE :search ESCAPE '\\'";
    }

    private static JdbcClient.StatementSpec withSearch(JdbcClient.StatementSpec spec, String search) {
        return search == null || search.isBlank() ? spec
                : spec.param("search", "%" + escapeLike(search.trim().toLowerCase()) + "%");
    }

    /** % dan _ yang diketik user dicari apa adanya, bukan sebagai wildcard. */
    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private static Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
