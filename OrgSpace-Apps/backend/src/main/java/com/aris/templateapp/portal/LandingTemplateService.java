package com.aris.templateapp.portal;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.gallery.GalleryQueries;
import com.aris.templateapp.gallery.dto.GalleryTemplateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Template unggulan landing page. Admin memilihnya dari galeri ({@code GET /api/templates}, per halaman); di sini
 * hanya disimpan daftar pilihannya (maks 12), sehingga landing tidak pernah memuat seluruh galeri.
 */
@Service
@RequiredArgsConstructor
public class LandingTemplateService {

    private final JdbcClient jdbc;
    private final GalleryQueries gallery;

    @Transactional(readOnly = true)
    public List<GalleryTemplateResponse> list() {
        return gallery.landing();
    }

    /** Mengganti seluruh daftar; urutan list = urutan tampil. */
    @Transactional
    public List<GalleryTemplateResponse> replace(UUID adminId, List<UUID> templateIds) {
        if (new HashSet<>(templateIds).size() != templateIds.size()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Template yang sama dipilih lebih dari sekali.");
        }
        if (!templateIds.isEmpty() && gallery.countVisible(templateIds) != templateIds.size()) {
            throw new ApiException(ErrorCode.LANDING_TEMPLATE_INVALID);
        }
        jdbc.sql("DELETE FROM landing_templates").update();
        for (int i = 0; i < templateIds.size(); i++) {
            jdbc.sql("INSERT INTO landing_templates (template_id, sort_order, added_by) VALUES (:id, :order, :admin)")
                    .param("id", templateIds.get(i)).param("order", i).param("admin", adminId)
                    .update();
        }
        return gallery.landing();
    }
}
