package com.aris.templateapp.site;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.organization.AttemptLimiter;
import com.aris.templateapp.organization.Organization;
import com.aris.templateapp.organization.OrganizationMemberRepository;
import com.aris.templateapp.organization.OrganizationRepository;
import com.aris.templateapp.project.Project;
import com.aris.templateapp.project.ProjectRepository;
import com.aris.templateapp.project.ProjectService;
import com.aris.templateapp.site.dto.SiteRenderResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
 * Website publik yang sudah tayang: data render untuk OrgSpace-Hosting, domain kustom (resolve-domain, ask Caddy),
 * aset dan foto website, koleksi publik, kiriman formulir pengunjung, dan jalur lama {@code /sites/**}.
 * Portal Pengurus ada di {@link PortalAuthController}, {@link PortalContentController}.
 * Tanpa {@code @CrossOrigin}: izin CORS diatur satu tempat di {@code CorsOriginPolicy}.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class PublicSiteController {

    /** Nama modul yang diisi dari tab Berita (tabel site_news), sama dengan TemplateRenderer di Hosting. */
    private static final java.util.Set<String> NEWS_MODULES = java.util.Set.of("berita", "kegiatan", "pengumuman");
    /** Jenis isian yang boleh ditulis provider lewat data-ws-type; gambar & link dikenali dari elemennya. */
    private static final java.util.Set<String> FIELD_TYPES = java.util.Set.of("teks", "paragraf", "angka", "tanggal");

    private final VisitorIp visitorIp;
    private final SiteRepository siteRepository;
    private final SiteReleaseRepository siteReleaseRepository;
    private final SiteStorage siteStorage;
    private final SitePublishService publishService;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final OrganizationRepository organizationRepository;
    private final SiteNewsRepository siteNewsRepository;
    private final FormSubmissionRepository formSubmissionRepository;
    private final SiteCustomItemRepository siteCustomItemRepository;
    private final OrganizationMemberRepository memberRepository;
    private final SiteDomainRepository siteDomainRepository;
    private final AppProperties properties;
    private final AttemptLimiter attemptLimiter;

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
        Map<String, Map<String, String>> modules = templateModules(allPagesHtml(releaseDir, compiledHtml));
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
                modules,
                siteStats(site, org, dbNews.size(), collectionsMap),
                orgProfile(site, org, owner),
                portalConfig(releaseDir, modules)
        );
    }

    /** portal.json rilis sudah lolos cek upload; tetap dibaca ulang agar Hosting hanya menerima bagian yang valid. */
    private static Map<String, Object> portalConfig(Path releaseDir, Map<String, Map<String, String>> modules) {
        Path file = releaseDir.resolve(PortalConfig.FILE_NAME);
        if (!Files.isRegularFile(file)) {
            return Map.of();
        }
        try {
            return PortalConfig.read(Files.readString(file, StandardCharsets.UTF_8), modules,
                    path -> Files.isRegularFile(releaseDir.resolve(path))).config();
        } catch (IOException e) {
            log.warn("Gagal membaca portal.json {}", releaseDir, e);
            return Map.of();
        }
    }

    /** Semua halaman rilis digabung, agar modul di halaman selain beranda (mis. struktur di tentang.html) ikut terbaca. */
    private static String allPagesHtml(Path releaseDir, String indexHtml) {
        StringBuilder html = new StringBuilder(indexHtml == null ? "" : indexHtml);
        try (var files = Files.walk(releaseDir)) {
            Path index = releaseDir.resolve("index.html");
            for (Path page : files.filter(f -> f.toString().endsWith(".html") && !f.equals(index)).sorted().toList()) {
                html.append(Files.readString(page, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            log.warn("Gagal membaca halaman rilis {}", releaseDir, e);
        }
        return html.toString();
    }

    /**
     * Angka untuk data-ws-stat. Nama bawaan (pengurus, berita, form.{nama}) menang jika modul kustom memakai nama
     * yang sama, agar arti angkanya selalu jelas.
     */
    private Map<String, Long> siteStats(Site site, Organization org, int newsCount,
                                        Map<String, List<Map<String, Object>>> collections) {
        Map<String, Long> stats = new LinkedHashMap<>();
        collections.forEach((name, items) -> stats.put(name, (long) items.size()));
        stats.put("pengurus", memberRepository.countByOrganizationId(org.getId()));
        stats.put("berita", (long) newsCount);
        for (Object[] row : formSubmissionRepository.countByFormName(site.getId())) {
            stats.put("form." + row[0], (Long) row[1]);
        }
        return stats;
    }

    /** Profil umum organisasi untuk data-ws-org (isinya sudah publik di profil organisasi). */
    private static Map<String, String> orgProfile(Site site, Organization org, String owner) {
        Map<String, String> profile = new LinkedHashMap<>();
        profile.put("nama", owner);
        if (!org.isPersonal()) {
            profile.put("singkatan", org.getShortName());
            profile.put("kategori", org.getCategory() != null ? org.getCategory().label() : null);
            profile.put("institusi", org.getInstitutionName());
            profile.put("kota", org.getCity());
            profile.put("alamat", org.getAddress());
            profile.put("email", org.getEmail());
            profile.put("whatsapp", org.getWhatsapp());
            profile.put("instagram", org.getInstagram());
            profile.put("tiktok", org.getTiktok());
            profile.put("youtube", org.getYoutube());
            // Logo disajikan Hosting di alamat website sendiri, karena halaman sandbox tidak memanggil backend.
            profile.put("logo", org.getLogoUrl() != null ? "/" + site.getSlug() + "/_media/logo" : null);
        }
        profile.values().removeIf(v -> v == null || v.isBlank());
        return profile;
    }

    /**
     * Koleksi data-ws-list/collection di template beserta field dan jenisnya; berita diisi lewat tab Berita,
     * jadi tidak ikut. Jenis: img = gambar, data-ws-link = link, selain itu data-ws-type atau teks.
     */
    public static Map<String, Map<String, String>> templateModules(String html) {
        Map<String, Map<String, String>> modules = new LinkedHashMap<>();
        if (html == null) {
            return modules;
        }
        for (org.jsoup.nodes.Element list : org.jsoup.Jsoup.parse(html).select("[data-ws-list], [data-ws-collection]")) {
            String name = (list.hasAttr("data-ws-list") ? list.attr("data-ws-list") : list.attr("data-ws-collection"))
                    .strip().toLowerCase(Locale.ROOT);
            if (name.isEmpty() || NEWS_MODULES.contains(name)) {
                continue;
            }
            Map<String, String> fields = modules.computeIfAbsent(name, k -> new LinkedHashMap<>());
            for (org.jsoup.nodes.Element f : list.select("[data-ws-field], [data-ws-link]")) {
                boolean isField = f.hasAttr("data-ws-field");
                String field = (isField ? f.attr("data-ws-field") : f.attr("data-ws-link")).strip();
                if (field.isEmpty()) {
                    continue;
                }
                String declared = f.attr("data-ws-type").strip().toLowerCase(Locale.ROOT);
                String type = !isField ? "link"
                        : f.normalName().equals("img") ? "gambar"
                        : FIELD_TYPES.contains(declared) ? declared : "teks";
                fields.putIfAbsent(field, type);
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
        Optional<SiteDomain> customDomain = siteDomainRepository.findActiveForHost(cleanDomain);
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
        Optional<SiteDomain> active = siteDomainRepository.findActiveForHost(cleanDomain);
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
        return ResponseEntity.status(HttpStatus.FOUND).location(portalAdminUri(slug)).build();
    }

    /** Portal Pengurus ada di OrgSpace-Hosting ({@code SITES_HOSTING_URL}), bukan di backend. */
    private URI portalAdminUri(String slug) {
        return URI.create(properties.sites().hostingUrl() + "/" + slug.toLowerCase(Locale.ROOT) + "/admin");
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
            return ResponseEntity.status(HttpStatus.FOUND).location(portalAdminUri(slug)).build();
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
        String ip = visitorIp.of(request);
        attemptLimiter.checkFormSubmission(ip, slug);
        String ua = request.getHeader(HttpHeaders.USER_AGENT);
        publishService.submitForm(slug, formName, payload, ip, ua);
        return Map.of("success", true, "message", "Terima kasih, data berhasil dikirim.");
    }

    @GetMapping("/api/public/sites/{slug}/collections/{collectionName}")
    public List<Map<String, Object>> getCollectionItems(@PathVariable String slug,
                                                        @PathVariable String collectionName) {
        Site site = siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));
        return siteCustomItemRepository.findBySiteIdAndCollectionNameOrderBySortOrderAscCreatedAtDesc(site.getId(), collectionName.toLowerCase(Locale.ROOT))
                .stream()
                .map(PortalContentController::row)
                .toList();
    }

    @GetMapping("/api/public/sites/{slug}/media/{assetId}")
    public ResponseEntity<Resource> serveMedia(@PathVariable String slug, @PathVariable UUID assetId) {
        Site site = siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));
        ProjectService.StoredAsset asset = projectService.siteMedia(site.getProjectId(), assetId);
        // Isi aset tidak pernah berubah (foto baru = id baru), jadi aman disimpan lama oleh browser.
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(asset.mimeType()))
                .cacheControl(org.springframework.http.CacheControl.maxAge(java.time.Duration.ofDays(365)).cachePublic())
                .body(new FileSystemResource(asset.file()));
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
