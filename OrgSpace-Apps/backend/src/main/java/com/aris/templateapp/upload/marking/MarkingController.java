package com.aris.templateapp.upload.marking;

import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.security.CurrentUser;
import com.aris.templateapp.upload.publish.PublishService;
import com.aris.templateapp.upload.publish.SubmitRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Upload", description = "Upload template provider: ZIP per potongan, pengecekan, draft (alur-fitur-upload.md)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/providers/me/uploads/{templateId}")
@RequiredArgsConstructor
public class MarkingController {

    private final MarkingService markingService;
    private final PublishService publishService;
    private final CurrentUser currentUser;

    @Operation(summary = "ZIP salinan bernomor data-tpl-id untuk mode tandai di HP")
    @GetMapping(value = "/work-package", produces = "application/zip")
    public ResponseEntity<Resource> workPackage(@PathVariable UUID templateId) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/zip"))
                .body(new FileSystemResource(markingService.workPackage(currentUser.id(), templateId)));
    }

    @Operation(summary = "Tandaan yang tersimpan (kosong jika belum pernah disimpan)")
    @GetMapping("/marking")
    public ResponseEntity<MarkingData> marking(@PathVariable UUID templateId) {
        MarkingData data = markingService.marking(currentUser.id(), templateId);
        return data == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(data);
    }

    @Operation(summary = "Simpan tandaan (tombol Simpan di langkah 4)")
    @PutMapping("/marking")
    public MarkingData save(@PathVariable UUID templateId, @RequestBody MarkingData data) {
        return markingService.save(currentUser.id(), templateId, data);
    }

    @Operation(summary = "Kirim template (langkah 6)",
            description = "Syarat: info lengkap, minimal 3 isian tersimpan, agreedAssetRights = true. Pengecekan akhir & "
                    + "pembuatan paket berjalan di latar belakang; pantau lewat GET /check sampai status bukan checking.")
    @PostMapping("/submit")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void submit(@PathVariable UUID templateId, @RequestBody SubmitRequest request) {
        publishService.submit(currentUser.id(), templateId, request);
    }
}
