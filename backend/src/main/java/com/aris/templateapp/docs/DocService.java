package com.aris.templateapp.docs;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.docs.dto.DocDetailResponse;
import com.aris.templateapp.docs.dto.DocRequest;
import com.aris.templateapp.docs.dto.DocSummaryResponse;
import com.aris.templateapp.docs.dto.DocTreeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Pusat dokumentasi: dibaca publik (hanya yang tayang), dikelola tim internal dari portal web. */
@Service
@RequiredArgsConstructor
public class DocService {

    private static final String SUMMARY_COLUMNS =
            "id, slug, category, audience, title, summary, sort_order, status, published_at, updated_at";

    private final JdbcClient jdbc;
    private final Clock clock;

    // ---------- publik ----------

    @Transactional(readOnly = true)
    public DocTreeResponse tree() {
        List<DocSummaryResponse> published = jdbc.sql("SELECT " + SUMMARY_COLUMNS
                        + " FROM doc_articles WHERE status = 'published' ORDER BY sort_order, title")
                .query((rs, row) -> summary(rs))
                .list();
        List<DocTreeResponse.Category> categories = new ArrayList<>();
        for (DocCategory category : DocCategory.values()) {
            List<DocSummaryResponse> articles = published.stream()
                    .filter(a -> a.category().equals(category.value()))
                    .toList();
            if (!articles.isEmpty()) {
                categories.add(new DocTreeResponse.Category(category.value(), category.label(), articles));
            }
        }
        return new DocTreeResponse(categories);
    }

    @Transactional(readOnly = true)
    public DocDetailResponse published(String slug) {
        return jdbc.sql("SELECT * FROM doc_articles WHERE slug = :slug AND status = 'published'")
                .param("slug", slug)
                .query((rs, row) -> detail(rs))
                .optional()
                .orElseThrow(() -> new ApiException(ErrorCode.DOC_NOT_FOUND));
    }

    // ---------- admin ----------

    @Transactional(readOnly = true)
    public List<DocSummaryResponse> all() {
        return jdbc.sql("SELECT " + SUMMARY_COLUMNS + " FROM doc_articles")
                .query((rs, row) -> summary(rs))
                .list().stream()
                .sorted(Comparator.comparing((DocSummaryResponse a) -> DocCategory.of(a.category())
                                .map(Enum::ordinal).orElse(Integer.MAX_VALUE))
                        .thenComparingInt(DocSummaryResponse::sortOrder)
                        .thenComparing(DocSummaryResponse::title))
                .toList();
    }

    @Transactional(readOnly = true)
    public DocDetailResponse get(UUID id) {
        return jdbc.sql("SELECT * FROM doc_articles WHERE id = :id")
                .param("id", id)
                .query((rs, row) -> detail(rs))
                .optional()
                .orElseThrow(() -> new ApiException(ErrorCode.DOC_NOT_FOUND));
    }

    @Transactional
    public DocDetailResponse create(UUID adminId, DocRequest request) {
        Instant now = clock.instant();
        boolean publish = "published".equals(request.status());
        try {
            UUID id = jdbc.sql("""
                            INSERT INTO doc_articles (slug, category, audience, title, summary, body, sort_order, status,
                                                      published_at, created_by, updated_by, created_at, updated_at)
                            VALUES (:slug, :category, :audience, :title, :summary, :body, :sortOrder, :status,
                                    :publishedAt, :admin, :admin, :now, :now)
                            RETURNING id""")
                    .params(params(request))
                    .param("status", publish ? "published" : "draft")
                    .param("publishedAt", publish ? Timestamp.from(now) : null)
                    .param("admin", adminId).param("now", Timestamp.from(now))
                    .query(UUID.class).single();
            return get(id);
        } catch (DuplicateKeyException e) {
            throw new ApiException(ErrorCode.DOC_SLUG_TAKEN);
        }
    }

    @Transactional
    public DocDetailResponse update(UUID adminId, UUID id, DocRequest request) {
        get(id);
        Instant now = clock.instant();
        boolean publish = "published".equals(request.status());
        try {
            // published_at = tanggal tayang PERTAMA; menurunkan ke draft lalu menayangkan lagi tidak mengubahnya.
            jdbc.sql("""
                            UPDATE doc_articles SET slug = :slug, category = :category, audience = :audience,
                                   title = :title, summary = :summary, body = :body, sort_order = :sortOrder,
                                   status = :status,
                                   published_at = CASE WHEN :publish THEN COALESCE(published_at, :now) ELSE published_at END,
                                   updated_by = :admin, updated_at = :now
                             WHERE id = :id""")
                    .params(params(request))
                    .param("status", publish ? "published" : "draft")
                    .param("publish", publish)
                    .param("admin", adminId).param("now", Timestamp.from(now)).param("id", id)
                    .update();
        } catch (DuplicateKeyException e) {
            throw new ApiException(ErrorCode.DOC_SLUG_TAKEN);
        }
        return get(id);
    }

    @Transactional
    public void delete(UUID id) {
        if (jdbc.sql("DELETE FROM doc_articles WHERE id = :id").param("id", id).update() == 0) {
            throw new ApiException(ErrorCode.DOC_NOT_FOUND);
        }
    }

    // ---------- pembantu ----------

    private static java.util.Map<String, Object> params(DocRequest request) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("slug", request.slug());
        params.put("category", request.category());
        params.put("audience", request.audience() == null ? "all" : request.audience());
        params.put("title", request.title().trim());
        params.put("summary", request.summary() == null || request.summary().isBlank() ? null : request.summary().trim());
        params.put("body", request.body());
        params.put("sortOrder", request.sortOrder() == null ? 0 : request.sortOrder());
        return params;
    }

    private static DocSummaryResponse summary(ResultSet rs) throws SQLException {
        return new DocSummaryResponse(rs.getObject("id", UUID.class), rs.getString("slug"), rs.getString("category"),
                rs.getString("audience"), rs.getString("title"), rs.getString("summary"), rs.getInt("sort_order"),
                rs.getString("status"), instant(rs, "published_at"), instant(rs, "updated_at"));
    }

    private static DocDetailResponse detail(ResultSet rs) throws SQLException {
        String category = rs.getString("category");
        return new DocDetailResponse(rs.getObject("id", UUID.class), rs.getString("slug"), category,
                DocCategory.of(category).map(DocCategory::label).orElse(category), rs.getString("audience"),
                rs.getString("title"), rs.getString("summary"), rs.getString("body"), rs.getInt("sort_order"),
                rs.getString("status"), instant(rs, "published_at"), instant(rs, "updated_at"));
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toInstant();
    }
}
