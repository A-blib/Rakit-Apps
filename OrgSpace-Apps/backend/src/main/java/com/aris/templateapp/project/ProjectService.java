package com.aris.templateapp.project;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.gallery.TemplatePackageService;
import com.aris.templateapp.organization.ActivityService;
import com.aris.templateapp.organization.OrgAction;
import com.aris.templateapp.organization.OrgLogoStorage;
import com.aris.templateapp.organization.Organization;
import com.aris.templateapp.organization.OrganizationAccess;
import com.aris.templateapp.project.dto.AssetResponse;
import com.aris.templateapp.project.dto.BackupResponse;
import com.aris.templateapp.project.dto.ContentVersionResponse;
import com.aris.templateapp.project.dto.LockResponse;
import com.aris.templateapp.project.dto.WebsiteDetailResponse;
import com.aris.templateapp.template.Template;
import com.aris.templateapp.template.TemplateRepository;
import com.aris.templateapp.upload.marking.MarkingData;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Website di cloud (alur-buat-website-via-template.md 11–12): membuat website dari template (dengan isi otomatis dari
 * Profil organisasi), menyimpan isi dari HP, kunci edit, gambar, thumbnail, dan cadangan. Daftar, ganti nama,
 * duplikat, dan hapus ada di {@link WebsiteService}.
 */
@Service
@RequiredArgsConstructor
public class ProjectService {

    private static final int BACKUPS_SHOWN = 5;

    private final ProjectRepository projectRepository;
    private final WebsiteService websiteService;
    private final TemplatePackageService templates;
    private final TemplateRepository templateRepository;
    private final OrganizationAccess access;
    private final ProjectLocks locks;
    private final ProjectAssetStorage assetStorage;
    private final OrgLogoStorage logoStorage;
    private final ActivityService activityService;
    private final AppProperties properties;
    private final JdbcClient jdbc;
    private final JsonMapper jsonMapper;
    private final Clock clock;

    /**
     * "Pakai template": website dibuat saat itu juga (bukan menunggu perubahan pertama), karena server mengisi data
     * Profil organisasi dan memberi kunci edit kepada pembuatnya.
     */
    @Transactional
    public WebsiteDetailResponse create(UUID organizationId, UUID userId, UUID templateId, int templateVersion,
                                        String requestedName, String deviceId) {
        Organization org = access.require(organizationId, userId, OrgAction.EDIT_DRAFT).organization();
        Template template = templates.visible(templateId);
        if (template.getPackageVersion() != templateVersion) {
            // HP memegang paket lama: minta unduh ulang versi terbaru dulu (layar Unduh menanganinya).
            throw new ApiException(ErrorCode.TEMPLATE_NOT_FOUND, "Template sudah diperbarui. Unduh versi terbarunya dulu.");
        }
        String base = requestedName == null || requestedName.isBlank() ? template.getName() : requestedName.strip();
        Project project = new Project(organizationId, websiteService.uniqueName(organizationId, base),
                ProjectMode.TEMPLATE, userId);
        project.setSourceTemplateId(templateId);
        project.setTemplateVersion(templateVersion);
        project = projectRepository.saveAndFlush(project);

        UUID projectId = project.getId();
        project.setContent(ProfileAutofill.content(template.getMarking(), org, () -> copyLogo(org, projectId, userId)));
        project = projectRepository.saveAndFlush(project);
        locks.acquire(projectId, userId, deviceId);
        activityService.log(organizationId, userId, "website.created", "project", projectId,
                Map.of("name", project.getName(), "template", template.getName()));
        return websiteService.detailOf(project, userId);
    }

    /** Menyimpan isi dari HP: pemegang kunci saja, versi dasar harus sama dengan versi di server. */
    @Transactional
    public ContentVersionResponse updateContent(UUID projectId, UUID userId, int baseVersion,
                                                Map<String, Object> content, int missingCount) {
        Project project = load(projectId, userId, OrgAction.EDIT_DRAFT);
        locks.requireHeldBy(projectId, userId);
        if (project.getContentVersion() != baseVersion) {
            throw new ApiException(ErrorCode.VERSION_CONFLICT)
                    .withDetails(Map.of("contentVersion", project.getContentVersion()));
        }
        ContentValidator.validate(content, marking(project), asset -> ownsAsset(projectId, asset));
        project.setContent(content);
        project.setContentVersion(project.getContentVersion() + 1);
        project.setContentUpdatedAt(clock.instant());
        project.setMissingCount(missingCount);
        project.touch(userId);
        projectRepository.saveAndFlush(project);
        return new ContentVersionResponse(project.getContentVersion());
    }

