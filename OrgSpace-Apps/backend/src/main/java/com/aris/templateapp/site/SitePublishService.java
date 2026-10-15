package com.aris.templateapp.site;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.organization.MemberRole;
import com.aris.templateapp.organization.OrgAction;
import com.aris.templateapp.organization.Organization;
import com.aris.templateapp.organization.OrganizationAccess;
import com.aris.templateapp.organization.OrganizationMember;
import com.aris.templateapp.organization.OrganizationMemberRepository;
import com.aris.templateapp.organization.OrganizationRepository;
import com.aris.templateapp.organization.OrganizationType;
import com.aris.templateapp.project.Project;
import com.aris.templateapp.project.ProjectMode;
import com.aris.templateapp.project.ProjectRepository;
import com.aris.templateapp.site.dto.BuildResponse;
import com.aris.templateapp.site.dto.DirectPublishRequest;
import com.aris.templateapp.site.dto.DirectPublishResponse;
import com.aris.templateapp.site.dto.FormSubmissionResponse;
import com.aris.templateapp.site.dto.SiteResponse;
import com.aris.templateapp.user.User;
import com.aris.templateapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Layanan untuk mengelola siklus hidup penerbitan website (alur-publish-website.md).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SitePublishService {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");
    private static final Set<String> RESERVED_SLUGS = Set.of(
            "www", "api", "admin", "dashboard", "app", "mail", "smtp", "ftp", "cdn", "static", "assets",
            "custom", "demo", "tes", "test", "support", "help", "status", "blog", "docs", "login", "auth",
            "account", "orgspace", "official", "resmi", "pemerintah", "kemdikbud"
    );

    private final ProjectRepository projectRepository;
    private final SiteRepository siteRepository;
    private final SiteReleaseRepository siteReleaseRepository;
    private final SiteBuildRepository siteBuildRepository;
    private final FormSubmissionRepository formSubmissionRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final OrganizationAccess access;
    private final SiteBuildService siteBuildService;
    private final SiteStorage siteStorage;
    private final AppProperties properties;
    private final Clock clock;

    @Transactional
    public DirectPublishResponse publishDirect(UUID userId, DirectPublishRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));

        // 1. Cari atau buat ruang pribadi (PERSONAL) untuk user ini
        Organization personalOrg = organizationRepository.findAllForUser(userId).stream()
                .map(OrganizationRepository.OrganizationWithRole::getOrganization)
                .filter(Organization::isPersonal)
                .findFirst()
                .orElseGet(() -> {
                    Organization org = new Organization(OrganizationType.PERSONAL, "Ruang Pribadi " + user.getDisplayName(), userId);
                    org = organizationRepository.save(org);
                    memberRepository.save(new OrganizationMember(org.getId(), userId, MemberRole.OWNER, null, clock.instant()));
                    return org;
                });

        // 2. Cari atau buat Project di ruang pribadi
        String projectName = request.name() != null && !request.name().isBlank() ? request.name().trim() : "Website Saya";
        Project project = projectRepository.findByOrganizationIdAndName(personalOrg.getId(), projectName)
                .orElseGet(() -> {
                    Project p = new Project(personalOrg.getId(), projectName, ProjectMode.TEMPLATE, userId);
                    p.setSourceTemplateId(request.sourceTemplateId());
                    p.setTemplateVersion(request.templateVersion());
                    return p;
                });

        if (request.sourceTemplateId() != null) {
            project.setSourceTemplateId(request.sourceTemplateId());
        }
        if (request.templateVersion() != null) {
            project.setTemplateVersion(request.templateVersion());
        }
        project.setContent(request.content() != null ? request.content() : Map.of());
        project.setMissingCount(request.missingCount() != null ? request.missingCount() : 0);
        project.setContentUpdatedAt(clock.instant());
        project.touch(userId);
        project = projectRepository.saveAndFlush(project);

        // 3. Cari atau buat Site
        Site site = siteRepository.findByProjectId(project.getId()).orElse(null);
        if (site == null) {
            String slug = resolveSlug(request.slug(), personalOrg, project);
            site = siteRepository.save(new Site(project.getId(), personalOrg.getId(), slug));
        }

        // 4. Buat SiteBuild dan jalankan build
        SiteBuild build = new SiteBuild(project.getId(), site.getId(), "publish", userId);
        build = siteBuildRepository.saveAndFlush(build);
        siteBuildService.build(build.getId());

        // 5. Muat ulang site & bentuk respon
        site = siteRepository.findById(site.getId()).orElse(site);
        String url = properties.sites().urlOf(site.getSlug());
        int port = 8080;
        try {
            java.net.URI uri = java.net.URI.create(url);
            if (uri.getPort() > 0) {
                port = uri.getPort();
            }
        } catch (Exception ignored) {
        }

        Integer version = null;
        if (site.getCurrentReleaseId() != null) {
            version = siteReleaseRepository.findById(site.getCurrentReleaseId())
                    .map(SiteRelease::getVersion).orElse(null);
        }

        return new DirectPublishResponse(
                project.getId(),
                personalOrg.getId(),
                personalOrg.getName(),
                site.getId(),
                site.getSlug(),
                url,
                port,
                site.getStatus(),
                version
        );
    }

    @Transactional
    public BuildResponse publish(UUID projectId, UUID userId, String requestedSlug) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND));

        OrganizationAccess.Membership membership = access.require(project.getOrganizationId(), userId, OrgAction.VIEW);
        if (membership.role() != MemberRole.OWNER && membership.role() != MemberRole.ADMIN) {
            throw new ApiException(ErrorCode.ROLE_NOT_ALLOWED, "Hanya Admin dan Pemilik yang dapat menerbitkan website.");
        }

        Site site = siteRepository.findByProjectId(projectId).orElse(null);
        if (site == null) {
            // Cek batas website organisasi
            long siteCount = siteRepository.countByOrganizationId(project.getOrganizationId());
            int maxSites = membership.organization().isPersonal() ? properties.sites().maxPerPersonal() : properties.sites().maxPerOrg();
            if (siteCount >= maxSites) {
                throw new ApiException(ErrorCode.SITE_LIMIT_REACHED, "Batas maksimal " + maxSites + " website per ruang kerja telah tercapai.");
            }

            String slug = resolveSlug(requestedSlug, membership.organization(), project);
            site = siteRepository.save(new Site(projectId, project.getOrganizationId(), slug));
        }

        SiteBuild build = new SiteBuild(projectId, site.getId(), "publish", userId);
        build = siteBuildRepository.saveAndFlush(build);

        // Jalankan proses build secara sinkron agar hasil terbit langsung selesai
        siteBuildService.build(build.getId());

        // Muat ulang build terbaru
        build = siteBuildRepository.findById(build.getId()).orElse(build);
        return toBuildResponse(build);
    }

    @Transactional(readOnly = true)
    public BuildResponse getBuild(UUID buildId) {
        SiteBuild build = siteBuildRepository.findById(buildId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Build tidak ditemukan"));
        return toBuildResponse(build);
    }

    @Transactional(readOnly = true)
    public SiteResponse getSite(UUID siteId, UUID userId) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ApiException(ErrorCode.SITE_NOT_PUBLISHED));
        access.require(site.getOrganizationId(), userId, OrgAction.VIEW);
        return toSiteResponse(site);
    }

    @Transactional(readOnly = true)
    public SiteResponse getSiteByProject(UUID projectId, UUID userId) {
        Site site = siteRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ApiException(ErrorCode.SITE_NOT_PUBLISHED));
        access.require(site.getOrganizationId(), userId, OrgAction.VIEW);
        return toSiteResponse(site);
    }

    @Transactional
    public SiteResponse unpublish(UUID siteId, UUID userId) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ApiException(ErrorCode.SITE_NOT_PUBLISHED));

        OrganizationAccess.Membership membership = access.require(site.getOrganizationId(), userId, OrgAction.VIEW);
        if (membership.role() != MemberRole.OWNER && membership.role() != MemberRole.ADMIN) {
            throw new ApiException(ErrorCode.ROLE_NOT_ALLOWED, "Hanya Admin dan Pemilik yang dapat menonaktifkan website.");
        }

        if ("suspended".equalsIgnoreCase(site.getStatus())) {
            throw new ApiException(ErrorCode.SITE_SUSPENDED, "Website ini sedang ditangguhkan oleh sistem.");
        }

        site.setStatus("offline");
        site.setUpdatedAt(clock.instant());
        site = siteRepository.save(site);

        // Hapus symlink di direktori hosts jika ada
        Path hostsDir = Path.of(properties.sites().storageRootDir()).resolve("hosts");
        Path symlink = hostsDir.resolve(site.getSlug() + "." + properties.sites().domain());
        try {
            Files.deleteIfExists(symlink);
        } catch (IOException ignored) {
        }

        log.info("Website {} telah dinonaktifkan (unpublish) oleh user {}", site.getSlug(), userId);
        return toSiteResponse(site);
    }

    @Transactional
    public SiteResponse republish(UUID siteId, UUID userId) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new ApiException(ErrorCode.SITE_NOT_PUBLISHED));

        OrganizationAccess.Membership membership = access.require(site.getOrganizationId(), userId, OrgAction.VIEW);
        if (membership.role() != MemberRole.OWNER && membership.role() != MemberRole.ADMIN) {
            throw new ApiException(ErrorCode.ROLE_NOT_ALLOWED, "Hanya Admin dan Pemilik yang dapat mengaktifkan kembali website.");
        }

        if ("suspended".equalsIgnoreCase(site.getStatus())) {
            throw new ApiException(ErrorCode.SITE_SUSPENDED, "Website ini sedang ditangguhkan oleh sistem.");
        }

        if (site.getCurrentReleaseId() == null) {
            throw new ApiException(ErrorCode.SITE_NOT_PUBLISHED, "Website belum memiliki rilis untuk diaktifkan.");
        }

        SiteRelease release = siteReleaseRepository.findById(site.getCurrentReleaseId())
                .orElseThrow(() -> new ApiException(ErrorCode.RELEASE_NOT_FOUND, "Rilis aktif tidak ditemukan"));

        site.setStatus("online");
        site.setPublishedAt(clock.instant());
        site.setUpdatedAt(clock.instant());
        site = siteRepository.save(site);

        // Pasang kembali symlink ke rilis aktif
        Path hostsDir = Path.of(properties.sites().storageRootDir()).resolve("hosts");
        try {
            Files.createDirectories(hostsDir);
            Path symlink = hostsDir.resolve(site.getSlug() + "." + properties.sites().domain());
            Path target = siteStorage.releasePath(site.getId(), release.getVersion());
            Files.deleteIfExists(symlink);
            try {
                Files.createSymbolicLink(symlink, target);
            } catch (UnsupportedOperationException | IOException ignored) {
            }
        } catch (IOException ignored) {
        }

        log.info("Website {} telah diaktifkan kembali (republish) oleh user {}", site.getSlug(), userId);
        return toSiteResponse(site);
    }

    @Transactional
    public void submitForm(String slug, String formName, Map<String, Object> data, String ip, String ua) {
        Site site = siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));
        if (!"online".equalsIgnoreCase(site.getStatus())) {
            throw new ApiException(ErrorCode.SITE_NOT_PUBLISHED, "Website sedang tidak aktif.");
        }
        formSubmissionRepository.save(new FormSubmission(site.getId(), formName, data, ip, ua));
    }

    @Transactional(readOnly = true)
    public List<FormSubmissionResponse> getSubmissions(UUID projectId, UUID userId, String formName) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND));
        access.require(project.getOrganizationId(), userId, OrgAction.VIEW);

        Site site = siteRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ApiException(ErrorCode.SITE_NOT_PUBLISHED));

        List<FormSubmission> submissions = formName != null && !formName.isBlank()
                ? formSubmissionRepository.findBySiteIdAndFormNameOrderByCreatedAtDesc(site.getId(), formName)
                : formSubmissionRepository.findBySiteIdOrderByCreatedAtDesc(site.getId());

        return submissions.stream()
                .map(s -> new FormSubmissionResponse(s.getId(), s.getSiteId(), s.getFormName(), s.getData(), s.getCreatedAt()))
                .toList();
    }

    private String resolveSlug(String requested, Organization org, Project project) {
        if (requested != null && !requested.isBlank()) {
            String slug = requested.trim().toLowerCase(Locale.ROOT);
            validateSlug(slug);
            if (siteRepository.existsBySlug(slug)) {
                throw new ApiException(ErrorCode.SLUG_TAKEN, "Nama subdomain sudah digunakan. Silakan pilih nama lain.");
            }
            return slug;
        }

        // Generate otomatis dari nama project jika ada, atau nama singkat / nama organisasi
        String baseName = project != null && project.getName() != null && !project.getName().isBlank()
                ? project.getName()
                : (org.getShortName() != null && !org.getShortName().isBlank() ? org.getShortName() : org.getName());
        String candidate = toSlug(baseName);
        if (candidate.length() < 3) {
            candidate = "site-" + candidate;
        }
        if (candidate.length() > 35) {
            candidate = candidate.substring(0, 35);
        }

        String finalSlug = candidate;
        int counter = 1;
        while (siteRepository.existsBySlug(finalSlug) || RESERVED_SLUGS.contains(finalSlug)) {
            finalSlug = candidate + "-" + counter++;
        }
        return finalSlug;
    }

    private void validateSlug(String slug) {
        if (slug.length() < 3 || slug.length() > 40) {
            throw new ApiException(ErrorCode.SLUG_INVALID, "Panjang subdomain harus 3–40 karakter.");
        }
        if (!SLUG_PATTERN.matcher(slug).matches()) {
            throw new ApiException(ErrorCode.SLUG_INVALID, "Subdomain hanya boleh berupa huruf kecil, angka, dan strip.");
        }
        if (RESERVED_SLUGS.contains(slug)) {
            throw new ApiException(ErrorCode.SLUG_RESERVED, "Nama subdomain ini dicadangkan oleh sistem.");
        }
    }

    private String toSlug(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private BuildResponse toBuildResponse(SiteBuild build) {
        Integer version = null;
        if (build.getReleaseId() != null) {
            version = siteReleaseRepository.findById(build.getReleaseId()).map(SiteRelease::getVersion).orElse(null);
        }
        List<String> problems = build.getErrorDetail() != null && build.getErrorDetail().containsKey("message")
                ? List.of(String.valueOf(build.getErrorDetail().get("message"))) : List.of();
        return new BuildResponse(build.getId(), build.getSiteId(), build.getKind(), build.getStatus(), 0,
                build.getErrorCode(), problems, version);
    }

    private SiteResponse toSiteResponse(Site site) {
        Integer version = null;
        String publishedByName = null;
        if (site.getCurrentReleaseId() != null) {
            Optional<SiteRelease> rel = siteReleaseRepository.findById(site.getCurrentReleaseId());
            if (rel.isPresent()) {
                version = rel.get().getVersion();
                if (rel.get().getPublishedBy() != null) {
                    publishedByName = userRepository.findById(rel.get().getPublishedBy())
                            .map(User::getDisplayName).orElse(null);
                }
            }
        }
        String url = properties.sites().urlOf(site.getSlug());
        return new SiteResponse(site.getId(), site.getProjectId(), site.getSlug(), url, site.getStatus(),
                version, site.getPublishedAt(), publishedByName, false, site.getSeoTitle(), site.getSeoDescription());
    }
}
