package com.aris.templateapp.gallery;

import com.aris.templateapp.gallery.dto.TemplatePreviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

/**
 * Preview template untuk landing page, disajikan OrgSpace-Hosting di {@code /demo/{id}/} dengan header sandbox.
 * Backend hanya memberi file paket dan data contoh; tidak ada login, token, atau data user.
 */
@Tag(name = "Galeri: Preview", description = "Preview template dengan data contoh (publik, tanpa login)")
@RestController
@RequiredArgsConstructor
public class TemplatePreviewController {

    private final TemplatePreviewService previewService;

    @Operation(summary = "Data contoh untuk modul data-ws-* template",
            description = "Bentuk sama dengan news/collections/modules/stats/org di render website. 404 jika template "
                    + "tidak tayang.")
    @GetMapping("/api/public/templates/{id}/preview")
    public TemplatePreviewResponse preview(@PathVariable UUID id) {
        return previewService.preview(id);
    }

    @Operation(summary = "Satu file paket template (HTML, CSS, JS, gambar)",
            description = "Untuk OrgSpace-Hosting. Jangan ditampilkan langsung ke pengunjung tanpa header sandbox Hosting.")
    @GetMapping("/api/public/templates/{id}/files/**")
    public ResponseEntity<byte[]> file(@PathVariable UUID id, HttpServletRequest request) {
        String prefix = "/api/public/templates/" + id + "/files/";
        String path = UriUtils.decode(request.getRequestURI().substring(prefix.length()), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaTypeFactory.getMediaType(path).orElse(MediaType.APPLICATION_OCTET_STREAM))
                // Versi paket baru memakai URL yang sama, jadi cache dibuat singkat.
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                // Jika file HTML ini dibuka langsung di alamat backend, script template tetap ter-sandbox (origin anonim).
                .header("Content-Security-Policy", "sandbox")
                .header("X-Content-Type-Options", "nosniff")
                .body(previewService.file(id, path));
    }
}
