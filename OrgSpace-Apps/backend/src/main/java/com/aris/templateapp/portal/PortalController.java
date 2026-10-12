package com.aris.templateapp.portal;

import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.portal.dto.PublicOverviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Data untuk landing page portal web. Galeri template publik sudah ada di {@code GET /api/templates}.
 */
@Tag(name = "Publik: Portal", description = "Data landing page (tanpa login)")
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PortalController {

    private final JdbcClient jdbc;
    private final AppProperties properties;

    @Operation(summary = "Angka & link untuk landing page", description = "Boleh di-cache 5 menit.")
    @GetMapping("/overview")
    @Transactional(readOnly = true)
    public ResponseEntity<PublicOverviewResponse> overview() {
        PublicOverviewResponse.Stats stats = jdbc.sql("""
                        SELECT (SELECT count(*) FROM organizations WHERE type = 'organization') AS organizations,
                               (SELECT count(*) FROM sites WHERE status = 'online') AS websites_online,
                               (SELECT count(*) FROM templates WHERE status = 'published') AS templates,
                               (SELECT count(*) FROM provider_profiles WHERE status = 'active') AS providers""")
                .query((rs, row) -> new PublicOverviewResponse.Stats(rs.getLong("organizations"),
                        rs.getLong("websites_online"), rs.getLong("templates"), rs.getLong("providers")))
                .single();
        String download = properties.portal().androidDownloadUrl();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                .body(new PublicOverviewResponse(stats, download == null || download.isBlank() ? null : download));
    }
}
