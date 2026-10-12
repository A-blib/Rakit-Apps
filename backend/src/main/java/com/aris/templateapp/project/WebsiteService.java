package com.aris.templateapp.project;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.organization.ActivityService;
import com.aris.templateapp.organization.MemberRole;
import com.aris.templateapp.organization.OrgAction;
import com.aris.templateapp.organization.OrganizationAccess;
import com.aris.templateapp.project.dto.PublishRequestResponse;
import com.aris.templateapp.project.dto.WebsiteDetailResponse;
import com.aris.templateapp.project.dto.WebsiteListResponse;
import com.aris.templateapp.project.dto.WebsiteSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Tab Website dan halaman kelola website (alur-pembuatan-website.md 4–5): daftar, detail, ganti nama, duplikat,
 * hapus, dan "Minta diterbitkan". Isi editor (sinkron, kunci edit, aset) ada di service project fase T2.
 * <p>
 * Setiap method memulai dengan {@link OrganizationAccess#require}; project selalu dicek milik ruang kerja itu.
 */
@Service
@RequiredArgsConstructor
public class WebsiteService {

    private final ProjectRepository projectRepository;
    private final WebsiteQueries queries;
    private final OrganizationAccess access;
    private final ActivityService activityService;
    private final AppProperties properties;
    private final JdbcClient jdbc;

    @Transactional(readOnly = true)
    public WebsiteListResponse list(UUID organizationId, UUID userId, WebsiteQueries.Filter filter, String search,
                                    WebsiteQueries.Sort sort, int page, int size) {
        OrganizationAccess.Membership membership = access.require(organizationId, userId, OrgAction.VIEW);
        // Ambil satu baris lebih untuk tahu masih ada halaman berikutnya tanpa query hitung tambahan.
        List<WebsiteSummaryResponse> rows = queries.list(organizationId, userId, filter, search, sort, page, size + 1);
        boolean hasMore = rows.size() > size;
        int limit = membership.organization().isPersonal() ? properties.sites().maxPerPersonal()
                : properties.sites().maxPerOrg();
        return new WebsiteListResponse(hasMore ? rows.subList(0, size) : rows, queries.counts(organizationId, search),
                queries.sitesUsed(organizationId), limit, page, hasMore);
    }

    @Transactional(readOnly = true)
    public WebsiteDetailResponse detail(UUID projectId, UUID userId) {
        Project project = load(projectId, userId, OrgAction.VIEW);
        return detailOf(project, userId);
    }

    @Transactional
    public WebsiteDetailResponse rename(UUID projectId, UUID userId, String name) {
        Project project = load(projectId, userId, OrgAction.EDIT_DRAFT);
        String newName = name.trim();
        if (!newName.equals(project.getName())) {
            String oldName = project.getName();
            project.setName(newName);
            project.touch(userId);
            activityService.log(project.getOrganizationId(), userId, "website.renamed", "project", projectId,
                    Map.of("from", oldName, "to", newName));
        }
        return detailOf(projectRepository.saveAndFlush(project), userId);
    }

    /**
     * Salinan berupa website draft baru dengan isi yang sama. Aset ikut disalin sebagai baris baru (file fisiknya
     * dipakai bersama), karena isi project hanya boleh merujuk aset miliknya sendiri.
     */
    @Transactional
    public WebsiteDetailResponse duplicate(UUID projectId, UUID userId, String requestedName) {
        Project source = load(projectId, userId, OrgAction.EDIT_DRAFT);
        String base = requestedName == null || requestedName.isBlank() ? source.getName() : requestedName.trim();
        Project copy = new Project(source.getOrganizationId(), uniqueName(source.getOrganizationId(), base),
                source.getMode(), userId);
        copy.setSourceTemplateId(source.getSourceTemplateId());
        copy.setTemplateVersion(source.getTemplateVersion());
        copy.setMissingCount(source.getMissingCount());
        // Versi 1 (bukan 0): salinan bukan "website baru yang masih kosong".
        copy.setContentVersion(1);
        copy.setContent(new HashMap<>(source.getContent()));
        copy = projectRepository.saveAndFlush(copy);

        Map<UUID, UUID> assetIds = copyAssets(source.getId(), copy.getId());
        if (!assetIds.isEmpty()) {
            copy.setContent(ContentAssets.remap(copy.getContent(), assetIds));
            if (source.getThumbnailAssetId() != null) {
                copy.setThumbnailAssetId(assetIds.get(source.getThumbnailAssetId()));
            }
            copy = projectRepository.saveAndFlush(copy);
        }
        activityService.log(copy.getOrganizationId(), userId, "website.duplicated", "project", copy.getId(),
                Map.of("from", source.getName(), "name", copy.getName()));
        return detailOf(copy, userId);
    }

    /**
     * Hapus website. Pemilik boleh menghapus website mana pun (yang pernah terbit wajib mengetik ulang namanya);
     * pembuat website boleh menghapus draft barunya yang belum pernah diubah (website kosong, rancangan T 6.7).
     */
    @Transactional
    public void delete(UUID projectId, UUID userId, String confirmName) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND));
        MemberRole role = access.require(project.getOrganizationId(), userId, OrgAction.VIEW).role();
        boolean published = queries.hasSite(projectId);
        boolean ownEmptyDraft = !published && project.getContentVersion() == 0 && userId.equals(project.getCreatedBy());
        if (!OrgAction.DELETE_SITE.allowedFor(role) && !ownEmptyDraft) {
            throw new ApiException(ErrorCode.ROLE_NOT_ALLOWED);
        }
        if (published && (confirmName == null || !confirmName.trim().equalsIgnoreCase(project.getName().trim()))) {
            throw new ApiException(ErrorCode.CONFIRM_NAME_MISMATCH);
        }
        projectRepository.delete(project);
        // Website kosong yang dihapus otomatis tidak perlu memenuhi Riwayat aktivitas.
        if (!ownEmptyDraft) {
            activityService.log(project.getOrganizationId(), userId, "website.deleted", "project", projectId,
                    Map.of("name", project.getName()));
        }
    }

    /** Editor meminta Admin/Pemilik menerbitkan. Permintaan terbuka yang sudah ada diperbarui, bukan ditambah. */
    @Transactional
    public PublishRequestResponse requestPublish(UUID projectId, UUID userId, String note) {
        Project project = load(projectId, userId, OrgAction.EDIT_DRAFT);
        String cleanNote = note == null || note.isBlank() ? null : note.trim();
        int updated = jdbc.sql("""
                        UPDATE publish_requests SET requested_by = :user, note = :note, created_at = now()
                         WHERE project_id = :project AND status = 'open'
                        """)
                .param("user", userId).param("note", cleanNote).param("project", projectId)
                .update();
        if (updated == 0) {
            jdbc.sql("""
                            INSERT INTO publish_requests (project_id, organization_id, requested_by, note)
                            VALUES (:project, :org, :user, :note)
                            """)
                    .param("project", projectId).param("org", project.getOrganizationId())
                    .param("user", userId).param("note", cleanNote)
                    .update();
        }
        activityService.log(project.getOrganizationId(), userId, "website.publish_requested", "project", projectId,
                Map.of("name", project.getName()));
        return queries.openRequest(projectId).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<PublishRequestResponse> openRequests(UUID organizationId, UUID userId) {
        access.require(organizationId, userId, OrgAction.PUBLISH);
        return queries.openRequests(organizationId);
    }

    @Transactional
    public void dismissRequest(UUID requestId, UUID userId) {
        UUID organizationId = jdbc.sql("SELECT organization_id FROM publish_requests WHERE id = :id AND status = 'open'")
                .param("id", requestId)
                .query(UUID.class)
                .optional()
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        access.require(organizationId, userId, OrgAction.PUBLISH);
        jdbc.sql("UPDATE publish_requests SET status = 'dismissed', handled_by = :user, handled_at = now() WHERE id = :id")
                .param("user", userId).param("id", requestId)
                .update();
    }

    /** Memuat project dan memastikan user anggota ruang kerja pemiliknya dengan peran yang cukup. */
    private Project load(UUID projectId, UUID userId, OrgAction action) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND));
        access.require(project.getOrganizationId(), userId, action);
        return project;
    }

    private WebsiteDetailResponse detailOf(Project project, UUID userId) {
        WebsiteSummaryResponse summary = queries.find(project.getId(), userId).orElseThrow();
        WebsiteQueries.SiteInfo site = queries.siteInfo(project.getId()).orElse(null);
        return new WebsiteDetailResponse(summary, project.getContent(), project.getContentVersion(),
                site == null ? null : site.publishedAt(), queries.openRequest(project.getId()).orElse(null),
                site == null ? null : site.suspensionReason());
    }

    /** "Aksara" sudah ada → "Aksara (2)", lalu "(3)", dst. (rancangan T bagian 3, nama awal). */
    String uniqueName(UUID organizationId, String base) {
        String trimmed = base.length() > 94 ? base.substring(0, 94).trim() : base;
        List<String> taken = queries.namesStartingWith(organizationId, trimmed).stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .toList();
        if (!taken.contains(trimmed.toLowerCase(Locale.ROOT))) {
            return trimmed;
        }
        for (int n = 2; ; n++) {
            String candidate = trimmed + " (" + n + ")";
            if (!taken.contains(candidate.toLowerCase(Locale.ROOT))) {
                return candidate;
            }
        }
    }

    private Map<UUID, UUID> copyAssets(UUID fromProjectId, UUID toProjectId) {
        Map<UUID, UUID> ids = new HashMap<>();
        jdbc.sql("SELECT id FROM project_assets WHERE project_id = :id")
                .param("id", fromProjectId)
                .query(UUID.class)
                .list()
                .forEach(oldId -> ids.put(oldId, jdbc.sql("""
                                INSERT INTO project_assets (organization_id, project_id, sha256, mime_type, width, height,
                                                            size_bytes, storage_path, created_by)
                                SELECT organization_id, :to, sha256, mime_type, width, height, size_bytes, storage_path,
                                       created_by
                                  FROM project_assets WHERE id = :old
                                RETURNING id
                                """)
                        .param("to", toProjectId).param("old", oldId)
                        .query(UUID.class)
                        .single()));
        return ids;
    }
}
