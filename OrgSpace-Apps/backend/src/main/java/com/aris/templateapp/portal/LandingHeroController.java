package com.aris.templateapp.portal;

import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.portal.dto.LandingHeroRequest;
import com.aris.templateapp.portal.dto.LandingHeroResponse;
import com.aris.templateapp.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;

/** Hero landing page: dibaca publik (/api/public/**), diubah tim internal (/api/admin/**, dijaga AdminGuard). */
@Tag(name = "Landing: Hero", description = "Teks & gambar hero landing page portal web")
@RestController
@RequiredArgsConstructor
public class LandingHeroController {

    private final LandingHeroService service;
    private final CurrentUser currentUser;

    @Operation(summary = "Isi hero (publik)", description = "Boleh di-cache 1 menit.")
    @GetMapping("/api/public/landing/hero")
    public ResponseEntity<LandingHeroResponse> publicHero() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)).cachePublic())
                .body(service.get());
    }

    @Operation(summary = "Gambar hero unggahan (publik)", description = "404 jika masih memakai gambar bawaan web.")
    @GetMapping("/api/public/landing/hero/image")
    public ResponseEntity<Resource> image() {
        // URL gambar di web membawa ?v=<imageUpdatedAt>, jadi gambar boleh disimpan lama di cache.
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .contentType(MediaType.IMAGE_PNG)
                .body(new FileSystemResource(service.imageFile()));
    }

    @Operation(summary = "Isi hero (admin)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @GetMapping("/api/admin/landing/hero")
    public LandingHeroResponse get() {
        return service.get();
    }

    @Operation(summary = "Ubah teks hero (semua field dikirim ulang)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @PutMapping("/api/admin/landing/hero")
    public LandingHeroResponse update(@Valid @RequestBody LandingHeroRequest request) {
        return service.update(currentUser.id(), request);
    }

    @Operation(summary = "Unggah gambar hero (PNG/JPG, maks 2 MB)",
            description = "Diperkecil maks 1600 px tanpa dipotong dan ditulis ulang sebagai PNG.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @PostMapping(path = "/api/admin/landing/hero/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public LandingHeroResponse uploadImage(@RequestPart("file") MultipartFile file) throws IOException {
        return service.uploadImage(currentUser.id(), file.getBytes());
    }

    @Operation(summary = "Hapus gambar unggahan (kembali ke gambar bawaan web)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @DeleteMapping("/api/admin/landing/hero/image")
    public LandingHeroResponse deleteImage() {
        return service.deleteImage(currentUser.id());
    }
}
