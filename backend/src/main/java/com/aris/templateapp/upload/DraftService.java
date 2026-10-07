package com.aris.templateapp.upload;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.provider.ProviderAccess;
import com.aris.templateapp.template.IssueSeverity;
import com.aris.templateapp.template.Template;
import com.aris.templateapp.template.TemplateCheck;
import com.aris.templateapp.template.TemplateCheckIssue;
import com.aris.templateapp.template.TemplateCheckIssueRepository;
import com.aris.templateapp.template.TemplateCheckRepository;
import com.aris.templateapp.template.TemplateRepository;
import com.aris.templateapp.template.TemplateStatus;
import com.aris.templateapp.upload.check.CheckRule;
import com.aris.templateapp.upload.dto.DeviceWarningsRequest;
import com.aris.templateapp.upload.dto.DraftInfoRequest;
import com.aris.templateapp.upload.dto.DraftResponse;
import com.aris.templateapp.upload.dto.UploadSettingsResponse;
import com.aris.templateapp.user.WebsitePurpose;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Draft setelah file lolos pengecekan (alur-fitur-upload.md bagian 6): Info template yang tersimpan otomatis,
 * thumbnail, langkah wizard terakhir, ZIP untuk ditampilkan di HP, dan Peringatan dari WebView HP (tahap C).
 */
@Service
public class DraftService {

    static final int MAX_THUMBNAIL_BYTES = 1024 * 1024;
    private static final int KEYWORD_MAX = 20;
    private static final Set<String> THUMBNAIL_SOURCES = Set.of("auto", "section", "custom");
    private static final Set<String> THUMBNAIL_VIEWS = Set.of("mobile", "desktop");

    private final ProviderAccess providerAccess;
    private final TemplateRepository templateRepository;
    private final TemplateCheckRepository checkRepository;
    private final TemplateCheckIssueRepository issueRepository;
    private final UploadStorage storage;
    private final AppProperties.Upload settings;
    private final Clock clock;

    public DraftService(ProviderAccess providerAccess, TemplateRepository templateRepository,
                        TemplateCheckRepository checkRepository, TemplateCheckIssueRepository issueRepository,
                        UploadStorage storage, AppProperties properties, Clock clock) {
        this.providerAccess = providerAccess;
        this.templateRepository = templateRepository;
        this.checkRepository = checkRepository;
        this.issueRepository = issueRepository;
        this.storage = storage;
        this.settings = properties.upload();
        this.clock = clock;
    }

    public UploadSettingsResponse settings(UUID userId) {
        providerAccess.requireActive(userId);
        Set<String> hosts = new LinkedHashSet<>(settings.trustedCdnHosts());
        hosts.add("fonts.googleapis.com");
        hosts.add("fonts.gstatic.com");
        for (String iframe : settings.iframeAllowed()) {
            hosts.add(iframe.contains("/") ? iframe.substring(0, iframe.indexOf('/')) : iframe);
        }
        return new UploadSettingsResponse(settings.limits().maxZipBytes(), settings.chunkSizeBytes(),
                settings.draftLimit(), settings.limits().maxPages(), List.copyOf(hosts));
    }

    @Transactional(readOnly = true)
    public DraftResponse draft(UUID userId, UUID templateId) {
        providerAccess.requireActive(userId);
        return toResponse(ownedTemplate(userId, templateId));
    }

