package com.aris.templateapp.provider;

import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.provider.dto.DailyCountResponse;
import com.aris.templateapp.provider.dto.ProviderDashboardResponse;
import com.aris.templateapp.provider.dto.ProviderProfileResponse;
import com.aris.templateapp.provider.dto.ProviderProfileUpdateRequest;
import com.aris.templateapp.security.CurrentUser;
import com.aris.templateapp.template.ProviderTemplateService;
import com.aris.templateapp.template.TemplateFolderService;
import com.aris.templateapp.template.dto.FolderRequest;
import com.aris.templateapp.template.dto.FolderResponse;
import com.aris.templateapp.template.dto.MoveTemplateRequest;
import com.aris.templateapp.template.dto.TemplateDetailResponse;
import com.aris.templateapp.template.dto.TemplateListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Dashboard Provider: Beranda, Template Anda, Profil. Semua endpoint butuh profil provider yang aktif
 * (403 PROVIDER_REQUIRED / PROVIDER_SUSPENDED).
 */
@Tag(name = "Provider", description = "Beranda, Template Anda, dan Profil penyedia template")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/providers/me")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderDashboardService dashboardService;
    private final ProviderTemplateService templateService;
    private final ProviderProfileService profileService;
    private final TemplateFolderService folderService;
    private final CurrentUser currentUser;

    @Operation(summary = "Data Beranda: perlu tindakan, ringkasan, tren download, template populer",
            description = "period = 7d (bawaan) atau 30d")
    @GetMapping("/dashboard")
    public ProviderDashboardResponse dashboard(@RequestParam(defaultValue = "7d") String period) {
        return dashboardService.dashboard(currentUser.id(), period);
    }

    @Operation(summary = "Jumlah download per hari (hari tanpa download bernilai 0)")
    @GetMapping("/stats/downloads")
    public List<DailyCountResponse> downloadTrend(@RequestParam(defaultValue = "7d") String period) {
        return dashboardService.downloadTrend(currentUser.id(), period);
    }

    @Operation(summary = "Daftar Template Anda dengan filter, urutan, pencarian, dan paginasi (20 per halaman)",
            description = "folder = (kosong) semua template | root = hanya yang tanpa folder | id folder · status = all|published|needs_fix|checking|draft|disabled · category = all|sekolah|organisasi|umkm|instansi|pribadi|lainnya · sort = updated|downloads|views|name · page dimulai dari 0")
    @GetMapping("/templates")
    public TemplateListResponse templates(@RequestParam(required = false) String folder,
                                          @RequestParam(defaultValue = "all") String status,
                                          @RequestParam(defaultValue = "all") String category,
                                          @RequestParam(defaultValue = "updated") String sort,
                                          @RequestParam(required = false) String q,
                                          @RequestParam(defaultValue = "0") int page) {
        return templateService.list(currentUser.id(), folder, status, category, sort, q, page);
    }

    @Operation(summary = "Pindahkan template ke folder", description = "folderId null = keluarkan dari folder")
    @PutMapping("/templates/{id}/folder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void moveTemplate(@PathVariable UUID id, @RequestBody MoveTemplateRequest request) {
        folderService.moveTemplate(currentUser.id(), id, request.folderId());
    }

    @Operation(summary = "Daftar folder Template Anda (urut nama) + jumlah template di dalamnya")
    @GetMapping("/template-folders")
    public List<FolderResponse> folders() {
        return folderService.list(currentUser.id());
    }

    @Operation(summary = "Buat folder (nama unik per provider, maks 60 karakter, maks 100 folder)")
    @PostMapping("/template-folders")
    @ResponseStatus(HttpStatus.CREATED)
    public FolderResponse createFolder(@Valid @RequestBody FolderRequest request) {
        return folderService.create(currentUser.id(), request.name());
    }

    @Operation(summary = "Ganti nama folder")
    @PatchMapping("/template-folders/{id}")
    public FolderResponse renameFolder(@PathVariable UUID id, @Valid @RequestBody FolderRequest request) {
        return folderService.rename(currentUser.id(), id, request.name());
    }

    @Operation(summary = "Hapus folder", description = "Template di dalamnya kembali ke halaman utama, tidak ikut terhapus.")
    @DeleteMapping("/template-folders/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFolder(@PathVariable UUID id) {
        folderService.delete(currentUser.id(), id);
    }

    @Operation(summary = "Detail template + hasil pengecekan terakhir")
    @GetMapping("/templates/{id}")
    public TemplateDetailResponse template(@PathVariable UUID id) {
        return templateService.detail(currentUser.id(), id);
    }

    @Operation(summary = "Profil provider + ringkasan singkat")
    @GetMapping("/profile")
    public ProviderProfileResponse profile() {
        return profileService.get(currentUser.id());
    }

    @Operation(summary = "Edit profil provider")
    @PatchMapping("/profile")
    public ProviderProfileResponse updateProfile(@Valid @RequestBody ProviderProfileUpdateRequest request) {
        return profileService.update(currentUser.id(), request);
    }
}
