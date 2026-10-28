package com.aris.templateapp.site;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.organization.OrgAction;
import com.aris.templateapp.project.ProjectService;
import com.aris.templateapp.site.dto.FormSubmissionResponse;
import com.aris.templateapp.site.dto.NewsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Konten yang dikelola pengurus di Portal: ringkasan, data formulir, berita, koleksi modul, dan foto modul. */
@RestController
@RequiredArgsConstructor
public class PortalContentController {

    private static final int MAX_FIELDS = 30;
    private static final int MAX_VALUE_LENGTH = 5000;
    private static final int NEWS_TITLE_MAX = 200;
    private static final int NEWS_TEXT_MAX = 500;

    private final PortalAccess portal;
    private final SitePublishService publishService;
    private final ProjectService projectService;
    private final SiteNewsRepository siteNewsRepository;
    private final FormSubmissionRepository formSubmissionRepository;
    private final SiteCustomItemRepository siteCustomItemRepository;

    @GetMapping("/api/public/sites/{slug}/admin/stats")
    public Map<String, Object> getAdminStats(@PathVariable String slug,
                                             @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = portal.site(slug);
        portal.requireSession(authHeader, site);

        // Dihitung di database (SELECT count(*)): dulu semua baris lengkap dimuat ke memori hanya untuk .size().
        long submissionsCount = formSubmissionRepository.countBySiteId(site.getId());
        long newsCount = siteNewsRepository.countByOrganizationId(site.getOrganizationId());
        long customCount = siteCustomItemRepository.countBySiteId(site.getId());

        return Map.of(
                "submissionsCount", submissionsCount,
                "newsCount", newsCount,
                "customItemsCount", customCount,
                "status", site.getStatus()
        );
    }

    @GetMapping("/api/public/sites/{slug}/admin/submissions")
    public List<FormSubmissionResponse> getSubmissions(@PathVariable String slug,
                                                       @RequestParam(required = false) String formName,
                                                       @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = portal.site(slug);
        // Aturan peran dan bentuk respons (tanpa IP/User-Agent pengunjung) sama dengan jalur app.
        portal.requireSession(authHeader, site).require(OrgAction.VIEW_SUBMISSIONS);
        return publishService.submissionsOf(site.getId(), formName == null ? null : formName.trim());
    }

    // ---------- berita ----------

    @GetMapping("/api/public/sites/{slug}/admin/news")
    public List<NewsResponse> getNews(@PathVariable String slug,
                                      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = portal.site(slug);
        portal.requireSession(authHeader, site);
        return siteNewsRepository.findByOrganizationIdOrderByPublishedAtDesc(site.getOrganizationId()).stream()
                .map(NewsResponse::of)
                .toList();
    }

    @PostMapping("/api/public/sites/{slug}/admin/news")
    public NewsResponse createNews(@PathVariable String slug,
                                   @RequestBody Map<String, String> payload,
                                   @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = portal.site(slug);
        portal.requireSession(authHeader, site).require(OrgAction.MANAGE_CONTENT);

        String title = payload.get("title");
        String summary = payload.get("summary");
        String content = payload.get("content");
        String imageUrl = payload.get("imageUrl");

        if (title == null || title.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Judul berita tidak boleh kosong");
        }
        // Batas sama dengan kolom site_news (V23); tanpa ini isian yang terlalu panjang ditolak database menjadi 500.
        requireMaxLength(title.trim(), NEWS_TITLE_MAX, "Judul berita");
        requireMaxLength(summary, NEWS_TEXT_MAX, "Ringkasan berita");
        requireMaxLength(imageUrl, NEWS_TEXT_MAX, "Alamat gambar");

        SiteNews news = new SiteNews(site.getOrganizationId(), title.trim(), summary, content, imageUrl, null);
        return NewsResponse.of(siteNewsRepository.save(news));
    }

