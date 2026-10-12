package com.aris.templateapp.feedback;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.common.response.PageResponse;
import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.feedback.dto.CreateFeedbackRequest;
import com.aris.templateapp.feedback.dto.FeedbackCountsResponse;
import com.aris.templateapp.feedback.dto.FeedbackDetailResponse;
import com.aris.templateapp.feedback.dto.FeedbackSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Kritik &amp; saran (docs/rancangan/portal-web.md bagian 4): pembuat website &amp; provider mengirim dari app, tim
 * internal membaca dan membalas dari portal web. Satu feedback adalah satu percakapan.
 * <p>
 * Aturan status: balasan pertama tim memindahkan {@code new} → {@code in_progress}; tanggapan pengirim pada feedback
 * {@code resolved} membukanya lagi ({@code in_progress}); feedback {@code closed} tidak bisa dibalas pengirim.
 */
@Service
@RequiredArgsConstructor
public class FeedbackService {

    /** Nama yang dilihat pengirim untuk balasan staf. */
    static final String STAFF_NAME = "Tim OrgSpace";

    /** Siapa yang membuka percakapan menentukan tanda "belum dibaca" mana yang dihapus. */
    public enum Viewer { SENDER, STAFF }

    /** Filter daftar admin; semua boleh null. */
    public record AdminFilter(String status, String senderRole, String category, String search, Boolean unread) {
    }

    private static final String SUMMARY_SELECT = """
            SELECT f.id, f.category, f.subject, f.status, f.sender_role, f.rating, f.source, f.user_unread,
                   f.staff_unread, f.last_message_at, f.created_at,
                   1 + (SELECT count(*) FROM feedback_messages m WHERE m.feedback_id = f.id) AS message_count,
                   u.id AS sender_id, u.display_name AS sender_name, u.email AS sender_email,
                   o.name AS organization_name
              FROM feedback f
              JOIN users u ON u.id = f.user_id
              LEFT JOIN organizations o ON o.id = f.organization_id
            """;

    private final JdbcClient jdbc;
    private final Clock clock;
    private final AppProperties properties;

    // ---------- pengirim (app / web pengguna) ----------

    @Transactional
    public FeedbackDetailResponse create(UUID userId, CreateFeedbackRequest request, String source) {
        Instant now = clock.instant();
        long recent = jdbc.sql("SELECT count(*) FROM feedback WHERE user_id = :user AND created_at > :since")
                .param("user", userId).param("since", Timestamp.from(now.minus(Duration.ofDays(1))))
                .query(Long.class).single();
        if (recent >= properties.portal().feedbackDailyLimit()) {
            throw new ApiException(ErrorCode.TOO_MANY_ATTEMPTS,
                    "Kamu sudah mengirim banyak kritik & saran hari ini. Coba lagi besok, ya.");
        }
        String role = senderRole(userId, request.senderRole());
        if (request.organizationId() != null) {
            boolean member = jdbc.sql("""
                            SELECT EXISTS (SELECT 1 FROM organization_members
                                            WHERE organization_id = :org AND user_id = :user)""")
                    .param("org", request.organizationId()).param("user", userId)
                    .query(Boolean.class).single();
            if (!member) {
                throw new ApiException(ErrorCode.NOT_ORG_MEMBER);
            }
        }
        UUID id = jdbc.sql("""
                        INSERT INTO feedback (user_id, sender_role, organization_id, category, subject, message, rating,
                                              source, app_version, device_info, last_message_at, created_at, updated_at)
                        VALUES (:user, :role, :org, :category, :subject, :message, :rating, :source, :appVersion,
                                :deviceInfo, :now, :now, :now)
                        RETURNING id""")
                .param("user", userId).param("role", role).param("org", request.organizationId())
                .param("category", request.category()).param("subject", request.subject().trim())
                .param("message", request.message().trim()).param("rating", request.rating())
                .param("source", source).param("appVersion", blankToNull(request.appVersion()))
                .param("deviceInfo", blankToNull(request.deviceInfo())).param("now", Timestamp.from(now))
                .query(UUID.class).single();
        return detail(id, Viewer.SENDER);
    }

