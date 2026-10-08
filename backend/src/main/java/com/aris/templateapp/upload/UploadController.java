package com.aris.templateapp.upload;

import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.security.CurrentUser;
import com.aris.templateapp.upload.dto.CreateUploadSessionRequest;
import com.aris.templateapp.upload.dto.DeviceWarningsRequest;
import com.aris.templateapp.upload.dto.DraftInfoRequest;
import com.aris.templateapp.upload.dto.DraftResponse;
import com.aris.templateapp.upload.dto.DraftStepRequest;
import com.aris.templateapp.upload.dto.UploadSettingsResponse;
import com.aris.templateapp.upload.dto.ReportIssueRequest;
import com.aris.templateapp.upload.dto.UploadCheckResponse;
import com.aris.templateapp.upload.dto.UploadOverviewResponse;
import com.aris.templateapp.upload.dto.UploadSessionResponse;
import com.aris.templateapp.upload.dto.UploadStartedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

@Tag(name = "Upload", description = "Upload template provider: ZIP per potongan, pengecekan, draft (alur-fitur-upload.md)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/providers/me/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;
    private final DraftService draftService;
    private final CurrentUser currentUser;

    @Operation(summary = "Halaman awal tab Upload: perlu diperbaiki, sedang dicek, draft, kuota")
    @GetMapping
    public UploadOverviewResponse overview() {
        return uploadService.overview(currentUser.id());
    }

    @Operation(summary = "Mulai upload ZIP per potongan",
            description = "ZIP saja (RAR → RAR_NOT_SUPPORTED), maks 20 MB. Isi templateId untuk \"Upload file perbaikan\".")
    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public UploadSessionResponse createSession(@Valid @RequestBody CreateUploadSessionRequest request) {
        return uploadService.createSession(currentUser.id(), request);
    }

    @Operation(summary = "Posisi upload terakhir (untuk melanjutkan setelah sinyal putus)")
    @GetMapping("/sessions/{sessionId}")
    public UploadSessionResponse session(@PathVariable UUID sessionId) {
        return uploadService.session(currentUser.id(), sessionId);
    }

    @Operation(summary = "Kirim satu potongan (body = byte mentah, maks chunkSize)",
            description = "offset wajib sama dengan receivedSize di server; jika tidak → 409 UPLOAD_OFFSET_MISMATCH.")
    @PutMapping(value = "/sessions/{sessionId}", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public UploadSessionResponse appendChunk(@PathVariable UUID sessionId, @RequestParam long offset,
                                             HttpServletRequest request) throws IOException {
        // Body dibaca langsung sebagai stream agar potongan tidak perlu ditampung utuh di memori.
        return uploadService.appendChunk(currentUser.id(), sessionId, offset, request.getInputStream());
    }

    @Operation(summary = "Upload selesai: mulai pengecekan file (langkah 2)")
    @PostMapping("/sessions/{sessionId}/complete")
    public UploadStartedResponse complete(@PathVariable UUID sessionId) {
        return uploadService.complete(currentUser.id(), sessionId);
    }

    @Operation(summary = "Status & hasil pengecekan satu upload (dipantau app selama pengecekan berjalan)")
    @GetMapping("/{templateId}/check")
    public UploadCheckResponse progress(@PathVariable UUID templateId) {
        return uploadService.progress(currentUser.id(), templateId);
    }

    @Operation(summary = "Hapus draft atau upload yang gagal pengecekan")
    @DeleteMapping("/{templateId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID templateId) {
        uploadService.delete(currentUser.id(), templateId);
    }

    @Operation(summary = "\"Ini keliru? Laporkan\" untuk satu Error",
            description = "Satu laporan per Error per upload (ALREADY_REPORTED). Template tidak otomatis lolos.")
    @PostMapping("/issues/{issueId}/report")
    @ResponseStatus(HttpStatus.CREATED)
    public void report(@PathVariable UUID issueId, @Valid @RequestBody(required = false) ReportIssueRequest request) {
        uploadService.report(currentUser.id(), issueId, request == null ? null : request.reason());
    }

    // ---------- draft: langkah 3 dan seterusnya ----------

    @Operation(summary = "Pengaturan upload untuk app (batas ukuran, potongan, host luar yang boleh dimuat WebView)")
    @GetMapping("/settings")
    public UploadSettingsResponse settings() {
        return draftService.settings(currentUser.id());
    }

    @Operation(summary = "Isi draft (Info template, thumbnail, info teknis, langkah terakhir)")
    @GetMapping("/{templateId}")
    public DraftResponse draft(@PathVariable UUID templateId) {
        return draftService.draft(currentUser.id(), templateId);
    }

    @Operation(summary = "Simpan otomatis Info template (field null tidak diubah)")
    @PatchMapping("/{templateId}/info")
    public DraftResponse updateInfo(@PathVariable UUID templateId, @Valid @RequestBody DraftInfoRequest request) {
        return draftService.updateInfo(currentUser.id(), templateId, request);
    }

    @Operation(summary = "Catat langkah wizard yang sedang dibuka (3–6)")
    @PatchMapping("/{templateId}/step")
    public DraftResponse updateStep(@PathVariable UUID templateId, @Valid @RequestBody DraftStepRequest request) {
        return draftService.updateStep(currentUser.id(), templateId, request.step());
    }

    @Operation(summary = "Ganti thumbnail (body = gambar JPG/PNG/WebP, maks 1 MB)",
            description = "source: auto · section · custom; view: mobile · desktop")
    @PutMapping(value = "/{templateId}/thumbnail", consumes = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE,
            "image/webp", MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public DraftResponse updateThumbnail(@PathVariable UUID templateId, @RequestParam String source,
                                         @RequestParam String view, HttpServletRequest request) throws IOException {
        byte[] image = request.getInputStream().readNBytes(DraftService.MAX_THUMBNAIL_BYTES + 1);
        return draftService.updateThumbnail(currentUser.id(), templateId, image, source, view);
    }

    @Operation(summary = "Unduh ZIP template untuk ditampilkan di HP")
    @GetMapping(value = "/{templateId}/source", produces = "application/zip")
    public ResponseEntity<Resource> source(@PathVariable UUID templateId) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/zip"))
                .body(new FileSystemResource(draftService.source(currentUser.id(), templateId)));
    }

    @Operation(summary = "Peringatan dari WebView HP (tahap C): error JavaScript & tampilan melebar")
    @PutMapping("/{templateId}/device-warnings")
    public DraftResponse deviceWarnings(@PathVariable UUID templateId, @Valid @RequestBody DeviceWarningsRequest request) {
        return draftService.deviceWarnings(currentUser.id(), templateId, request);
    }
}
