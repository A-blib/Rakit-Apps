package com.aris.templateapp.gallery;

import com.aris.templateapp.gallery.dto.GalleryPageResponse;
import com.aris.templateapp.gallery.dto.TemplateDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Galeri", description = "Galeri template untuk pembuat website (publik, tanpa login)")
@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class GalleryController {

    /** Header versi paket, agar HP bisa mencatat versi yang benar-benar diunduh. */
    static final String VERSION_HEADER = "X-Template-Version";

    private final GalleryService galleryService;
    private final TemplatePackageService packageService;

    @Operation(summary = "Daftar template yang tayang",
            description = "Hanya template published dari provider aktif. \"Template untuk anda\" memakai endpoint ini "
                    + "dengan category={tujuan website}&sort=popular&size=6.")
    @GetMapping
    public GalleryPageResponse list(
            @Parameter(description = "all, sekolah, organisasi, umkm, instansi, pribadi, lainnya")
            @RequestParam(required = false) String category,
            @Parameter(description = "Cari berdasarkan nama template") @RequestParam(required = false) String q,
            @Parameter(description = "popular (bawaan) atau newest") @RequestParam(defaultValue = "popular") String sort,
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "1–50, bawaan 20") @RequestParam(required = false) Integer size) {
        return galleryService.list(category, q, sort, page, size);
    }

    @Operation(summary = "Detail template untuk layar Unduh",
            description = "404 jika template tidak tayang atau provider-nya ditangguhkan.")
    @GetMapping("/{id}")
    public TemplateDetailResponse detail(@PathVariable UUID id) {
        return packageService.detail(id);
    }

    @Operation(summary = "Unduh paket template (ZIP)",
            description = "Berisi manifest.json, HTML ber-data-key, dan library yang sudah disalin. Content-Length dikirim "
                    + "agar HP bisa menampilkan progres.")
    @GetMapping(value = "/{id}/package", produces = "application/zip")
    public ResponseEntity<Resource> downloadPackage(@PathVariable UUID id) {
        TemplatePackageService.Package pkg = packageService.packageFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(VERSION_HEADER, String.valueOf(pkg.version()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("template-" + id + ".zip").build().toString())
                // Isi paket satu versi tidak pernah berubah, tetapi versi baru memakai URL yang sama: jangan di-cache.
                .cacheControl(CacheControl.noStore())
                .body(new FileSystemResource(pkg.file()));
    }
}
