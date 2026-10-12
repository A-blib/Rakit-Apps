package com.aris.templateapp.admin;

import com.aris.templateapp.admin.dto.AdminUserResponse;
import com.aris.templateapp.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Daftar pengguna untuk dashboard (tanpa data rahasia: tidak ada hash password atau token). */
@Repository
@RequiredArgsConstructor
public class AdminUserQueries {

    private static final String SELECT = """
            SELECT u.id, u.display_name, u.email, u.avatar_url, u.is_admin, u.onboarding_completed, u.created_at,
                   ARRAY(SELECT i.provider FROM user_identities i WHERE i.user_id = u.id ORDER BY i.created_at) AS methods,
                   p.status AS provider_status,
                   (SELECT count(*) FROM organization_members m JOIN organizations o ON o.id = m.organization_id
                     WHERE m.user_id = u.id AND o.type = 'organization') AS organizations,
                   (SELECT count(*) FROM projects pr WHERE pr.created_by = u.id) AS websites,
                   (SELECT max(a.day) FROM user_daily_activity a WHERE a.user_id = u.id) AS last_active_day
              FROM users u
              LEFT JOIN provider_profiles p ON p.user_id = u.id
            """;

    /** Filter daftar pengguna. {@code role}: creator / provider / admin, atau null untuk semua. */
    public record Filter(String search, String role) {
    }

    private final JdbcClient jdbc;

    public PageResponse<AdminUserResponse> page(Filter filter, int page, int size) {
        String where = where(filter);
        String pattern = pattern(filter.search());
        long total = jdbc.sql("SELECT count(*) FROM users u LEFT JOIN provider_profiles p ON p.user_id = u.id " + where)
                .param("q", pattern)
                .query(Long.class).single();
        List<AdminUserResponse> items = jdbc.sql(SELECT + where + " ORDER BY u.created_at DESC, u.id LIMIT :limit OFFSET :offset")
                .param("q", pattern).param("limit", size).param("offset", (long) page * size)
                .query((rs, row) -> map(rs))
                .list();
        return PageResponse.of(items, page, size, total);
    }

    public java.util.Optional<AdminUserResponse> find(UUID id) {
        return jdbc.sql(SELECT + " WHERE u.id = :id").param("id", id).query((rs, row) -> map(rs)).optional();
    }

    private static String where(Filter filter) {
        StringBuilder where = new StringBuilder(" WHERE (CAST(:q AS text) IS NULL OR lower(u.display_name) LIKE :q"
                + " OR lower(u.email) LIKE :q)");
        if ("provider".equals(filter.role())) {
            where.append(" AND p.user_id IS NOT NULL");
        } else if ("creator".equals(filter.role())) {
            where.append(" AND EXISTS (SELECT 1 FROM creator_profiles c WHERE c.user_id = u.id)");
        } else if ("admin".equals(filter.role())) {
            where.append(" AND u.is_admin");
        }
        return where.toString();
    }

    /** Kata kunci dicari sebagai teks biasa: % dan _ dari pengguna di-escape. */
    private static String pattern(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String escaped = search.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }

    private static AdminUserResponse map(java.sql.ResultSet rs) throws SQLException {
        Array methods = rs.getArray("methods");
        Date lastActive = rs.getDate("last_active_day");
        return new AdminUserResponse(rs.getObject("id", UUID.class), rs.getString("display_name"), rs.getString("email"),
                rs.getString("avatar_url"), rs.getBoolean("is_admin"), rs.getBoolean("onboarding_completed"),
                methods == null ? List.of() : Arrays.asList((String[]) methods.getArray()),
                rs.getString("provider_status"), rs.getLong("organizations"), rs.getLong("websites"),
                lastActive == null ? null : lastActive.toLocalDate(),
                rs.getTimestamp("created_at").toInstant());
    }
}
