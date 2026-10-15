package com.aris.templateapp.site;

import com.aris.templateapp.auth.IdentityProvider;
import com.aris.templateapp.auth.UserIdentity;
import com.aris.templateapp.auth.UserIdentityRepository;
import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

        // Ambil data berita organisasi untuk modul dinamis
        List<SiteNews> dbNews = siteNewsRepository.findByOrganizationIdOrderByPublishedAtDesc(org.getId());
        if (dbNews.isEmpty()) {
            dbNews = List.of(
                    new SiteNews(org.getId(), "Pelantikan Pengurus " + org.getName(),
                            "Selamat bertugas bagi jajaran pengurus yang baru dilantik untuk masa bakti ini.",
                            "Dokumentasi dan sambutan lengkap pengurus...", null, null),
                    new SiteNews(org.getId(), "Rencana Kerja & Agenda Terdekat",
                            "Simak agenda kegiatan dan program unggulan yang akan segera diselenggarakan.",
                            "Program kerja terlaksana dengan sukses...", null, null)
            );
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("id", "ID"));
        List<SiteRenderResponse.NewsItem> newsList = dbNews.stream()
                .map(n -> new SiteRenderResponse.NewsItem(
                        n.getId(),
                        n.getTitle(),
                        n.getSummary(),
                        n.getContent(),
                        LocalDate.now().format(fmt)
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

        // SEO Metadata
        String seoTitle = site.getSeoTitle() != null && !site.getSeoTitle().isBlank()
                ? site.getSeoTitle() : (org.getName() + " — Website Resmi");
        String seoDesc = site.getSeoDescription() != null && !site.getSeoDescription().isBlank()
                ? site.getSeoDescription() : "Website resmi " + org.getName() + " diterbitkan melalui platform OrgSpace.";
        SiteRenderResponse.SeoMeta seo = new SiteRenderResponse.SeoMeta(seoTitle, seoDesc, null, null);

        String mode = project != null && project.getMode() != null ? project.getMode().value() : "template";
        String projectName = project != null && project.getName() != null ? project.getName() : org.getName();

        // Data koleksi kustom dinamis
        List<SiteCustomItem> customItems = siteCustomItemRepository.findBySiteIdOrderByCollectionNameAscSortOrderAscCreatedAtDesc(site.getId());
        Map<String, List<Map<String, Object>>> collectionsMap = new LinkedHashMap<>();
        for (SiteCustomItem item : customItems) {
            Map<String, Object> row = new HashMap<>(item.getData());
            row.put("id", item.getId());
            collectionsMap.computeIfAbsent(item.getCollectionName(), k -> new ArrayList<>()).add(row);
        }
        // Contoh data bawaan jika koleksi prestasi belum diisi
        if (!collectionsMap.containsKey("prestasi")) {
            collectionsMap.put("prestasi", List.of(
                    Map.of("nama_juara", "Budi Santoso", "lomba", "Juara 1 Olimpiade Sains Nasional", "tahun", "2026", "tingkat", "Nasional"),
                    Map.of("nama_juara", "Siti Rahma", "lomba", "Juara 2 Lomba Debat Bahasa Inggris", "tahun", "2026", "tingkat", "Provinsi"),
                    Map.of("nama_juara", "Tim Futsal OSIS", "lomba", "Juara 1 Turnamen Pelajar Se-Kota", "tahun", "2025", "tingkat", "Kota")
            ));
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
                compiledHtml
        );
    }

    /**
     * Menyelesaikan custom domain ke slug website terkait untuk router hosting.
     */
    @GetMapping("/api/public/sites/resolve-domain")
    public Map<String, Object> resolveDomain(@RequestParam String domain) {
        if (domain == null || domain.isBlank()) {
            throw new ApiException(ErrorCode.NOT_FOUND, "Domain tidak boleh kosong");
        }
        String cleanDomain = domain.toLowerCase(Locale.ROOT).trim();
        if (cleanDomain.contains(".")) {
            String prefix = cleanDomain.substring(0, cleanDomain.indexOf('.'));
            if (siteRepository.existsBySlug(prefix)) {
                return Map.of("slug", prefix, "domain", cleanDomain);
            }
        }
        if (siteRepository.existsBySlug(cleanDomain)) {
            return Map.of("slug", cleanDomain, "domain", cleanDomain);
        }
        return Map.of("slug", "", "domain", cleanDomain);
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
        UUID userId = jwtService.parseUserId(token)
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
        UUID userId = jwtService.parseUserId(token)
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

        String token = jwtService.createAccessToken(user.getId());
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
        User user = userRepository.findByEmailIgnoreCase(cleanEmail)
                .orElseGet(() -> {
                    String name = payload.get("name") != null && !payload.get("name").isBlank()
                            ? payload.get("name").trim()
                            : cleanEmail.split("@")[0];
                    User newUser = new User(name, cleanEmail);
                    newUser.setOnboardingCompleted(true);
                    User savedUser = userRepository.save(newUser);
                    String tempPassword = payload.get("password") != null && !payload.get("password").isBlank()
                            ? payload.get("password")
                            : "password123";
                    userIdentityRepository.save(UserIdentity.local(savedUser, cleanEmail, passwordEncoder.encode(tempPassword)));
                    return savedUser;
                });

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