    @Transactional(readOnly = true)
    public PageResponse<FeedbackSummaryResponse> mine(UUID userId, int page, int size) {
        long total = jdbc.sql("SELECT count(*) FROM feedback WHERE user_id = :user")
                .param("user", userId).query(Long.class).single();
        List<FeedbackSummaryResponse> items = jdbc.sql(SUMMARY_SELECT + """
                         WHERE f.user_id = :user ORDER BY f.last_message_at DESC, f.id LIMIT :limit OFFSET :offset""")
                .param("user", userId).param("limit", size).param("offset", (long) page * size)
                .query((rs, row) -> summary(rs, Viewer.SENDER))
                .list();
        return PageResponse.of(items, page, size, total);
    }

    @Transactional(readOnly = true)
    public long unreadForSender(UUID userId) {
        return jdbc.sql("SELECT count(*) FROM feedback WHERE user_id = :user AND user_unread")
                .param("user", userId).query(Long.class).single();
    }

    /** Membuka percakapan milik sendiri; tanda balasan baru dihapus. */
    @Transactional
    public FeedbackDetailResponse openAsSender(UUID userId, UUID id) {
        requireOwner(userId, id);
        jdbc.sql("UPDATE feedback SET user_unread = false WHERE id = :id").param("id", id).update();
        return detail(id, Viewer.SENDER);
    }

    @Transactional
    public FeedbackDetailResponse replyAsSender(UUID userId, UUID id, String body) {
        String status = requireOwner(userId, id);
        if ("closed".equals(status)) {
            throw new ApiException(ErrorCode.FEEDBACK_CLOSED);
        }
        Instant now = clock.instant();
        insertMessage(id, userId, false, body, now);
        jdbc.sql("""
                        UPDATE feedback SET staff_unread = true, user_unread = false, last_message_at = :now,
                               updated_at = :now,
                               status = CASE WHEN status = 'resolved' THEN 'in_progress' ELSE status END
                         WHERE id = :id""")
                .param("id", id).param("now", Timestamp.from(now)).update();
        return detail(id, Viewer.SENDER);
    }

    // ---------- tim internal (portal web) ----------

