package com.aris.templateapp.project;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.project.dto.DeleteWebsiteRequest;
import com.aris.templateapp.project.dto.DuplicateWebsiteRequest;
import com.aris.templateapp.project.dto.PublishRequestRequest;
import com.aris.templateapp.project.dto.PublishRequestResponse;
import com.aris.templateapp.project.dto.RenameWebsiteRequest;
import com.aris.templateapp.project.dto.WebsiteDetailResponse;
import com.aris.templateapp.project.dto.WebsiteListResponse;
import com.aris.templateapp.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Website", description = "Tab Website dan halaman kelola website (ruang kerja aktif)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WebsiteController {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final WebsiteService websiteService;
    private final CurrentUser currentUser;

    @Operation(summary = "Daftar website di ruang kerja + jumlah per status",
            description = "Semua anggota. Status dihitung server: draft, online (+ hasUnpublishedChanges), offline, "
                    + "suspended.")
    @GetMapping("/organizations/{orgId}/websites")
    public WebsiteListResponse list(
            @PathVariable UUID orgId,
            @Parameter(description = "all (bawaan), draft, online, changed") @RequestParam(defaultValue = "all")
            String status,
            @Parameter(description = "Cari berdasarkan nama website") @RequestParam(required = false) String q,
            @Parameter(description = "updated (bawaan), newest, name") @RequestParam(defaultValue = "updated")
            String sort,
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "1–50, bawaan 20") @RequestParam(required = false) Integer size) {
        WebsiteQueries.Filter filter;
        WebsiteQueries.Sort order;
        try {
            filter = WebsiteQueries.Filter.from(status);
            order = WebsiteQueries.Sort.from(sort);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, e.getMessage());
        }
        int pageSize = size == null ? DEFAULT_SIZE : size;
        if (page < 0 || pageSize < 1 || pageSize > MAX_SIZE) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR,
                    "Halaman tidak boleh negatif dan size harus 1–" + MAX_SIZE + ".");
        }
        String search = q == null || q.isBlank() ? null : q.trim();
        return websiteService.list(orgId, currentUser.id(), filter, search, order, page, pageSize);
    }

    @Operation(summary = "Detail website (halaman kelola & editor)")
    @GetMapping("/projects/{id}")
    public WebsiteDetailResponse detail(@PathVariable UUID id) {
        return websiteService.detail(id, currentUser.id());
    }

    @Operation(summary = "Ganti nama website", description = "Semua anggota (Editor ke atas).")
    @PatchMapping("/projects/{id}")
    public WebsiteDetailResponse rename(@PathVariable UUID id, @Valid @RequestBody RenameWebsiteRequest request) {
        return websiteService.rename(id, currentUser.id(), request.name());
    }

    @Operation(summary = "Duplikat website menjadi draft baru",
            description = "Nama kosong = \"{nama} (2)\". Aset gambar ikut disalin.")
    @PostMapping("/projects/{id}/duplicate")
    @ResponseStatus(HttpStatus.CREATED)
    public WebsiteDetailResponse duplicate(@PathVariable UUID id,
                                           @Valid @RequestBody(required = false) DuplicateWebsiteRequest request) {
        return websiteService.duplicate(id, currentUser.id(), request == null ? null : request.name());
    }

    @Operation(summary = "Hapus website",
            description = "Pemilik. Website yang pernah diterbitkan wajib confirmName (400 CONFIRM_NAME_MISMATCH). "
                    + "Pembuat boleh menghapus draft barunya yang belum pernah diubah.")
    @DeleteMapping("/projects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @RequestBody(required = false) DeleteWebsiteRequest request) {
        websiteService.delete(id, currentUser.id(), request == null ? null : request.confirmName());
    }

    @Operation(summary = "Minta diterbitkan (Editor → Admin/Pemilik)",
            description = "Permintaan terbuka yang sudah ada diperbarui, bukan ditambah.")
    @PostMapping("/projects/{id}/publish-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public PublishRequestResponse requestPublish(@PathVariable UUID id,
                                                 @Valid @RequestBody(required = false) PublishRequestRequest request) {
        return websiteService.requestPublish(id, currentUser.id(), request == null ? null : request.note());
    }

    @Operation(summary = "Permintaan terbit yang masih terbuka", description = "Admin & Pemilik (banner Beranda).")
    @GetMapping("/organizations/{orgId}/publish-requests")
    public List<PublishRequestResponse> openRequests(@PathVariable UUID orgId) {
        return websiteService.openRequests(orgId, currentUser.id());
    }

    @Operation(summary = "Tutup permintaan terbit tanpa menerbitkan", description = "Admin & Pemilik.")
    @PostMapping("/publish-requests/{id}/dismiss")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void dismiss(@PathVariable UUID id) {
        websiteService.dismissRequest(id, currentUser.id());
    }
}
