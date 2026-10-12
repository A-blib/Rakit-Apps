package com.aris.templateapp.upload;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.provider.ProviderAccess;
import com.aris.templateapp.template.CheckResponses;
import com.aris.templateapp.template.CheckStatus;
import com.aris.templateapp.template.IssueSeverity;
import com.aris.templateapp.template.Template;
import com.aris.templateapp.template.TemplateCheck;
import com.aris.templateapp.template.TemplateCheckIssue;
import com.aris.templateapp.template.TemplateCheckIssueRepository;
import com.aris.templateapp.template.TemplateCheckRepository;
import com.aris.templateapp.template.TemplateRepository;
import com.aris.templateapp.template.TemplateStatus;
import com.aris.templateapp.template.dto.CheckResponse;
import com.aris.templateapp.upload.check.CheckStage;
import com.aris.templateapp.upload.check.TemplateChecker;
import com.aris.templateapp.upload.dto.CreateUploadSessionRequest;
import com.aris.templateapp.upload.dto.UploadCheckResponse;
import com.aris.templateapp.upload.dto.UploadOverviewResponse;
import com.aris.templateapp.upload.dto.UploadSessionResponse;
import com.aris.templateapp.upload.dto.UploadStartedResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Langkah 1–2 Upload (docs/rancangan/alur-fitur-upload.md): menerima ZIP per potongan, membuat/memperbarui
 * template, lalu memulai pengecekan di latar belakang ({@link TemplateCheckRunner}).
 */
@Service
public class UploadService {

    /** Status yang dihitung ke kuota draft: draft dan upload baru yang sedang dicek (sebentar lagi jadi draft). */
    static final List<TemplateStatus> DRAFT_QUOTA_STATUSES = List.of(TemplateStatus.DRAFT, TemplateStatus.CHECKING);

    private final ProviderAccess providerAccess;
    private final UploadSessionRepository sessionRepository;
    private final TemplateRepository templateRepository;
    private final TemplateCheckRepository checkRepository;
    private final TemplateCheckIssueRepository issueRepository;
    private final CheckReportRepository reportRepository;
    private final CheckResponses checkResponses;
    private final UploadStorage storage;
    private final ApplicationEventPublisher events;
    private final AppProperties.Upload settings;
    private final Clock clock;

    public UploadService(ProviderAccess providerAccess, UploadSessionRepository sessionRepository,
                         TemplateRepository templateRepository, TemplateCheckRepository checkRepository,
                         TemplateCheckIssueRepository issueRepository, CheckReportRepository reportRepository,
                         CheckResponses checkResponses, UploadStorage storage, ApplicationEventPublisher events,
                         AppProperties properties, Clock clock) {
        this.providerAccess = providerAccess;
        this.sessionRepository = sessionRepository;
        this.templateRepository = templateRepository;
        this.checkRepository = checkRepository;
        this.issueRepository = issueRepository;
        this.reportRepository = reportRepository;
        this.checkResponses = checkResponses;
        this.storage = storage;
        this.events = events;
        this.settings = properties.upload();
        this.clock = clock;
    }

    // ---------- halaman awal Upload (bagian 3.1) ----------

    @Transactional(readOnly = true)
    public UploadOverviewResponse overview(UUID userId) {
        providerAccess.requireActive(userId);
        Instant now = clock.instant();
        Duration keep = Duration.ofDays(settings.draftExpireDays());
        Duration warn = Duration.ofDays(settings.draftWarnDays());

        List<UploadOverviewResponse.NeedsFix> needsFix = templateRepository
                .findByProviderIdAndStatusOrderByUpdatedAtDesc(userId, TemplateStatus.CHECK_FAILED).stream()
                .map(t -> {
                    CheckResponse check = checkResponses.latest(t.getId());
                    int errors = check == null ? 0 : check.errors().size();
                    int warnings = check == null ? 0 : check.warnings().size();
                    return new UploadOverviewResponse.NeedsFix(t.getId(), displayFileName(t), errors, warnings, t.getUpdatedAt());
                })
                .toList();
        List<UploadOverviewResponse.Checking> checking = templateRepository
                .findByProviderIdAndStatusOrderByUpdatedAtDesc(userId, TemplateStatus.CHECKING).stream()
                .map(t -> new UploadOverviewResponse.Checking(t.getId(), displayFileName(t),
                        checkRepository.findFirstByTemplateIdOrderByVersionDesc(t.getId()).map(TemplateCheck::getStage).orElse(null)))
                .toList();
        List<UploadOverviewResponse.Draft> drafts = templateRepository
                .findByProviderIdAndStatusOrderByUpdatedAtDesc(userId, TemplateStatus.DRAFT).stream()
                .map(t -> {
                    Instant deleteAt = t.getUpdatedAt().plus(keep);
                    return new UploadOverviewResponse.Draft(t.getId(), t.getName(), t.getThumbnailUrl(), t.getWizardStep(),
                            t.getMarkingFieldCount(), t.getUpdatedAt(), deleteAt, !now.isBefore(deleteAt.minus(warn)));
                })
                .toList();
        int draftCount = drafts.size() + checking.size();
        return new UploadOverviewResponse(needsFix, checking, drafts, draftCount, settings.draftLimit(),
                draftCount < settings.draftLimit());
    }

