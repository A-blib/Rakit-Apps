package com.aris.templateapp.project;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.project.dto.AssetResponse;
import com.aris.templateapp.project.dto.BackupRequest;
import com.aris.templateapp.project.dto.BackupResponse;
import com.aris.templateapp.project.dto.ContentUpdateRequest;
import com.aris.templateapp.project.dto.ContentVersionResponse;
import com.aris.templateapp.project.dto.CreateProjectRequest;
import com.aris.templateapp.project.dto.LockRequest;
import com.aris.templateapp.project.dto.LockResponse;
import com.aris.templateapp.project.dto.WebsiteDetailResponse;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Tag(name = "Project", description = "Isi website di cloud: buat dari template, sinkron isi, kunci edit, gambar, cadangan")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final CurrentUser currentUser;

    @Operation(summary = "Pakai template: buat website baru di ruang kerja",
            description = "Isian yang dihubungkan kreator ke Profil organisasi langsung terisi; kunci edit langsung dipegang "
                    + "pembuat. 404 TEMPLATE_NOT_FOUND jika template tidak tayang atau versi paket sudah berganti.")
    @PostMapping("/organizations/{orgId}/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public WebsiteDetailResponse create(@PathVariable UUID orgId, @Valid @RequestBody CreateProjectRequest request) {
        return projectService.create(orgId, currentUser.id(), request.sourceTemplateId(), request.templateVersion(),
                request.name(), request.deviceId());
    }

    @Operation(summary = "Simpan isi website dari HP",
            description = "Pemegang kunci saja. 409 VERSION_CONFLICT jika baseVersion bukan versi terbaru, 409 LOCK_LOST jika "
                    + "kunci sudah diambil anggota lain.")
    @PutMapping("/projects/{id}/content")
    public ContentVersionResponse updateContent(@PathVariable UUID id, @Valid @RequestBody ContentUpdateRequest request) {
        return projectService.updateContent(id, currentUser.id(), request.baseVersion(), request.content(),
                request.missingCount());
    }

    @Operation(summary = "Ambil / perpanjang kunci edit",
            description = "409 PROJECT_LOCKED + details {holderName, lastActiveAt} jika anggota lain sedang mengedit.")
    @PostMapping("/projects/{id}/lock")
    public LockResponse lock(@PathVariable UUID id, @Valid @RequestBody LockRequest request) {
        return projectService.lock(id, currentUser.id(), request.deviceId());
    }

    @Operation(summary = "Lepas kunci edit (editor ditutup)")
    @DeleteMapping("/projects/{id}/lock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlock(@PathVariable UUID id) {
        projectService.unlock(id, currentUser.id());
    }

    @Operation(summary = "Ambil alih kunci edit", description = "Admin & Pemilik.")
    @PostMapping("/projects/{id}/lock/takeover")
    public LockResponse takeOver(@PathVariable UUID id, @Valid @RequestBody LockRequest request) {
        return projectService.takeOver(id, currentUser.id(), request.deviceId());
    }

    @Operation(summary = "Unggah gambar website",
            description = "WebP/JPEG/PNG maks PROJECT_ASSET_MAX_KB. 409 STORAGE_LIMIT_REACHED jika penyimpanan ruang kerja penuh.")
    @PostMapping(value = "/projects/{id}/assets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AssetResponse uploadAsset(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        return projectService.uploadAsset(id, currentUser.id(), bytes(file));
    }

    @Operation(summary = "Unggah thumbnail kartu website")
    @PostMapping(value = "/projects/{id}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadThumbnail(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        projectService.uploadThumbnail(id, currentUser.id(), bytes(file));
    }

    @Operation(summary = "File gambar website", description = "Anggota ruang kerja pemilik website.")
    @GetMapping("/assets/{id}")
    public ResponseEntity<Resource> asset(@PathVariable UUID id) {
        ProjectService.StoredAsset asset = projectService.asset(id, currentUser.id());
        // Isi aset tidak pernah berubah (id baru untuk gambar baru), jadi boleh disimpan lama di HP.
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(asset.mimeType()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePrivate())
                .body(new FileSystemResource(asset.file()));
    }

    @Operation(summary = "Daftar cadangan isi (5 terbaru)")
    @GetMapping("/projects/{id}/backups")
    public List<BackupResponse> backups(@PathVariable UUID id) {
        return projectService.backups(id, currentUser.id());
    }

    @Operation(summary = "Simpan isi lokal sebagai cadangan", description = "Dipakai HP saat LOCK_LOST / VERSION_CONFLICT.")
    @PostMapping("/projects/{id}/backups")
    @ResponseStatus(HttpStatus.CREATED)
    public BackupResponse createBackup(@PathVariable UUID id, @Valid @RequestBody BackupRequest request) {
        return projectService.createBackup(id, currentUser.id(), request.content(), request.reason());
    }

    @Operation(summary = "Pulihkan cadangan", description = "Admin & Pemilik yang memegang kunci; isi saat ini dicadangkan dulu.")
    @PostMapping("/projects/{id}/backups/{backupId}/restore")
    public ContentVersionResponse restore(@PathVariable UUID id, @PathVariable UUID backupId) {
        return projectService.restoreBackup(id, backupId, currentUser.id());
    }

    private static byte[] bytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new ApiException(ErrorCode.ASSET_INVALID);
        }
    }
}