    @DeleteMapping("/api/public/sites/{slug}/admin/news/{newsId}")
    public Map<String, Object> deleteNews(@PathVariable String slug,
                                          @PathVariable UUID newsId,
                                          @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = portal.site(slug);
        portal.requireSession(authHeader, site);

        SiteNews news = siteNewsRepository.findById(newsId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Berita tidak ditemukan"));
        if (!news.getOrganizationId().equals(site.getOrganizationId())) {
            throw new ApiException(ErrorCode.ROLE_NOT_ALLOWED, "Berita bukan milik organisasi ini");
        }
        siteNewsRepository.delete(news);
        return Map.of("success", true, "message", "Berita berhasil dihapus");
    }

    // ---------- koleksi modul ----------

    @GetMapping("/api/public/sites/{slug}/admin/collections")
    public Map<String, List<Map<String, Object>>> getAdminCollections(@PathVariable String slug,
                                                                      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = portal.site(slug);
        portal.requireSession(authHeader, site);
        Map<String, List<Map<String, Object>>> collectionsMap = new LinkedHashMap<>();
        for (SiteCustomItem item : siteCustomItemRepository.findBySiteIdOrderByCollectionNameAscSortOrderAscCreatedAtDesc(site.getId())) {
            collectionsMap.computeIfAbsent(item.getCollectionName(), k -> new ArrayList<>()).add(row(item));
        }
        return collectionsMap;
    }

    @GetMapping("/api/public/sites/{slug}/admin/collections/{collectionName}")
    public List<Map<String, Object>> getAdminCollectionItems(@PathVariable String slug,
                                                             @PathVariable String collectionName,
                                                             @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = portal.site(slug);
        portal.requireSession(authHeader, site);
        String cleanCol = collectionName.toLowerCase(Locale.ROOT).trim();
        return siteCustomItemRepository.findBySiteIdAndCollectionNameOrderBySortOrderAscCreatedAtDesc(site.getId(), cleanCol)
                .stream().map(PortalContentController::row).toList();
    }

    @PostMapping("/api/public/sites/{slug}/admin/collections/{collectionName}")
    public Map<String, Object> addCollectionItem(@PathVariable String slug,
                                                 @PathVariable String collectionName,
                                                 @RequestBody Map<String, Object> itemData,
                                                 @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = portal.site(slug);
        portal.requireSession(authHeader, site);

        String cleanCol = collectionName.toLowerCase(Locale.ROOT).trim();
        SiteCustomItem item = new SiteCustomItem(site.getId(), cleanCol, cleanItemData(itemData), 0);
        return row(siteCustomItemRepository.save(item));
    }

    @PutMapping("/api/public/sites/{slug}/admin/collections/{collectionName}/{itemId}")
    public Map<String, Object> updateCollectionItem(@PathVariable String slug,
                                                    @PathVariable String collectionName,
                                                    @PathVariable UUID itemId,
                                                    @RequestBody Map<String, Object> itemData,
                                                    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = portal.site(slug);
        portal.requireSession(authHeader, site);

        SiteCustomItem item = siteCustomItemRepository.findById(itemId)
                .filter(i -> i.getSiteId().equals(site.getId()))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Item tidak ditemukan"));
        item.setData(cleanItemData(itemData));
        item.setUpdatedAt(Instant.now());
        return row(siteCustomItemRepository.save(item));
    }

    /** Urutan tampil item di website mengikuti urutan id yang dikirim Portal (atas ke bawah). */
    @PutMapping("/api/public/sites/{slug}/admin/collections/{collectionName}/order")
    public Map<String, Object> reorderCollection(@PathVariable String slug,
                                                 @PathVariable String collectionName,
                                                 @RequestBody Map<String, List<UUID>> payload,
                                                 @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = portal.site(slug);
        portal.requireSession(authHeader, site);

        List<UUID> ids = payload == null ? null : payload.get("ids");
        if (ids == null || ids.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Urutan item tidak boleh kosong");
        }
        String cleanCol = collectionName.toLowerCase(Locale.ROOT).trim();
        Map<UUID, SiteCustomItem> items = new HashMap<>();
        siteCustomItemRepository.findBySiteIdAndCollectionNameOrderBySortOrderAscCreatedAtDesc(site.getId(), cleanCol)
                .forEach(i -> items.put(i.getId(), i));
        if (ids.size() != items.size() || !items.keySet().containsAll(ids)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Daftar item sudah berubah. Muat ulang lalu coba lagi.");
        }
        for (int i = 0; i < ids.size(); i++) {
            items.get(ids.get(i)).setSortOrder(i);
        }
        siteCustomItemRepository.saveAll(items.values());
        return Map.of("success", true);
    }

    @DeleteMapping("/api/public/sites/{slug}/admin/collections/{collectionName}/{itemId}")
    public Map<String, Object> deleteCollectionItem(@PathVariable String slug,
                                                    @PathVariable String collectionName,
                                                    @PathVariable UUID itemId,
                                                    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = portal.site(slug);
        portal.requireSession(authHeader, site);

        SiteCustomItem item = siteCustomItemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Item tidak ditemukan"));
        if (!item.getSiteId().equals(site.getId())) {
            throw new ApiException(ErrorCode.ROLE_NOT_ALLOWED);
        }
        siteCustomItemRepository.delete(item);
        return Map.of("success", true, "message", "Item berhasil dihapus");
    }

    /** Foto untuk field gambar modul. Disimpan sebagai aset website, tampil di /{slug}/_media/{id}. */
    @PostMapping(value = "/api/public/sites/{slug}/admin/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadMedia(@PathVariable String slug,
                                           @RequestPart("file") MultipartFile file,
                                           @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = portal.site(slug);
        portal.requireSession(authHeader, site).require(OrgAction.MANAGE_CONTENT);
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new ApiException(ErrorCode.ASSET_INVALID);
        }
        UUID assetId = projectService.saveSiteMedia(site.getProjectId(), null, bytes).assetId();
        return Map.of("id", assetId, "url", "/" + site.getSlug() + "/_media/" + assetId);
    }

    /** Isian modul dari Portal: hanya teks, kosong dibuang, dibatasi agar satu item tidak membengkak. */
    static Map<String, Object> cleanItemData(Map<String, Object> itemData) {
        Map<String, Object> clean = new LinkedHashMap<>();
        if (itemData != null) {
            itemData.forEach((key, value) -> {
                String k = key == null ? "" : key.strip();
                String v = value == null ? "" : String.valueOf(value).strip();
                if (k.isEmpty() || k.equals("id") || v.isEmpty()) {
                    return;
                }
                if (k.length() > 60 || v.length() > MAX_VALUE_LENGTH) {
                    throw new ApiException(ErrorCode.VALIDATION_ERROR,
                            "Isian \"" + k + "\" terlalu panjang (maks " + MAX_VALUE_LENGTH + " karakter)");
                }
                clean.put(k, v);
            });
        }
        if (clean.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Data koleksi tidak boleh kosong");
        }
        if (clean.size() > MAX_FIELDS) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Maksimal " + MAX_FIELDS + " field per item");
        }
        return clean;
    }

    /** Satu item koleksi seperti yang dibaca Portal dan website: isian + id. */
    static Map<String, Object> row(SiteCustomItem item) {
        Map<String, Object> row = new HashMap<>(item.getData());
        row.put("id", item.getId());
        return row;
    }

    private static void requireMaxLength(String value, int max, String label) {
        if (value != null && value.length() > max) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, label + " maksimal " + max + " karakter.");
        }
    }
}