    // ---------- langkah 1: upload per potongan (bagian 5.7b) ----------

    @Transactional
    public UploadSessionResponse createSession(UUID userId, CreateUploadSessionRequest request) {
        providerAccess.requireActive(userId);
        String fileName = request.fileName().strip();
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".rar")) {
            throw new ApiException(ErrorCode.RAR_NOT_SUPPORTED);
        }
        if (!lower.endsWith(".zip")) {
            throw new ApiException(ErrorCode.FILE_NOT_ZIP);
        }
        long max = settings.limits().maxZipBytes();
        if (request.totalSize() > max) {
            throw new ApiException(ErrorCode.UPLOAD_TOO_LARGE, "Ukuran ZIP maksimal " + max / (1024 * 1024) + " MB.");
        }
        if (request.templateId() != null) {
            requireFixable(ownedTemplate(userId, request.templateId()));
        } else {
            requireDraftQuota(userId);
        }

        UploadSession session = sessionRepository.save(
                new UploadSession(userId, request.templateId(), fileName, request.totalSize(), clock.instant()));
        storage.createSessionFile(session.getId());
        return toResponse(session);
    }

    @Transactional(readOnly = true)
    public UploadSessionResponse session(UUID userId, UUID sessionId) {
        providerAccess.requireActive(userId);
        return toResponse(sessionRepository.findByIdAndProviderId(sessionId, userId)
                .filter(s -> s.getStatus() == UploadSession.Status.UPLOADING)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Sesi upload tidak ditemukan atau sudah selesai.")));
    }

    /**
     * Menerima satu potongan. {@code offset} wajib sama dengan posisi yang sudah diterima server; jika berbeda
     * (mis. potongan sebelumnya ternyata sudah sampai), app diminta menanyakan posisi terakhir lalu melanjutkan.
     */
    @Transactional
    public UploadSessionResponse appendChunk(UUID userId, UUID sessionId, long offset, InputStream body) {
        providerAccess.requireActive(userId);
        UploadSession session = sessionRepository.lockOwned(sessionId, userId)
                .filter(s -> s.getStatus() == UploadSession.Status.UPLOADING)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Sesi upload tidak ditemukan atau sudah selesai."));
        if (offset != session.getReceivedSize()) {
            throw new ApiException(ErrorCode.UPLOAD_OFFSET_MISMATCH,
                    "Server sudah menerima " + session.getReceivedSize() + " byte. Lanjutkan dari posisi itu.");
        }
        long remaining = session.getTotalSize() - session.getReceivedSize();
        long written = storage.writeChunk(sessionId, offset, body, Math.min(settings.chunkSizeBytes(), remaining));
        if (written == 0) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Potongan upload kosong.");
        }
        session.setReceivedSize(session.getReceivedSize() + written);
        session.setUpdatedAt(clock.instant());
        return toResponse(session);
    }

    /** Semua potongan sudah diterima: pindahkan ZIP ke template lalu mulai pengecekan (langkah 2). */
    @Transactional
    public UploadStartedResponse complete(UUID userId, UUID sessionId) {
        providerAccess.requireActive(userId);
        UploadSession session = sessionRepository.lockOwned(sessionId, userId)
                .filter(s -> s.getStatus() == UploadSession.Status.UPLOADING)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Sesi upload tidak ditemukan atau sudah selesai."));
        if (session.getReceivedSize() != session.getTotalSize()) {
            throw new ApiException(ErrorCode.UPLOAD_INCOMPLETE, "Upload baru " + session.getReceivedSize() + " dari "
                    + session.getTotalSize() + " byte.");
        }

        Instant now = clock.instant();
        Template template;
        if (session.getTemplateId() != null) {
            // "Upload file perbaikan": ZIP baru menggantikan ZIP di catatan template yang sama (bagian 5.4).
            template = requireFixable(ownedTemplate(userId, session.getTemplateId()));
        } else {
            requireDraftQuota(userId);
            template = new Template(userId, TemplateChecker.nameFromFile(session.getFileName()), null);
        }
        template.setStatus(TemplateStatus.CHECKING);
        template.setSourceFileName(session.getFileName());
        template.setSourceSize(session.getTotalSize());
        template.setWizardStep(2);
        template.touch(now);
        template = templateRepository.save(template);

        storage.promote(session.getId(), template.getId());
        sessionRepository.delete(session);

        int version = checkRepository.findFirstByTemplateIdOrderByVersionDesc(template.getId())
                .map(c -> c.getVersion() + 1).orElse(1);
        TemplateCheck check = new TemplateCheck(template.getId(), version, CheckStatus.RUNNING, null);
        check.setStage(CheckStage.UPLOADED);
        check = checkRepository.save(check);

        // Pengecekan dijalankan setelah transaksi ini tersimpan, di thread lain, agar request upload cepat selesai.
        events.publishEvent(new UploadCompletedEvent(template.getId(), check.getId()));
        return new UploadStartedResponse(template.getId(), version);
    }

    // ---------- langkah 2: hasil pengecekan (bagian 5.4 & 5.8) ----------

    @Transactional(readOnly = true)
    public UploadCheckResponse progress(UUID userId, UUID templateId) {
        providerAccess.requireActive(userId);
        Template template = ownedTemplate(userId, templateId);
        return new UploadCheckResponse(template.getId(), template.getStatus(), template.getSourceFileName(),
                template.getSourceSize(), template.getWizardStep(), checkResponses.latest(templateId), template.getTechInfo());
    }

    /** Hapus draft atau upload yang gagal pengecekan, beserta file ZIP-nya. */
    @Transactional
    public void delete(UUID userId, UUID templateId) {
        providerAccess.requireActive(userId);
        Template template = ownedTemplate(userId, templateId);
        if (template.getStatus() != TemplateStatus.DRAFT && template.getStatus() != TemplateStatus.CHECK_FAILED) {
            throw new ApiException(ErrorCode.TEMPLATE_NOT_EDITABLE, "Hanya draft atau upload yang gagal yang bisa dihapus di sini.");
        }
        templateRepository.delete(template);
        storage.deleteTemplate(templateId);
    }

    // ---------- "Ini keliru? Laporkan" (bagian 5.9) ----------

    @Transactional
    public void report(UUID userId, UUID issueId, String reason) {
        providerAccess.requireActive(userId);
        TemplateCheckIssue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        TemplateCheck check = checkRepository.findById(issue.getCheckId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        ownedTemplate(userId, check.getTemplateId());
        if (issue.getSeverity() != IssueSeverity.ERROR) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Hanya Error yang bisa dilaporkan.");
        }
        if (reportRepository.existsByIssueId(issueId)) {
            throw new ApiException(ErrorCode.ALREADY_REPORTED);
        }
        String location = issue.getFile() == null ? null
                : issue.getFile() + (issue.getLine() != null ? " baris " + issue.getLine() : "");
        String cleanReason = reason == null || reason.isBlank() ? null : reason.strip();
        reportRepository.save(new CheckReport(check.getTemplateId(), check.getId(), issueId, issue.getCode(),
                issue.getRuleVersion(), location, issue.getSnippet(), cleanReason, userId));
    }

    // ---------- helper ----------

    private Template ownedTemplate(UUID userId, UUID templateId) {
        // Template milik provider lain diperlakukan seperti tidak ada, agar keberadaannya tidak bocor.
        return templateRepository.findByIdAndProviderId(templateId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
    }

    private static Template requireFixable(Template template) {
        if (template.getStatus() != TemplateStatus.CHECK_FAILED) {
            throw new ApiException(ErrorCode.TEMPLATE_NOT_EDITABLE, "File perbaikan hanya untuk upload yang gagal pengecekan.");
        }
        return template;
    }

    private void requireDraftQuota(UUID userId) {
        if (templateRepository.countByProviderIdAndStatusIn(userId, DRAFT_QUOTA_STATUSES) >= settings.draftLimit()) {
            throw new ApiException(ErrorCode.DRAFT_LIMIT_REACHED,
                    "Selesaikan atau hapus draft dulu (maks " + settings.draftLimit() + ").");
        }
    }

    private UploadSessionResponse toResponse(UploadSession session) {
        return new UploadSessionResponse(session.getId(), session.getFileName(), session.getTotalSize(),
                session.getReceivedSize(), settings.chunkSizeBytes());
    }

    private static String displayFileName(Template template) {
        return template.getSourceFileName() != null ? template.getSourceFileName() : template.getName();
    }
}
