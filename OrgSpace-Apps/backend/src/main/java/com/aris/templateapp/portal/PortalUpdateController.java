package com.aris.templateapp.portal;

import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.portal.dto.PortalUpdateRequest;
import com.aris.templateapp.portal.dto.PortalUpdateResponse;
import com.aris.templateapp.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/** Info terbaru: dibaca publik (/api/public/**, hanya yang tayang), dikelola tim internal (/api/admin/**, AdminGuard). */
@Tag(name = "Landing: Info terbaru", description = "Update fitur, optimasi, dan desain ulang OrgSpace untuk portal web")
@RestController
@RequiredArgsConstructor
public class PortalUpdateController {

    private final PortalUpdateService service;
    private final CurrentUser currentUser;

    @Operation(summary = "Info terbaru yang tayang (publik)",
            description = "Terbaru dulu, tanpa isi lengkap. limit 1–12. Boleh di-cache 1 menit.")
    @GetMapping("/api/public/updates")
    public ResponseEntity<List<PortalUpdateResponse>> published(@RequestParam(defaultValue = "3") int limit) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(1)).cachePublic())
                .body(service.listPublished(limit));
    }

    @Operation(summary = "Detail info yang tayang (publik)", description = "404 jika draft atau tidak ada.")
    @GetMapping("/api/public/updates/{id}")
    public PortalUpdateResponse publishedDetail(@PathVariable UUID id) {
        return service.getPublished(id);
    }

    @Operation(summary = "Gambar info yang tayang (publik)")
    @GetMapping("/api/public/updates/{id}/image")
    public ResponseEntity<Resource> image(@PathVariable UUID id) {
        // URL gambar di web membawa ?v=<imageUpdatedAt>, jadi gambar boleh disimpan lama di cache.
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .contentType(MediaType.IMAGE_PNG)
                .body(new FileSystemResource(service.publishedImage(id)));
    }

    @Operation(summary = "Semua info, draft & tayang (admin)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @GetMapping("/api/admin/updates")
    public List<PortalUpdateResponse> list() {
        return service.listAll();
    }

    @Operation(summary = "Detail info (admin)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @GetMapping("/api/admin/updates/{id}")
    public PortalUpdateResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @Operation(summary = "Tulis info baru", description = "published = true langsung tayang di landing.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @PostMapping("/api/admin/updates")
    @ResponseStatus(HttpStatus.CREATED)
    public PortalUpdateResponse create(@Valid @RequestBody PortalUpdateRequest request) {
        return service.create(currentUser.id(), request);
    }

    @Operation(summary = "Ubah info (semua field dikirim ulang)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @PutMapping("/api/admin/updates/{id}")
    public PortalUpdateResponse update(@PathVariable UUID id, @Valid @RequestBody PortalUpdateRequest request) {
        return service.update(currentUser.id(), id, request);
    }

    @Operation(summary = "Tampilkan / sembunyikan dari landing", description = "Body { published: boolean }. Isi tidak berubah.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @PutMapping("/api/admin/updates/{id}/published")
    public PortalUpdateResponse setPublished(@PathVariable UUID id, @RequestBody PublishedRequest request) {
        return service.setPublished(currentUser.id(), id, request.published());
    }

    public record PublishedRequest(boolean published) {
    }

    @Operation(summary = "Hapus info beserta gambarnya")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @DeleteMapping("/api/admin/updates/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @Operation(summary = "Unggah gambar info (PNG/JPG, maks 2 MB)",
            description = "Diperkecil maks 1600 px tanpa dipotong dan ditulis ulang sebagai PNG. Dipakai di kartu landing "
                    + "dan di atas halaman detail.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @PostMapping(path = "/api/admin/updates/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PortalUpdateResponse uploadImage(@PathVariable UUID id, @RequestPart("file") MultipartFile file) throws IOException {
        return service.uploadImage(currentUser.id(), id, file.getBytes());
    }

    @Operation(summary = "Hapus gambar info")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @DeleteMapping("/api/admin/updates/{id}/image")
    public PortalUpdateResponse deleteImage(@PathVariable UUID id) {
        return service.deleteImage(currentUser.id(), id);
    }
}