    @Transactional(readOnly = true)
    public PageResponse<FeedbackSummaryResponse> adminPage(AdminFilter filter, int page, int size) {
        StringBuilder where = new StringBuilder(" WHERE true");
        Map<String, Object> params = new java.util.HashMap<>();
        if (filter.status() != null) {
            where.append(" AND f.status = :status");
            params.put("status", filter.status());
        }
        if (filter.senderRole() != null) {
            where.append(" AND f.sender_role = :role");
            params.put("role", filter.senderRole());
        }
        if (filter.category() != null) {
            where.append(" AND f.category = :category");
            params.put("category", filter.category());
        }
        if (Boolean.TRUE.equals(filter.unread())) {
            where.append(" AND f.staff_unread");
        }
        if (filter.search() != null && !filter.search().isBlank()) {
            where.append(" AND (lower(f.subject) LIKE :q OR lower(f.message) LIKE :q OR lower(u.email) LIKE :q"
                    + " OR lower(u.display_name) LIKE :q)");
            params.put("q", "%" + filter.search().trim().toLowerCase(Locale.ROOT)
                    .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%");
        }
        long total = jdbc.sql("SELECT count(*) FROM feedback f JOIN users u ON u.id = f.user_id" + where)
                .params(params).query(Long.class).single();
        Map<String, Object> pageParams = new java.util.HashMap<>(params);
        pageParams.put("limit", size);
        pageParams.put("offset", (long) page * size);
        // Belum dibaca tim tampil paling atas, lalu yang terbaru.
        List<FeedbackSummaryResponse> items = jdbc.sql(SUMMARY_SELECT + where
                        + " ORDER BY f.staff_unread DESC, f.last_message_at DESC, f.id LIMIT :limit OFFSET :offset")
                .params(pageParams)
                .query((rs, row) -> summary(rs, Viewer.STAFF))
                .list();
        return PageResponse.of(items, page, size, total);
    }

    @Transactional(readOnly = true)
    public FeedbackCountsResponse counts() {
        return jdbc.sql("""
                        SELECT count(*) AS all_count,
                               count(*) FILTER (WHERE status = 'new') AS new_count,
                               count(*) FILTER (WHERE status = 'in_progress') AS in_progress,
                               count(*) FILTER (WHERE status = 'resolved') AS resolved,
                               count(*) FILTER (WHERE status = 'closed') AS closed,
                               count(*) FILTER (WHERE staff_unread) AS unread
                          FROM feedback""")
                .query((rs, row) -> new FeedbackCountsResponse(rs.getLong("all_count"), rs.getLong("new_count"),
                        rs.getLong("in_progress"), rs.getLong("resolved"), rs.getLong("closed"), rs.getLong("unread")))
                .single();
    }

    @Transactional
    public FeedbackDetailResponse openAsStaff(UUID id) {
        requireExists(id);
        jdbc.sql("UPDATE feedback SET staff_unread = false WHERE id = :id").param("id", id).update();
        return detail(id, Viewer.STAFF);
    }

    @Transactional
    public FeedbackDetailResponse replyAsStaff(UUID staffId, UUID id, String body) {
        requireExists(id);
        Instant now = clock.instant();
        insertMessage(id, staffId, true, body, now);
        jdbc.sql("""
                        UPDATE feedback SET user_unread = true, staff_unread = false, last_message_at = :now,
                               updated_at = :now,
                               status = CASE WHEN status = 'new' THEN 'in_progress' ELSE status END
                         WHERE id = :id""")
                .param("id", id).param("now", Timestamp.from(now)).update();
        return detail(id, Viewer.STAFF);
    }

    @Transactional
    public FeedbackDetailResponse changeStatus(UUID id, String status) {
        requireExists(id);
        jdbc.sql("UPDATE feedback SET status = :status, updated_at = :now WHERE id = :id")
                .param("id", id).param("status", status).param("now", Timestamp.from(clock.instant())).update();
        return detail(id, Viewer.STAFF);
    }

    // ---------- pembantu ----------

    private FeedbackDetailResponse detail(UUID id, Viewer viewer) {
        FeedbackDetailResponse base = jdbc.sql("""
                        SELECT f.*, u.display_name AS sender_name, u.email AS sender_email, o.name AS organization_name
                          FROM feedback f
                          JOIN users u ON u.id = f.user_id
                          LEFT JOIN organizations o ON o.id = f.organization_id
                         WHERE f.id = :id""")
                .param("id", id)
                .query((rs, row) -> {
                    boolean staff = viewer == Viewer.STAFF;
                    FeedbackSummaryResponse.Sender sender = new FeedbackSummaryResponse.Sender(
                            rs.getObject("user_id", UUID.class), rs.getString("sender_name"),
                            staff ? rs.getString("sender_email") : null);
                    List<FeedbackDetailResponse.Message> messages = new ArrayList<>();
                    messages.add(new FeedbackDetailResponse.Message(id, false, rs.getString("sender_name"),
                            rs.getString("message"), rs.getTimestamp("created_at").toInstant()));
                    return new FeedbackDetailResponse(id, rs.getString("category"), rs.getString("subject"),
                            rs.getString("status"), rs.getString("sender_role"), rating(rs), rs.getString("source"),
                            sender, rs.getObject("organization_id", UUID.class), rs.getString("organization_name"),
                            staff ? rs.getString("app_version") : null, staff ? rs.getString("device_info") : null,
                            messages, rs.getTimestamp("created_at").toInstant(),
                            rs.getTimestamp("updated_at").toInstant());
                })
                .optional()
                .orElseThrow(() -> new ApiException(ErrorCode.FEEDBACK_NOT_FOUND));
        jdbc.sql("""
                        SELECT m.id, m.from_staff, m.body, m.created_at, a.display_name AS author_name
                          FROM feedback_messages m LEFT JOIN users a ON a.id = m.author_id
                         WHERE m.feedback_id = :id ORDER BY m.created_at, m.id""")
                .param("id", id)
                .query((rs, row) -> {
                    boolean fromStaff = rs.getBoolean("from_staff");
                    String author = fromStaff && viewer == Viewer.SENDER ? STAFF_NAME : rs.getString("author_name");
                    return new FeedbackDetailResponse.Message(rs.getObject("id", UUID.class), fromStaff,
                            author == null ? (fromStaff ? STAFF_NAME : base.sender().displayName()) : author,
                            rs.getString("body"), rs.getTimestamp("created_at").toInstant());
                })
                .list()
                .forEach(base.messages()::add);
        return base;
    }

    private static FeedbackSummaryResponse summary(ResultSet rs, Viewer viewer) throws SQLException {
        boolean staff = viewer == Viewer.STAFF;
        return new FeedbackSummaryResponse(rs.getObject("id", UUID.class), rs.getString("category"),
                rs.getString("subject"), rs.getString("status"), rs.getString("sender_role"), rating(rs),
                rs.getString("source"), rs.getBoolean(staff ? "staff_unread" : "user_unread"),
                rs.getInt("message_count"),
                staff ? new FeedbackSummaryResponse.Sender(rs.getObject("sender_id", UUID.class),
                        rs.getString("sender_name"), rs.getString("sender_email")) : null,
                rs.getString("organization_name"), rs.getTimestamp("last_message_at").toInstant(),
                rs.getTimestamp("created_at").toInstant());
    }

    private static Integer rating(ResultSet rs) throws SQLException {
        int rating = rs.getInt("rating");
        return rs.wasNull() ? null : rating;
    }

    private void insertMessage(UUID feedbackId, UUID authorId, boolean fromStaff, String body, Instant now) {
        jdbc.sql("""
                        INSERT INTO feedback_messages (feedback_id, author_id, from_staff, body, created_at)
                        VALUES (:feedback, :author, :staff, :body, :now)""")
                .param("feedback", feedbackId).param("author", authorId).param("staff", fromStaff)
                .param("body", body.trim()).param("now", Timestamp.from(now))
                .update();
    }

    /** @return status feedback; 404 juga untuk feedback milik orang lain (tidak membocorkan keberadaannya) */
    private String requireOwner(UUID userId, UUID id) {
        return jdbc.sql("SELECT status FROM feedback WHERE id = :id AND user_id = :user")
                .param("id", id).param("user", userId)
                .query(String.class).optional()
                .orElseThrow(() -> new ApiException(ErrorCode.FEEDBACK_NOT_FOUND));
    }

    private void requireExists(UUID id) {
        jdbc.sql("SELECT id FROM feedback WHERE id = :id").param("id", id).query(UUID.class).optional()
                .orElseThrow(() -> new ApiException(ErrorCode.FEEDBACK_NOT_FOUND));
    }

    /** Peran pengirim: yang diminta (jika akunnya memang punya peran itu), atau mode aktif akun. */
    private String senderRole(UUID userId, String requested) {
        if ("provider".equals(requested)) {
            boolean provider = jdbc.sql("SELECT EXISTS (SELECT 1 FROM provider_profiles WHERE user_id = :user)")
                    .param("user", userId).query(Boolean.class).single();
            if (!provider) {
                throw new ApiException(ErrorCode.PROVIDER_REQUIRED);
            }
            return requested;
        }
        if ("creator".equals(requested)) {
            return requested;
        }
        return jdbc.sql("SELECT active_mode FROM users WHERE id = :user")
                .param("user", userId).query(String.class).optional()
                .filter(mode -> mode.equals("provider"))
                .orElse("creator");
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
