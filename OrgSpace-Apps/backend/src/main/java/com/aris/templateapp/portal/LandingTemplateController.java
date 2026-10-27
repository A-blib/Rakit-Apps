package com.aris.templateapp.portal;

import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.gallery.dto.GalleryTemplateResponse;
import com.aris.templateapp.portal.dto.LandingTemplatesRequest;
import com.aris.templateapp.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

/** Template unggulan landing page: dibaca publik, dipilih tim internal (/api/admin/**, dijaga AdminGuard). */
@Tag(name = "Landing: Template unggulan", description = "Template galeri yang dipajang di landing page portal web")
@RestController
@RequiredArgsConstructor
public class LandingTemplateController {

    private final LandingTemplateService service;
    private final CurrentUser currentUser;

    @Operation(summary = "Template unggulan (publik)", description = "Maks 12, urutan pilihan admin. Boleh di-cache 5 menit.")
    @GetMapping("/api/public/landing/templates")
    public ResponseEntity<List<GalleryTemplateResponse>> publicList() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                .body(service.list());
    }

    @Operation(summary = "Template unggulan (admin)",
            description = "Untuk memilih, jelajahi galeri per halaman lewat GET /api/templates?page=&size=&q=&category=.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @GetMapping("/api/admin/landing/templates")
    public List<GalleryTemplateResponse> list() {
        return service.list();
    }

    @Operation(summary = "Simpan daftar template unggulan (mengganti semua, berurutan)",
            description = "Maks 12. 400 LANDING_TEMPLATE_INVALID jika ada template yang tidak tayang di galeri.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @PutMapping("/api/admin/landing/templates")
    public List<GalleryTemplateResponse> replace(@Valid @RequestBody LandingTemplatesRequest request) {
        return service.replace(currentUser.id(), request.templateIds());
    }
}
