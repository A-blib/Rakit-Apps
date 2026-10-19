package com.aris.templateapp.site;

import com.aris.templateapp.auth.IdentityProvider;
import com.aris.templateapp.auth.UserIdentity;
import com.aris.templateapp.auth.UserIdentityRepository;
import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.organization.MemberRole;
import com.aris.templateapp.organization.Organization;
import com.aris.templateapp.organization.OrganizationAccess;
import com.aris.templateapp.organization.OrganizationMember;
import com.aris.templateapp.organization.OrganizationMemberRepository;
import com.aris.templateapp.organization.OrganizationRepository;
import com.aris.templateapp.project.Project;
import com.aris.templateapp.project.ProjectRepository;
import com.aris.templateapp.security.JwtService;
import com.aris.templateapp.site.dto.SiteRenderResponse;
import com.aris.templateapp.user.User;
import com.aris.templateapp.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller publik untuk menyajikan website yang sudah tayang secara lokal,
 * melayani data render untuk sistem hosting publik (OrgSpace-Hosting), modul autentikasi pengurus,
 * kelola formulir, serta modul koleksi kustom dinamis.
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class PublicSiteController {

    /** Nama modul yang diisi dari tab Berita (tabel site_news), sama dengan TemplateRenderer di Hosting. */
    private static final java.util.Set<String> NEWS_MODULES = java.util.Set.of("berita", "kegiatan", "pengumuman");

    private final SiteRepository siteRepository;
    private final SiteReleaseRepository siteReleaseRepository;
    private final SiteStorage siteStorage;
    private final SitePublishService publishService;
    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final SiteNewsRepository siteNewsRepository;
    private final FormSubmissionRepository formSubmissionRepository;
    private final SiteCustomItemRepository siteCustomItemRepository;
    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;
    private final OrganizationMemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SiteDomainRepository siteDomainRepository;
    private final AppProperties properties;

    /**
     * Endpoint utama untuk Sistem Hosting Publik (Next.js OrgSpace-Hosting).
     * Mengembalikan data lengkap website (struktur content, tema, berita dinamis, form config, dan compiled HTML).
     */
    @GetMapping("/api/public/sites/render/{slug}")
    public SiteRenderResponse getSiteRenderData(@PathVariable String slug) {
        String cleanSlug = slug.toLowerCase(Locale.ROOT).trim();
        Site site = siteRepository.findBySlug(cleanSlug)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website dengan slug '" + cleanSlug + "' tidak ditemukan"));

        if (!"online".equalsIgnoreCase(site.getStatus()) || site.getCurrentReleaseId() == null) {
            throw new ApiException(ErrorCode.SITE_NOT_PUBLISHED, "Website sedang tidak aktif atau belum dipublikasikan");
        }

        SiteRelease release = siteReleaseRepository.findById(site.getCurrentReleaseId())
                .orElseThrow(() -> new ApiException(ErrorCode.RELEASE_NOT_FOUND, "Rilis website tidak ditemukan"));

        Project project = projectRepository.findById(site.getProjectId()).orElse(null);
        Organization org = organizationRepository.findById(site.getOrganizationId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Organisasi tidak ditemukan"));

        // Baca file HTML terkompilasi jika ada
        Path releaseDir = siteStorage.releasePath(site.getId(), release.getVersion());
        Path indexPath = releaseDir.resolve("index.html");
        String compiledHtml = null;
        if (Files.exists(indexPath)) {
            try {
                compiledHtml = Files.readString(indexPath, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.warn("Gagal membaca index.html untuk site {}", cleanSlug, e);
            }
        }

        // Hanya data asli: website terbit tidak boleh menampilkan berita contoh.
        List<SiteNews> dbNews = siteNewsRepository.findByOrganizationIdOrderByPublishedAtDesc(org.getId());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("id", "ID"));
        List<SiteRenderResponse.NewsItem> newsList = dbNews.stream()
                .map(n -> new SiteRenderResponse.NewsItem(
                        n.getId(),
                        n.getTitle(),
                        n.getSummary(),
                        n.getContent(),
                        n.getPublishedAt().atZone(ZoneId.of(properties.timezone())).format(fmt),
                        n.getImageUrl()
                )).toList();

        // Konfigurasi formulir interaktif
        List<SiteRenderResponse.FormConfig> forms = List.of(
                new SiteRenderResponse.FormConfig(
                        "pendaftaran",
                        "Formulir Pendaftaran " + org.getName(),
                        "/api/public/sites/" + site.getSlug() + "/forms/pendaftaran",
                        List.of(
                                new SiteRenderResponse.FormField("nama", "Nama Lengkap", "text", true, "Masukkan nama lengkap Anda"),
                                new SiteRenderResponse.FormField("kelas", "Kelas / Departemen", "text", true, "Contoh: XII MIPA 1 / Divisi Humas"),
                                new SiteRenderResponse.FormField("whatsapp", "WhatsApp / No. HP", "tel", true, "08xxxxxxxxxx"),
                                new SiteRenderResponse.FormField("pesan", "Alasan / Catatan", "textarea", true, "Tuliskan motivasi atau pesan Anda...")
                        )
                )
        );

        String mode = project != null && project.getMode() != null ? project.getMode().value() : "template";
        String projectName = project != null && project.getName() != null ? project.getName() : org.getName();

        // SEO: judul bawaan dari nama website; nama ruang pribadi ("Ruang Pribadi X") tidak layak tampil ke publik.
        String owner = org.isPersonal() ? projectName : org.getName();
        String seoTitle = site.getSeoTitle() != null && !site.getSeoTitle().isBlank()
                ? site.getSeoTitle() : (org.isPersonal() ? projectName : projectName + " — " + org.getName());
        String seoDesc = site.getSeoDescription() != null && !site.getSeoDescription().isBlank()
                ? site.getSeoDescription() : "Website " + owner + ", diterbitkan melalui OrgSpace.";
        SiteRenderResponse.SeoMeta seo = new SiteRenderResponse.SeoMeta(seoTitle, seoDesc, null, null);

        // Data koleksi kustom dinamis
        List<SiteCustomItem> customItems = siteCustomItemRepository.findBySiteIdOrderByCollectionNameAscSortOrderAscCreatedAtDesc(site.getId());
        Map<String, List<Map<String, Object>>> collectionsMap = new LinkedHashMap<>();
        for (SiteCustomItem item : customItems) {
            Map<String, Object> row = new HashMap<>(item.getData());
            row.put("id", item.getId());
            collectionsMap.computeIfAbsent(item.getCollectionName(), k -> new ArrayList<>()).add(row);
        }
        return new SiteRenderResponse(
                site.getId(),
                site.getProjectId(),
                site.getOrganizationId(),
                site.getSlug(),
                projectName,
                org.getName(),
                mode,
                site.getStatus(),
                release.getVersion(),
                site.getPublishedAt(),
                seo,
                release.getProjectSnapshot(),
                newsList,
                forms,
                collectionsMap,
                compiledHtml,
                templateModules(compiledHtml)
        );
    }

    /** Koleksi data-ws-list/collection di template beserta field-nya; berita diisi lewat tab Berita, jadi tidak ikut. */
    static Map<String, List<String>> templateModules(String html) {
        Map<String, List<String>> modules = new LinkedHashMap<>();
        if (html == null) {
            return modules;
        }
        for (org.jsoup.nodes.Element list : org.jsoup.Jsoup.parse(html).select("[data-ws-list], [data-ws-collection]")) {
            String name = (list.hasAttr("data-ws-list") ? list.attr("data-ws-list") : list.attr("data-ws-collection"))
                    .strip().toLowerCase(Locale.ROOT);
            if (name.isEmpty() || NEWS_MODULES.contains(name)) {
                continue;
            }
            List<String> fields = modules.computeIfAbsent(name, k -> new ArrayList<>());
            for (org.jsoup.nodes.Element f : list.select("[data-ws-field], [data-ws-link]")) {
                String field = f.hasAttr("data-ws-field") ? f.attr("data-ws-field") : f.attr("data-ws-link");
                if (!field.isBlank() && !fields.contains(field.strip())) {
                    fields.add(field.strip());
                }
            }
        }
        return modules;
    }

    /**
     * Menyelesaikan custom domain ke slug website terkait untuk router hosting.
     * Hanya domain kustom aktif (mis. webuser.my.id); subdomain {slug}.orgspace.app tidak dipakai.
     */
    @GetMapping("/api/public/sites/resolve-domain")
    public Map<String, Object> resolveDomain(@RequestParam String domain) {
        if (domain == null || domain.isBlank()) {
            throw new ApiException(ErrorCode.NOT_FOUND, "Domain tidak boleh kosong");
        }
        String cleanDomain = domain.toLowerCase(Locale.ROOT).trim();
        cleanDomain = cleanDomain.replaceFirst("^https?://", "");
        cleanDomain = cleanDomain.split("[/?#:]")[0];

        // 1. Cek tabel site_domains apakah terdaftar sebagai domain kustom aktif
        Optional<SiteDomain> customDomain = siteDomainRepository.findByHostnameAndStatus(cleanDomain, "active");
        if (customDomain.isPresent()) {
            Site site = siteRepository.findById(customDomain.get().getSiteId()).orElse(null);
            if (site != null && "online".equalsIgnoreCase(site.getStatus())) {
                return Map.of("slug", site.getSlug(), "domain", cleanDomain, "customDomain", true);
            }
        }

        // Link bawaan berbentuk path (orgspace.app/{slug}); host lain hanya dikenali lewat domain kustom aktif.
        return Map.of("slug", "", "domain", cleanDomain, "customDomain", false);
    }

    /**
     * Endpoint ask untuk Caddy On-Demand TLS (alur-publish-website.md 7.11).
     * Caddy memanggil endpoint ini sebelum meminta sertifikat HTTPS untuk custom domain.
     */
    @GetMapping("/internal/caddy/ask")
    public ResponseEntity<Void> caddyAsk(@RequestParam String domain) {
        if (domain == null || domain.isBlank()) {
            return ResponseEntity.notFound().build();
        }
        String cleanDomain = domain.toLowerCase(Locale.ROOT).trim().split(":")[0];
        Optional<SiteDomain> active = siteDomainRepository.findByHostnameAndStatus(cleanDomain, "active");
        if (active.isPresent()) {
            Site site = siteRepository.findById(active.get().getSiteId()).orElse(null);
            if (site != null && "online".equalsIgnoreCase(site.getStatus())) {
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Menyajikan aset statis rilis (gambar, css, js) dari website yang tayang.
     */
    @GetMapping("/api/public/sites/{slug}/assets/**")
    public ResponseEntity<Resource> serveSiteAsset(@PathVariable String slug, HttpServletRequest request) {
        Site site = siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));

        if (!"online".equalsIgnoreCase(site.getStatus()) || site.getCurrentReleaseId() == null) {
            throw new ApiException(ErrorCode.SITE_NOT_PUBLISHED, "Website sedang tidak aktif");
        }

        SiteRelease release = siteReleaseRepository.findById(site.getCurrentReleaseId())
                .orElseThrow(() -> new ApiException(ErrorCode.RELEASE_NOT_FOUND));

        Path releaseDir = siteStorage.releasePath(site.getId(), release.getVersion());
        String fullPath = request.getRequestURI();
        String prefix = "/api/public/sites/" + slug + "/assets/";
        String subPath = fullPath.startsWith(prefix) ? fullPath.substring(prefix.length()) : "";

        Path target = releaseDir.resolve(subPath).normalize();
        if (!target.startsWith(releaseDir) || !Files.exists(target) || Files.isDirectory(target)) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = determineMediaType(target);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                .body(new FileSystemResource(target));
    }

    @GetMapping(value = {"/sites/{slug}", "/sites/{slug}/"})
    public ResponseEntity<Void> rootRedirect(@PathVariable String slug, HttpServletRequest request) {
        if (!request.getRequestURI().endsWith("/")) {
            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                    .location(URI.create(request.getRequestURI() + "/"))
                    .build();
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/sites/" + slug + "/index.html"))
                .build();
    }

    @GetMapping(value = {"/sites/{slug}/admin", "/sites/{slug}/admin/**"})
    public ResponseEntity<Void> redirectToSiteAdmin(@PathVariable String slug) {
        String target = "http://localhost:3001/" + slug + "/admin";
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(target))
                .build();
    }

    @GetMapping("/sites/{slug}/**")
    public ResponseEntity<?> serveSiteFile(@PathVariable String slug, HttpServletRequest request) {
        Site site = siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));

        if (!"online".equalsIgnoreCase(site.getStatus()) || site.getCurrentReleaseId() == null) {
            throw new ApiException(ErrorCode.SITE_NOT_PUBLISHED, "Website sedang tidak aktif");
        }

        SiteRelease release = siteReleaseRepository.findById(site.getCurrentReleaseId())
                .orElseThrow(() -> new ApiException(ErrorCode.RELEASE_NOT_FOUND));

        Path releaseDir = siteStorage.releasePath(site.getId(), release.getVersion());
        String fullPath = request.getRequestURI();
        String prefix = "/sites/" + slug + "/";
        String subPath = fullPath.startsWith(prefix) ? fullPath.substring(prefix.length()) : "index.html";
        if (subPath.isBlank() || subPath.endsWith("/")) {
            subPath += "index.html";
        }

        // Jika user mengakses halaman admin atau portal pengurus dari link website statis
        if ("admin".equalsIgnoreCase(subPath) || subPath.startsWith("admin/") || "admin.html".equalsIgnoreCase(subPath)) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("http://localhost:3001/" + slug + "/admin"))
                    .build();
        }

        Path target = releaseDir.resolve(subPath).normalize();
        if (!target.startsWith(releaseDir) || !Files.exists(target) || Files.isDirectory(target)) {
            Path notFound = releaseDir.resolve("404.html");
            if (Files.exists(notFound)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.TEXT_HTML)
                        .body(new FileSystemResource(notFound));
            }
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = determineMediaType(target);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(new FileSystemResource(target));
    }

    @PostMapping("/api/public/sites/{slug}/forms/{formName}")
    public Map<String, Object> submitForm(@PathVariable String slug,
                                          @PathVariable String formName,
                                          @RequestBody Map<String, Object> payload,
                                          HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String ua = request.getHeader(HttpHeaders.USER_AGENT);
        publishService.submitForm(slug, formName, payload, ip, ua);
        return Map.of("success", true, "message", "Data pendaftaran berhasil diterima!");
    }

    // ==========================================
    // Modul Autentikasi & Dashboard Pengurus Web
    // ==========================================

    private UUID authenticateSiteManager(String authHeader, Site site) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Token autentikasi pengurus diperlukan");
        }
        String token = authHeader.substring(7).trim();
        UUID userId = jwtService.parseSiteUserId(token, site.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "Sesi login pengurus tidak valid atau telah berakhir"));
        memberRepository.findByOrganizationIdAndUserId(site.getOrganizationId(), userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_ORG_MEMBER, "Anda bukan pengurus atau anggota dari organisasi website ini"));
        return userId;
    }

    private OrganizationMember authenticateSiteAdmin(String authHeader, Site site) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Token autentikasi pengurus diperlukan");
        }
        String token = authHeader.substring(7).trim();
        UUID userId = jwtService.parseSiteUserId(token, site.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "Sesi login pengurus tidak valid atau telah berakhir"));
        OrganizationMember member = memberRepository.findByOrganizationIdAndUserId(site.getOrganizationId(), userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_ORG_MEMBER, "Anda bukan pengurus atau anggota dari organisasi website ini"));
        if (member.getRole() != MemberRole.OWNER && member.getRole() != MemberRole.ADMIN) {
            throw new ApiException(ErrorCode.ROLE_NOT_ALLOWED, "Hanya Pemilik dan Admin yang berhak mengelola hak akses pengurus");
        }
        return member;
    }

    @PostMapping("/api/public/sites/{slug}/auth/login")
    public Map<String, Object> siteLogin(@PathVariable String slug, @RequestBody Map<String, String> payload) {
        String identifier = payload.get("identifier") != null ? payload.get("identifier") : payload.get("email");
        String password = payload.get("password");
        if (identifier == null || password == null || identifier.isBlank() || password.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Email/username dan password wajib diisi");
        }
        Site site = siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));

        String cleanEmail = identifier.trim().toLowerCase(Locale.ROOT);
        UserIdentity identity = userIdentityRepository.findByProviderAndProviderUserId(IdentityProvider.LOCAL, cleanEmail)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS, "Email atau password pengurus salah"));
        if (identity.getPasswordHash() == null || !passwordEncoder.matches(password, identity.getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "Email atau password pengurus salah");
        }
        UUID userId = identity.getUser().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User tidak ditemukan"));
        OrganizationMember member = memberRepository.findByOrganizationIdAndUserId(site.getOrganizationId(), user.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_ORG_MEMBER, "Akun ini tidak terdaftar sebagai pengurus organisasi website ini"));

        String token = jwtService.createSiteToken(user.getId(), site.getId());
        return Map.of(
                "success", true,
                "token", token,
                "user", Map.of(
                        "id", user.getId(),
                        "name", user.getDisplayName() != null ? user.getDisplayName() : user.getEmail(),
                        "email", user.getEmail(),
                        "role", member.getRole().name().toLowerCase(Locale.ROOT)
                ),
                "site", Map.of(
                        "slug", site.getSlug(),
                        "organizationId", site.getOrganizationId()
                )
        );
    }

    @GetMapping("/api/public/sites/{slug}/auth/me")
    public Map<String, Object> siteAuthMe(@PathVariable String slug,
                                          @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));
        UUID userId = authenticateSiteManager(authHeader, site);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User tidak ditemukan"));
        OrganizationMember member = memberRepository.findByOrganizationIdAndUserId(site.getOrganizationId(), userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_ORG_MEMBER));

        return Map.of(
                "authenticated", true,
                "user", Map.of(
                        "id", user.getId(),
                        "name", user.getDisplayName() != null ? user.getDisplayName() : user.getEmail(),
                        "email", user.getEmail(),
                        "role", member.getRole().name().toLowerCase(Locale.ROOT)
                )
        );
    }

    @GetMapping("/api/public/sites/{slug}/admin/stats")
    public Map<String, Object> getAdminStats(@PathVariable String slug,
                                             @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));
        authenticateSiteManager(authHeader, site);

        long submissionsCount = formSubmissionRepository.findBySiteIdOrderByCreatedAtDesc(site.getId()).size();
        long newsCount = siteNewsRepository.findByOrganizationIdOrderByPublishedAtDesc(site.getOrganizationId()).size();
        long customCount = siteCustomItemRepository.findBySiteIdOrderByCollectionNameAscSortOrderAscCreatedAtDesc(site.getId()).size();

        return Map.of(
                "submissionsCount", submissionsCount,
                "newsCount", newsCount,
                "customItemsCount", customCount,
                "status", site.getStatus()
        );
    }

    @GetMapping("/api/public/sites/{slug}/admin/submissions")
    public List<FormSubmission> getSubmissions(@PathVariable String slug,
                                               @RequestParam(required = false) String formName,
                                               @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));
        authenticateSiteManager(authHeader, site);

        if (formName != null && !formName.isBlank()) {
            return formSubmissionRepository.findBySiteIdAndFormNameOrderByCreatedAtDesc(site.getId(), formName.trim());
        }
        return formSubmissionRepository.findBySiteIdOrderByCreatedAtDesc(site.getId());
    }

    @GetMapping("/api/public/sites/{slug}/admin/news")
    public List<SiteNews> getNews(@PathVariable String slug,
                                  @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));
        authenticateSiteManager(authHeader, site);
        return siteNewsRepository.findByOrganizationIdOrderByPublishedAtDesc(site.getOrganizationId());
    }

    @GetMapping("/api/public/sites/{slug}/admin/collections")
    public Map<String, List<Map<String, Object>>> getAdminCollections(@PathVariable String slug,
                                                                      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));
        authenticateSiteManager(authHeader, site);
        List<SiteCustomItem> customItems = siteCustomItemRepository.findBySiteIdOrderByCollectionNameAscSortOrderAscCreatedAtDesc(site.getId());
        Map<String, List<Map<String, Object>>> collectionsMap = new LinkedHashMap<>();
        for (SiteCustomItem item : customItems) {
            Map<String, Object> row = new HashMap<>(item.getData());
            row.put("id", item.getId());
            collectionsMap.computeIfAbsent(item.getCollectionName(), k -> new ArrayList<>()).add(row);
        }
        return collectionsMap;
    }

    @GetMapping("/api/public/sites/{slug}/admin/collections/{collectionName}")
    public List<Map<String, Object>> getAdminCollectionItems(@PathVariable String slug,
                                                             @PathVariable String collectionName,
                                                             @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));
        authenticateSiteManager(authHeader, site);
        String cleanCol = collectionName.toLowerCase(Locale.ROOT).trim();
        List<SiteCustomItem> items = siteCustomItemRepository.findBySiteIdAndCollectionNameOrderBySortOrderAscCreatedAtDesc(site.getId(), cleanCol);
        return items.stream().map(item -> {
            Map<String, Object> row = new HashMap<>(item.getData());
            row.put("id", item.getId());
            return row;
        }).toList();
    }

    @PostMapping("/api/public/sites/{slug}/admin/news")
    public SiteNews createNews(@PathVariable String slug,
                               @RequestBody Map<String, String> payload,
                               @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));
        UUID userId = authenticateSiteManager(authHeader, site);

        String title = payload.get("title");
        String summary = payload.get("summary");
        String content = payload.get("content");
        String imageUrl = payload.get("imageUrl");

        if (title == null || title.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Judul berita tidak boleh kosong");
        }

        SiteNews news = new SiteNews(site.getOrganizationId(), title.trim(), summary, content, imageUrl, userId);
        return siteNewsRepository.save(news);
    }

    @DeleteMapping("/api/public/sites/{slug}/admin/news/{newsId}")
    public Map<String, Object> deleteNews(@PathVariable String slug,
                                          @PathVariable UUID newsId,
                                          @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));
        authenticateSiteManager(authHeader, site);

        SiteNews news = siteNewsRepository.findById(newsId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Berita tidak ditemukan"));
        if (!news.getOrganizationId().equals(site.getOrganizationId())) {
            throw new ApiException(ErrorCode.ROLE_NOT_ALLOWED, "Berita bukan milik organisasi ini");
        }
        siteNewsRepository.delete(news);
        return Map.of("success", true, "message", "Berita berhasil dihapus");
    }

    @GetMapping("/api/public/sites/{slug}/collections/{collectionName}")
    public List<Map<String, Object>> getCollectionItems(@PathVariable String slug,
                                                        @PathVariable String collectionName) {
        Site site = siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));
        return siteCustomItemRepository.findBySiteIdAndCollectionNameOrderBySortOrderAscCreatedAtDesc(site.getId(), collectionName.toLowerCase(Locale.ROOT))
                .stream()
                .map(item -> {
                    Map<String, Object> map = new HashMap<>(item.getData());
                    map.put("id", item.getId());
                    return map;
                })
                .toList();
    }

    @PostMapping("/api/public/sites/{slug}/admin/collections/{collectionName}")
    public Map<String, Object> addCollectionItem(@PathVariable String slug,
                                                 @PathVariable String collectionName,
                                                 @RequestBody Map<String, Object> itemData,
                                                 @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));
        authenticateSiteManager(authHeader, site);

        if (itemData == null || itemData.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Data koleksi tidak boleh kosong");
        }

        String cleanCol = collectionName.toLowerCase(Locale.ROOT).trim();
        SiteCustomItem item = new SiteCustomItem(site.getId(), cleanCol, itemData, 0);
        SiteCustomItem saved = siteCustomItemRepository.save(item);

        Map<String, Object> res = new HashMap<>(saved.getData());
        res.put("id", saved.getId());
        return res;
    }

    @DeleteMapping("/api/public/sites/{slug}/admin/collections/{collectionName}/{itemId}")
    public Map<String, Object> deleteCollectionItem(@PathVariable String slug,
                                                    @PathVariable String collectionName,
                                                    @PathVariable UUID itemId,
                                                    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));
        authenticateSiteManager(authHeader, site);

        SiteCustomItem item = siteCustomItemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Item tidak ditemukan"));
        if (!item.getSiteId().equals(site.getId())) {
            throw new ApiException(ErrorCode.ROLE_NOT_ALLOWED);
        }
        siteCustomItemRepository.delete(item);
        return Map.of("success", true, "message", "Item berhasil dihapus");
    }

    // ==========================================
    // Pengelolaan Pengurus & Hak Akses Organisasi
    // ==========================================

    @GetMapping("/api/public/sites/{slug}/admin/members")
    public List<Map<String, Object>> getSiteMembers(@PathVariable String slug,
                                                    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));
        authenticateSiteManager(authHeader, site);

        return memberRepository.findByOrganizationId(site.getOrganizationId())
                .stream()
                .map(m -> {
                    User u = userRepository.findById(m.getUserId()).orElse(null);
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", m.getUserId());
                    map.put("role", m.getRole().value());
                    map.put("positionTitle", m.getPositionTitle());
                    map.put("joinedAt", m.getJoinedAt());
                    map.put("name", u != null && u.getDisplayName() != null ? u.getDisplayName() : (u != null ? u.getEmail() : "Pengurus"));
                    map.put("email", u != null ? u.getEmail() : "-");
                    return map;
                })
                .toList();
    }

    @PostMapping("/api/public/sites/{slug}/admin/members")
    public Map<String, Object> addSiteMember(@PathVariable String slug,
                                             @RequestBody Map<String, String> payload,
                                             @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));
        OrganizationMember currentAdmin = authenticateSiteAdmin(authHeader, site);

        String email = payload.get("email");
        String roleStr = payload.get("role");
        String positionTitle = payload.get("positionTitle");
        if (email == null || email.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Email pengurus wajib diisi");
        }

        MemberRole targetRole = "admin".equalsIgnoreCase(roleStr) ? MemberRole.ADMIN : MemberRole.EDITOR;
        if (!OrganizationAccess.canInvite(currentAdmin.getRole(), targetRole)) {
            throw new ApiException(ErrorCode.ROLE_NOT_ALLOWED, "Anda tidak memiliki wewenang untuk memberikan peran ini");
        }

        String cleanEmail = email.trim().toLowerCase(Locale.ROOT);
        // Hanya akun OrgSpace yang sudah ada. Dulu akun baru dibuat dengan password bawaan yang diketahui
        // orang lain, sehingga pemilik email asli tidak bisa mendaftar dan akunnya bisa dimasuki pihak lain.
        User user = userRepository.findByEmailIgnoreCase(cleanEmail)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND,
                        "Email ini belum punya akun OrgSpace. Minta dia daftar di aplikasi OrgSpace dulu, atau undang lewat aplikasi."));

        if (memberRepository.findByOrganizationIdAndUserId(site.getOrganizationId(), user.getId()).isPresent()) {
            throw new ApiException(ErrorCode.ALREADY_MEMBER, "Pengguna sudah menjadi pengurus organisasi ini");
        }

        OrganizationMember newMember = new OrganizationMember(site.getOrganizationId(), user.getId(), targetRole, positionTitle, Instant.now());
        memberRepository.save(newMember);

        return Map.of(
                "success", true,
                "message", "Pengurus berhasil ditambahkan",
                "member", Map.of(
                        "userId", user.getId(),
                        "name", user.getDisplayName() != null ? user.getDisplayName() : user.getEmail(),
                        "email", user.getEmail(),
                        "role", targetRole.value()
                )
        );
    }

    @PutMapping("/api/public/sites/{slug}/admin/members/{targetUserId}")
    public Map<String, Object> updateMemberRole(@PathVariable String slug,
                                                @PathVariable UUID targetUserId,
                                                @RequestBody Map<String, String> payload,
                                                @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));
        OrganizationMember currentAdmin = authenticateSiteAdmin(authHeader, site);

        OrganizationMember target = memberRepository.findByOrganizationIdAndUserId(site.getOrganizationId(), targetUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Pengurus tidak ditemukan"));

        String roleStr = payload.get("role");
        MemberRole newRole = "admin".equalsIgnoreCase(roleStr) ? MemberRole.ADMIN : MemberRole.EDITOR;

        if (!OrganizationAccess.canChangeRole(currentAdmin.getRole(), target.getRole(), newRole)) {
            throw new ApiException(ErrorCode.ROLE_NOT_ALLOWED, "Anda tidak berhak mengubah peran pengurus ini");
        }

        target.setRole(newRole);
        if (payload.get("positionTitle") != null) {
            target.setPositionTitle(payload.get("positionTitle"));
        }
        memberRepository.save(target);

        return Map.of("success", true, "message", "Peran pengurus berhasil diperbarui");
    }

    @DeleteMapping("/api/public/sites/{slug}/admin/members/{targetUserId}")
    public Map<String, Object> removeMember(@PathVariable String slug,
                                            @PathVariable UUID targetUserId,
                                            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));
        OrganizationMember currentAdmin = authenticateSiteAdmin(authHeader, site);

        OrganizationMember target = memberRepository.findByOrganizationIdAndUserId(site.getOrganizationId(), targetUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Pengurus tidak ditemukan"));

        if (!OrganizationAccess.canRemove(currentAdmin.getRole(), target.getRole())) {
            throw new ApiException(ErrorCode.ROLE_NOT_ALLOWED, "Anda tidak berhak menghapus pengurus ini");
        }

        memberRepository.delete(target);
        return Map.of("success", true, "message", "Akses pengurus berhasil dicabut");
    }

    private MediaType determineMediaType(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".html")) return MediaType.TEXT_HTML;
        if (name.endsWith(".css")) return MediaType.parseMediaType("text/css");
        if (name.endsWith(".js")) return MediaType.parseMediaType("application/javascript");
        if (name.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (name.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        if (name.endsWith(".svg")) return MediaType.parseMediaType("image/svg+xml");
        if (name.endsWith(".json")) return MediaType.APPLICATION_JSON;
        try {
            String probe = Files.probeContentType(path);
            if (probe != null) return MediaType.parseMediaType(probe);
        } catch (IOException ignored) {
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
