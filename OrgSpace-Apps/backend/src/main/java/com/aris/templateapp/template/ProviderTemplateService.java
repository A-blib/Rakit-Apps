package com.aris.templateapp.template;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.organization.OrgCategory;
import com.aris.templateapp.provider.ProviderAccess;
import com.aris.templateapp.template.dto.CheckResponse;
import com.aris.templateapp.template.dto.StatusCountsResponse;
import com.aris.templateapp.template.dto.TemplateDetailResponse;
import com.aris.templateapp.template.dto.TemplateListResponse;
import com.aris.templateapp.template.dto.TemplateSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** "Template Anda": daftar dengan filter/urutan/pencarian/paginasi dan detail (alur-provider.md bagian 6). */
@Service
@RequiredArgsConstructor
public class ProviderTemplateService {

    static final int PAGE_SIZE = 20;

    private final ProviderAccess providerAccess;
    private final ProviderTemplateQueries queries;
    private final TemplateRepository templateRepository;
    private final CheckResponses checkResponses;
    private final TemplateFolderRepository folderRepository;

    /**
     * @param folder   null/kosong = semua template · "root" = hanya yang tanpa folder (halaman utama) · id folder
     * @param status   all · published · needs_fix · checking · draft · disabled (bawaan all)
     * @param category nilai kategori atau null/"all" untuk semua
     * @param sort     updated · downloads · views · name (bawaan updated)
     * @param query    kata kunci nama template (boleh kosong)
     * @param page     dimulai dari 0
     */
    @Transactional(readOnly = true)
    public TemplateListResponse list(UUID userId, String folder, String status, String category, String sort,
                                     String query, int page) {
        providerAccess.requireActive(userId);
        ProviderTemplateQueries.FolderScope folderScope = folderScope(userId, folder);
        ProviderTemplateQueries.StatusFilter statusFilter;
        ProviderTemplateQueries.SortOrder sortOrder;
        OrgCategory categoryFilter;
        try {
            statusFilter = ProviderTemplateQueries.StatusFilter.from(status == null ? "all" : status);
            sortOrder = ProviderTemplateQueries.SortOrder.from(sort == null ? "updated" : sort);
            categoryFilter = category == null || category.equals("all") ? null
                    : PersistableEnum.fromValue(OrgCategory.class, category);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, e.getMessage());
        }
        if (page < 0) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Halaman tidak boleh negatif.");
        }
        String search = query == null || query.isBlank() ? null : query.trim();

        List<TemplateSummaryResponse> items =
                queries.list(userId, folderScope, statusFilter, categoryFilter, search, sortOrder, page, PAGE_SIZE);
        // Di halaman utama, jumlah per status tetap menghitung SEMUA template (termasuk yang di dalam folder),
        // karena memilih status di menu + menampilkan template dari semua folder. Jumlah untuk paginasi
        // tetap mengikuti isi halaman utama saja.
        boolean root = folderScope.rootOnly();
        StatusCountsResponse counts = queries.countByStatus(userId,
                root ? ProviderTemplateQueries.FolderScope.ANY : folderScope, categoryFilter, search);
        StatusCountsResponse scoped = root ? queries.countByStatus(userId, folderScope, categoryFilter, search) : counts;
        long total = switch (statusFilter) {
            case ALL -> scoped.all();
            case PUBLISHED -> scoped.published();
            case NEEDS_FIX -> scoped.needsFix();
            case CHECKING -> scoped.checking();
            case DRAFT -> scoped.draft();
            case DISABLED -> scoped.disabled();
        };
        int totalPages = (int) ((total + PAGE_SIZE - 1) / PAGE_SIZE);
        return new TemplateListResponse(items, page, PAGE_SIZE, total, totalPages, counts);
    }

    private ProviderTemplateQueries.FolderScope folderScope(UUID userId, String folder) {
        if (folder == null || folder.isBlank()) {
            return ProviderTemplateQueries.FolderScope.ANY;
        }
        if (folder.equals("root")) {
            return ProviderTemplateQueries.FolderScope.ROOT;
        }
        UUID folderId;
        try {
            folderId = UUID.fromString(folder);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Folder tidak dikenal: " + folder);
        }
        // Folder milik provider lain diperlakukan seperti tidak ada.
        folderRepository.findByIdAndProviderId(folderId, userId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        return ProviderTemplateQueries.FolderScope.of(folderId);
    }

    @Transactional(readOnly = true)
    public TemplateDetailResponse detail(UUID userId, UUID templateId) {
        providerAccess.requireActive(userId);
        Template template = ownedTemplate(userId, templateId);
        long[] totals = queries.totals(templateId);
        return new TemplateDetailResponse(template.getId(), template.getName(), template.getCategory(),
                template.getThumbnailUrl(), template.getStatus(), template.getWarningCount(), totals[0], totals[1],
                template.getCreatedAt(), template.getUpdatedAt(), template.getPublishedAt(), latestCheckOf(templateId));
    }

    /** GET /templates/{id}/checks/latest: hanya pemilik template yang boleh melihat. */
    @Transactional(readOnly = true)
    public CheckResponse latestCheck(UUID userId, UUID templateId) {
        ownedTemplate(userId, templateId);
        CheckResponse check = latestCheckOf(templateId);
        if (check == null) {
            throw new ApiException(ErrorCode.NOT_FOUND, "Template ini belum pernah dicek.");
        }
        return check;
    }

    private Template ownedTemplate(UUID userId, UUID templateId) {
        // Template milik provider lain diperlakukan seperti tidak ada, agar keberadaannya tidak bocor.
        return templateRepository.findByIdAndProviderId(templateId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
    }

    private CheckResponse latestCheckOf(UUID templateId) {
        return checkResponses.latest(templateId);
    }
}
