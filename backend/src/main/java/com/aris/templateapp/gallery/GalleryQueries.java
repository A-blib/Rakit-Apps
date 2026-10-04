package com.aris.templateapp.gallery;

import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.gallery.dto.GalleryTemplateResponse;
import com.aris.templateapp.user.WebsitePurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * SQL galeri Template untuk pembuat website. Yang tampil hanya template {@code published} milik provider
 * yang {@code active}: provider yang ditangguhkan (rem darurat untuk konten berbahaya) ikut disembunyikan
 * templatenya dari galeri.
 * <p>
 * Seperti {@code ProviderTemplateQueries}, bagian SQL yang disambung hanya dari enum tetap; nilai dari user
 * selalu menjadi parameter.
 */
@Repository
@RequiredArgsConstructor
public class GalleryQueries {

    /** Urutan galeri (bagian 6.2). Urutan kedua & ketiga menjaga hasil stabil antarhalaman. */
    public enum SortOrder {
        POPULAR("popular", "downloads DESC, t.published_at DESC NULLS LAST, t.id"),
        NEWEST("newest", "t.published_at DESC NULLS LAST, t.id");

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

    private static final String VISIBLE = """
              FROM templates t
              JOIN provider_profiles p ON p.user_id = t.provider_id
             WHERE t.status = 'published' AND p.status = 'active'
            """;

    private final JdbcClient jdbc;

    public List<GalleryTemplateResponse> list(WebsitePurpose category, String search, SortOrder sort, int page, int size) {
        String sql = """
                SELECT t.id, t.name, t.category, t.thumbnail_url, t.published_at, p.creator_name,
                       (SELECT count(*) FROM template_events e
                         WHERE e.template_id = t.id AND e.type = 'download') AS downloads
                """ + VISIBLE + filters(category, search)
                + " ORDER BY " + sort.orderBy + " LIMIT :limit OFFSET :offset";
        return params(jdbc.sql(sql), category, search)
                .param("limit", size)
                .param("offset", page * size)
                .query((rs, row) -> {
                    Timestamp published = rs.getTimestamp("published_at");
                    return new GalleryTemplateResponse(
                            rs.getObject("id", UUID.class),
                            rs.getString("name"),
                            PersistableEnum.fromValue(WebsitePurpose.class, rs.getString("category")),
                            rs.getString("thumbnail_url"),
                            rs.getString("creator_name"),
                            rs.getLong("downloads"),
                            published == null ? null : published.toInstant());
                })
                .list();
    }

    public long count(WebsitePurpose category, String search) {
        return params(jdbc.sql("SELECT count(*) " + VISIBLE + filters(category, search)), category, search)
                .query(Long.class)
                .single();
    }

    private static String filters(WebsitePurpose category, String search) {
        return (category == null ? "" : " AND t.category = :category")
                + (search == null ? "" : " AND lower(t.name) LIKE :search ESCAPE '\\'");
    }

    private static JdbcClient.StatementSpec params(JdbcClient.StatementSpec spec, WebsitePurpose category,
                                                   String search) {
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