    @Transactional
    public DraftResponse updateInfo(UUID userId, UUID templateId, DraftInfoRequest request) {
        providerAccess.requireActive(userId);
        Template template = editableDraft(userId, templateId);
        if (request.name() != null && !request.name().isBlank()) {
            template.setName(request.name().strip());
        }
        if (request.category() != null) {
            try {
                template.setCategory(PersistableEnum.fromValue(WebsitePurpose.class, request.category()));
            } catch (IllegalArgumentException e) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "Kategori tidak dikenal.");
            }
        }
        if (request.description() != null) {
            String description = request.description().strip();
            template.setDescription(description.isEmpty() ? null : description);
        }
        if (request.keywords() != null) {
            template.setKeywords(normalizeKeywords(request.keywords()));
        }
        template.touch(clock.instant());
        return toResponse(template);
    }

    /** Kata kunci: huruf kecil, tanpa spasi di ujung, tanpa duplikat, masing-masing maks 20 karakter. */
    static List<String> normalizeKeywords(List<String> raw) {
        Set<String> result = new LinkedHashSet<>();
        for (String keyword : raw) {
            if (keyword == null) {
                continue;
            }
            String clean = keyword.strip().toLowerCase(Locale.ROOT);
            if (clean.isEmpty()) {
                continue;
            }
            if (clean.length() > KEYWORD_MAX) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "Kata kunci maksimal " + KEYWORD_MAX + " karakter.");
            }
            result.add(clean);
        }
        return new ArrayList<>(result);
    }

    @Transactional
    public DraftResponse updateStep(UUID userId, UUID templateId, int step) {
        providerAccess.requireActive(userId);
        Template template = editableDraft(userId, templateId);
        template.setWizardStep(step);
        template.touch(clock.instant());
        return toResponse(template);
    }

    /**
     * Thumbnail dari HP: hasil tangkapan otomatis/section, atau gambar provider (maks 1 MB, sudah dipotong app).
     * Jenis gambar dikenali dari isi file (magic bytes), bukan dari header yang dikirim app.
     */
    @Transactional
    public DraftResponse updateThumbnail(UUID userId, UUID templateId, byte[] image, String source, String view) {
        providerAccess.requireActive(userId);
        Template template = editableDraft(userId, templateId);
        if (!THUMBNAIL_SOURCES.contains(source) || !THUMBNAIL_VIEWS.contains(view)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Pilihan thumbnail tidak dikenal.");
        }
        String extension = imageExtension(image);
        if (extension == null || image.length > MAX_THUMBNAIL_BYTES) {
            throw new ApiException(ErrorCode.IMAGE_INVALID);
        }
        storage.writeThumbnail(templateId, image, extension);
        // ?v= berubah setiap thumbnail diganti, sehingga app tidak memakai gambar lama dari cache.
        template.setThumbnailUrl("/api/templates/" + templateId + "/thumbnail?v=" + clock.millis());
        template.setThumbnailSource(source);
        template.setThumbnailView(view);
        template.touch(clock.instant());
        return toResponse(template);
    }

    static String imageExtension(byte[] b) {
        if (b == null || b.length < 12) {
            return null;
        }
        if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if ((b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "png";
        }
        if (b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F' && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "webp";
        }
        return null;
    }

    /** ZIP template untuk ditampilkan di WebView HP (mis. draft dilanjutkan di HP lain). */
    @Transactional(readOnly = true)
    public Path source(UUID userId, UUID templateId) {
        providerAccess.requireActive(userId);
        ownedTemplate(userId, templateId);
        return storage.sourceZipPath(templateId);
    }

    /**
     * Peringatan tahap C dari WebView HP. Disimpan di pengecekan terakhir; hasil sebelumnya dari HP diganti,
     * sehingga menjalankan ulang tidak membuat peringatan ganda.
     */
    @Transactional
    public DraftResponse deviceWarnings(UUID userId, UUID templateId, DeviceWarningsRequest request) {
        providerAccess.requireActive(userId);
        Template template = editableDraft(userId, templateId);
        TemplateCheck check = checkRepository.findFirstByTemplateIdOrderByVersionDesc(templateId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        List<TemplateCheckIssue> existing = issueRepository.findByCheckId(check.getId());
        existing.stream().filter(i -> isDeviceRule(i.getCode())).forEach(issueRepository::delete);
        for (DeviceWarningsRequest.Warning w : request.warnings()) {
            if (!isDeviceRule(w.code())) {
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "Kode peringatan HP tidak dikenal: " + w.code());
            }
            CheckRule rule = CheckRule.valueOf(w.code());
            issueRepository.save(new TemplateCheckIssue(check.getId(), IssueSeverity.WARNING, rule.name(), rule.version(),
                    w.message(), w.file(), w.line(), suggestionFor(rule), null));
        }
        long serverWarnings = existing.stream()
                .filter(i -> i.getSeverity() == IssueSeverity.WARNING && !isDeviceRule(i.getCode())).count();
        template.setWarningCount((int) serverWarnings + request.warnings().size());
        return toResponse(template);
    }

    private static boolean isDeviceRule(String code) {
        return CheckRule.JS_RUNTIME_ERROR.name().equals(code) || CheckRule.HORIZONTAL_OVERFLOW.name().equals(code);
    }

    private static String suggestionFor(CheckRule rule) {
        return rule == CheckRule.JS_RUNTIME_ERROR
                ? "Buka halaman di browser laptop, lihat Console, lalu perbaiki baris yang disebut."
                : "Cari elemen dengan lebar tetap (mis. width: 1200px) dan ganti dengan max-width: 100%.";
    }

    private Template ownedTemplate(UUID userId, UUID templateId) {
        return templateRepository.findByIdAndProviderId(templateId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
    }

    private Template editableDraft(UUID userId, UUID templateId) {
        Template template = ownedTemplate(userId, templateId);
        if (template.getStatus() != TemplateStatus.DRAFT) {
            throw new ApiException(ErrorCode.TEMPLATE_NOT_EDITABLE, "Hanya draft yang bisa diubah di sini.");
        }
        return template;
    }

    private DraftResponse toResponse(Template t) {
        boolean duplicate = templateRepository.existsByProviderIdAndNameIgnoreCaseAndIdNot(t.getProviderId(), t.getName(), t.getId());
        return new DraftResponse(t.getId(), t.getStatus(), t.getWizardStep(), t.getSourceFileName(), t.getSourceSize(),
                t.getName(), t.getCategory(), t.getDescription(), List.copyOf(t.getKeywords()), t.getThumbnailUrl(),
                t.getThumbnailSource(), t.getThumbnailView(), t.getTechInfo(), t.getWarningCount(), t.getMarkingFieldCount(), duplicate,
                t.getUpdatedAt());
    }
}