    @Transactional
    public LockResponse lock(UUID projectId, UUID userId, String deviceId) {
        load(projectId, userId, OrgAction.EDIT_DRAFT);
        return new LockResponse(locks.acquire(projectId, userId, deviceId));
    }

    @Transactional
    public void unlock(UUID projectId, UUID userId) {
        load(projectId, userId, OrgAction.VIEW);
        locks.release(projectId, userId);
    }

    /** Ambil alih (E2: Admin & Pemilik). Perubahan pemegang lama yang belum terkirim menjadi cadangan di HP-nya. */
    @Transactional
    public LockResponse takeOver(UUID projectId, UUID userId, String deviceId) {
        Project project = load(projectId, userId, OrgAction.PUBLISH);
        String previous = locks.current(projectId, false).map(ProjectLocks.Holder::name).orElse(null);
        Instant expires = locks.takeOver(projectId, userId, deviceId);
        activityService.log(project.getOrganizationId(), userId, "website.lock_taken", "project", projectId,
                previous == null ? Map.of("name", project.getName()) : Map.of("name", project.getName(), "from", previous));
        return new LockResponse(expires);
    }

    /** Gambar website (pemegang kunci). Gambar yang sama persis tidak menambah file maupun hitungan penyimpanan. */
    @Transactional
    public AssetResponse uploadAsset(UUID projectId, UUID userId, byte[] bytes) {
        Project project = load(projectId, userId, OrgAction.EDIT_DRAFT);
        locks.requireHeldBy(projectId, userId);
        return saveAsset(project, userId, bytes);
    }

    /** Thumbnail kartu website, ditangkap HP dari pratinjau saat editor ditutup. Tidak menggeser "Diubah ...". */
    @Transactional
    public void uploadThumbnail(UUID projectId, UUID userId, byte[] bytes) {
        Project project = load(projectId, userId, OrgAction.EDIT_DRAFT);
        AssetResponse asset = saveAsset(project, userId, bytes);
        jdbc.sql("UPDATE projects SET thumbnail_asset_id = :asset WHERE id = :id")
                .param("asset", asset.assetId()).param("id", projectId)
                .update();
    }

    /** File gambar untuk anggota ruang kerja pemilik website. */
    @Transactional(readOnly = true)
    public StoredAsset asset(UUID assetId, UUID userId) {
        StoredAsset asset = jdbc.sql("""
                        SELECT organization_id, storage_path, mime_type FROM project_assets WHERE id = :id""")
                .param("id", assetId)
                .query((rs, row) -> new StoredAsset(rs.getObject("organization_id", UUID.class),
                        assetStorage.file(rs.getString("storage_path")), rs.getString("mime_type")))
                .optional()
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        access.require(asset.organizationId(), userId, OrgAction.VIEW);
        if (!Files.isRegularFile(asset.file())) {
            throw new ApiException(ErrorCode.NOT_FOUND);
        }
        return asset;
    }

    public record StoredAsset(UUID organizationId, Path file, String mimeType) {
    }

