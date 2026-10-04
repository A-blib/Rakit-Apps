package com.aris.templateapp.provider;

import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.provider.dto.DailyCountResponse;
import com.aris.templateapp.provider.dto.ProviderDashboardResponse;
import com.aris.templateapp.provider.dto.ProviderProfileResponse;
import com.aris.templateapp.provider.dto.ProviderProfileUpdateRequest;
import com.aris.templateapp.security.CurrentUser;
import com.aris.templateapp.template.ProviderTemplateService;
import com.aris.templateapp.template.dto.TemplateDetailResponse;
import com.aris.templateapp.template.dto.TemplateListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
            description = "status = all|published|needs_fix|checking|draft|disabled · category = all|sekolah|organisasi|umkm|instansi|pribadi|lainnya · sort = updated|downloads|views|name · page dimulai dari 0")
    @GetMapping("/templates")
    public TemplateListResponse templates(@RequestParam(defaultValue = "all") String status,
                                          @RequestParam(defaultValue = "all") String category,
                                          @RequestParam(defaultValue = "updated") String sort,
                                          @RequestParam(required = false) String q,
                                          @RequestParam(defaultValue = "0") int page) {
        return templateService.list(currentUser.id(), status, category, sort, q, page);
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