    /** Foto modul dari Portal Pengurus; izin sudah diperiksa lewat token portal, jadi tanpa kunci edit. */
    @Transactional
    public AssetResponse saveSiteMedia(UUID projectId, UUID userId, byte[] bytes) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        return saveAsset(project, userId, bytes);
    }

    /** Gambar milik website tayang, dibaca pengunjung tanpa login; aset website lain tidak bisa diambil. */
    @Transactional(readOnly = true)
    public StoredAsset siteMedia(UUID projectId, UUID assetId) {
        StoredAsset asset = jdbc.sql("""
                        SELECT organization_id, storage_path, mime_type FROM project_assets
                         WHERE id = :id AND project_id = :project""")
                .param("id", assetId).param("project", projectId)
                .query((rs, row) -> new StoredAsset(rs.getObject("organization_id", UUID.class),
                        assetStorage.file(rs.getString("storage_path")), rs.getString("mime_type")))
                .optional()
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!Files.isRegularFile(asset.file())) {
            throw new ApiException(ErrorCode.NOT_FOUND);
        }
        return asset;
    }

    @Transactional(readOnly = true)
    public List<BackupResponse> backups(UUID projectId, UUID userId) {
        load(projectId, userId, OrgAction.VIEW);
        return jdbc.sql("""
                        SELECT b.id, b.reason, b.created_at, u.display_name
                          FROM project_content_backups b LEFT JOIN users u ON u.id = b.created_by
                         WHERE b.project_id = :id ORDER BY b.created_at DESC LIMIT :limit""")
                .param("id", projectId).param("limit", BACKUPS_SHOWN)
                .query((rs, row) -> new BackupResponse(rs.getObject("id", UUID.class), rs.getString("reason"),
                        rs.getTimestamp("created_at").toInstant(), rs.getString("display_name")))
                .list();
    }

    /** Isi lokal yang tidak bisa disinkron (LOCK_LOST / konflik) disimpan apa adanya, tanpa divalidasi. */
    @Transactional
    public BackupResponse createBackup(UUID projectId, UUID userId, Map<String, Object> content, String reason) {
        load(projectId, userId, OrgAction.EDIT_DRAFT);
        return saveBackup(projectId, userId, content, reason);
    }

    /** Pulihkan cadangan (Admin/Pemilik yang memegang kunci); isi saat ini ikut dicadangkan dulu. */
    @Transactional
    public ContentVersionResponse restoreBackup(UUID projectId, UUID backupId, UUID userId) {
        Project project = load(projectId, userId, OrgAction.PUBLISH);
        locks.requireHeldBy(projectId, userId);
        String json = jdbc.sql("SELECT content::text FROM project_content_backups WHERE id = :id AND project_id = :project")
                .param("id", backupId).param("project", projectId)
                .query(String.class)
                .optional()
                .orElseThrow(() -> new ApiException(ErrorCode.BACKUP_NOT_FOUND));
        Map<String, Object> restored = parse(json);
        ContentValidator.validate(restored, marking(project), asset -> ownsAsset(projectId, asset));
        saveBackup(projectId, userId, project.getContent(), "restore");
        project.setContent(restored);
        project.setContentVersion(project.getContentVersion() + 1);
        project.setContentUpdatedAt(clock.instant());
        project.touch(userId);
        projectRepository.saveAndFlush(project);
        activityService.log(project.getOrganizationId(), userId, "website.backup_restored", "project", projectId,
                Map.of("name", project.getName()));
        return new ContentVersionResponse(project.getContentVersion());
    }

    // ---------- pembantu ----------

    private Project load(UUID projectId, UUID userId, OrgAction action) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND));
        access.require(project.getOrganizationId(), userId, action);
        return project;
    }

    /**
     * Tandaan template asal (isian, gaya, tema yang diizinkan); null untuk website Rakit sendiri. Status template
     * tidak dicek: website tetap bisa diedit walau templatenya kemudian tidak tayang lagi.
     */
    /** Publish langsung tidak boleh menimpa website yang sedang diedit anggota lain (kunci edit K17). */
    public void requireNotLockedByOthers(UUID projectId, UUID userId) {
        locks.requireFreeFor(projectId, userId);
    }

    /** Aturan isi yang sama dengan {@link #updateContent}, untuk jalur lain yang menyimpan isi (publish langsung). */
    public void validateContent(Project project, Map<String, Object> content) {
        ContentValidator.validate(content, marking(project), asset -> ownsAsset(project.getId(), asset));
    }

    private MarkingData marking(Project project) {
        if (project.getSourceTemplateId() == null) {
            return null;
        }
        return templateRepository.findById(project.getSourceTemplateId()).map(Template::getMarking).orElse(null);
    }

    private boolean ownsAsset(UUID projectId, UUID assetId) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM project_assets WHERE id = :id AND project_id = :project)")
                .param("id", assetId).param("project", projectId)
                .query(Boolean.class)
                .single();
    }

    private AssetResponse saveAsset(Project project, UUID userId, byte[] bytes) {
        if (bytes == null || bytes.length == 0 || bytes.length > properties.projects().assetMaxKb() * 1024L) {
            throw new ApiException(ErrorCode.ASSET_INVALID);
        }
        ProjectAssetStorage.Image image = ProjectAssetStorage.inspect(bytes);
        if (image == null) {
            throw new ApiException(ErrorCode.ASSET_INVALID);
        }
        String sha = ProjectAssetStorage.sha256(bytes);
        AssetResponse existing = jdbc.sql("""
                        SELECT id, width, height, size_bytes FROM project_assets WHERE project_id = :project AND sha256 = :sha""")
                .param("project", project.getId()).param("sha", sha)
                .query((rs, row) -> assetResponse(rs.getObject("id", UUID.class), rs.getInt("width"), rs.getInt("height"),
                        rs.getLong("size_bytes")))
                .optional()
                .orElse(null);
        if (existing != null) {
            return existing;
        }
        // Per file unik: foto yang sama di beberapa website satu organisasi tersimpan sekali di disk (nama = sha256).
        long used = jdbc.sql("""
                        SELECT COALESCE(SUM(size_bytes), 0) FROM (
                            SELECT DISTINCT ON (storage_path) size_bytes FROM project_assets WHERE organization_id = :org) unik""")
                .param("org", project.getOrganizationId())
                .query(Long.class)
                .single();
        if (used + bytes.length > properties.projects().storageMaxMbPerOrg() * 1024L * 1024L) {
            throw new ApiException(ErrorCode.STORAGE_LIMIT_REACHED);
        }
        String path = assetStorage.write(project.getOrganizationId(), sha, image.extension(), bytes);
        UUID id = jdbc.sql("""
                        INSERT INTO project_assets (organization_id, project_id, sha256, mime_type, width, height,
                                                    size_bytes, storage_path, created_by)
                        VALUES (:org, :project, :sha, :mime, :width, :height, :size, :path, :user)
                        RETURNING id""")
                .param("org", project.getOrganizationId()).param("project", project.getId()).param("sha", sha)
                .param("mime", image.mimeType()).param("width", image.width()).param("height", image.height())
                .param("size", (long) bytes.length).param("path", path).param("user", userId)
                .query(UUID.class)
                .single();
        return assetResponse(id, image.width(), image.height(), bytes.length);
    }

    /** Logo organisasi disalin menjadi aset website, agar isi website tidak bergantung pada logo yang bisa diganti. */
    private UUID copyLogo(Organization org, UUID projectId, UUID userId) {
        Path file = logoStorage.file(org.getId());
        if (file == null) {
            return null;
        }
        try {
            Project project = projectRepository.findById(projectId).orElseThrow();
            return saveAsset(project, userId, Files.readAllBytes(file)).assetId();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ApiException e) {
            // Penyimpanan penuh atau logo tidak terbaca: website tetap dibuat, logo memakai contoh template.
            return null;
        }
    }

    private BackupResponse saveBackup(UUID projectId, UUID userId, Map<String, Object> content, String reason) {
        Instant now = clock.instant();
        UUID id = jdbc.sql("""
                        INSERT INTO project_content_backups (project_id, content, reason, created_by, created_at)
                        VALUES (:project, :content::jsonb, :reason, :user, :now) RETURNING id""")
                .param("project", projectId).param("content", toJson(content)).param("reason", reason)
                .param("user", userId).param("now", Timestamp.from(now))
                .query(UUID.class)
                .single();
        return new BackupResponse(id, reason, now, null);
    }

    private static AssetResponse assetResponse(UUID id, int width, int height, long size) {
        return new AssetResponse(id, width, height, size, "/api/assets/" + id);
    }

    private String toJson(Map<String, Object> content) {
        try {
            return jsonMapper.writeValueAsString(content);
        } catch (JacksonException e) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Isi cadangan tidak valid.");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parse(String json) {
        try {
            return jsonMapper.readValue(json, Map.class);
        } catch (JacksonException e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
