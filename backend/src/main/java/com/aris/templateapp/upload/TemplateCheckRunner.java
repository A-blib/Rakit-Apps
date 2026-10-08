package com.aris.templateapp.upload;

import com.aris.templateapp.notification.NotificationRepository;
import com.aris.templateapp.notification.NotificationService;
import com.aris.templateapp.notification.NotificationType;
import com.aris.templateapp.template.CheckStatus;
import com.aris.templateapp.template.Template;
import com.aris.templateapp.template.TemplateCheck;
import com.aris.templateapp.template.TemplateCheckIssue;
import com.aris.templateapp.template.TemplateCheckIssueRepository;
import com.aris.templateapp.template.TemplateCheckRepository;
import com.aris.templateapp.template.TemplateRepository;
import com.aris.templateapp.template.TemplateStatus;
import com.aris.templateapp.template.IssueSeverity;
import com.aris.templateapp.upload.check.CheckStage;
import com.aris.templateapp.upload.check.Finding;
import com.aris.templateapp.upload.check.TemplateChecker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Menjalankan {@link TemplateChecker} di thread latar belakang setelah upload selesai, mencatat setiap tahap
 * (bagian 5.8), lalu menyimpan hasilnya: lolos → draft, gagal → check_failed + notifikasi "Perlu tindakan".
 * <p>
 * Setiap penulisan memakai transaksi pendek sendiri, agar tahap yang sedang berjalan langsung terlihat oleh app
 * yang memantau, bukan baru di akhir.
 */
@Slf4j
@Component
public class TemplateCheckRunner {

    /** Kode masalah jika pengecekan sendiri yang gagal (bug server), bukan karena file provider. */
    static final String INTERNAL_CHECK_ERROR = "CHECK_INTERNAL_ERROR";

    private final TemplateChecker checker;
    private final UploadStorage storage;
    private final TemplateRepository templateRepository;
    private final TemplateCheckRepository checkRepository;
    private final TemplateCheckIssueRepository issueRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final TransactionTemplate tx;
    private final Clock clock;

    public TemplateCheckRunner(TemplateChecker checker, UploadStorage storage, TemplateRepository templateRepository,
                               TemplateCheckRepository checkRepository, TemplateCheckIssueRepository issueRepository,
                               NotificationService notificationService, NotificationRepository notificationRepository,
                               TransactionTemplate tx, Clock clock) {
        this.checker = checker;
        this.storage = storage;
        this.templateRepository = templateRepository;
        this.checkRepository = checkRepository;
        this.issueRepository = issueRepository;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.tx = tx;
        this.clock = clock;
    }

    @Async(UploadAsyncConfig.CHECK_EXECUTOR)
    @TransactionalEventListener
    public void onUploadCompleted(UploadCompletedEvent event) {
        run(event.templateId(), event.checkId());
    }

    void run(UUID templateId, UUID checkId) {
        Template template = templateRepository.findById(templateId).orElse(null);
        if (template == null) {
            return; // dihapus sebelum sempat dicek
        }
        TemplateChecker.Result result;
        boolean crashed = false;
        try {
            byte[] zip = storage.readSource(templateId);
            result = checker.check(zip, template.getSourceFileName(),
                    stage -> tx.executeWithoutResult(s -> checkRepository.updateStage(checkId, stage)));
        } catch (RuntimeException e) {
            log.error("Pengecekan template {} gagal karena kesalahan server", templateId, e);
            result = new TemplateChecker.Result(List.of(), null);
            crashed = true;
        }
        TemplateChecker.Result finalResult = result;
        boolean finalCrashed = crashed;
        tx.executeWithoutResult(s -> finish(templateId, checkId, finalResult, finalCrashed));
    }

    private void finish(UUID templateId, UUID checkId, TemplateChecker.Result result, boolean crashed) {
        Template template = templateRepository.findById(templateId).orElse(null);
        TemplateCheck check = checkRepository.findById(checkId).orElse(null);
        if (template == null || check == null) {
            return;
        }
        Instant now = clock.instant();
        for (Finding f : result.findings()) {
            issueRepository.save(new TemplateCheckIssue(checkId, f.rule().severity(), f.rule().name(), f.rule().version(),
                    cut(f.message(), 300), cut(f.file(), 255), f.line(), cut(f.suggestion(), 300), cut(f.snippet(), 300)));
        }
        if (crashed) {
            issueRepository.save(new TemplateCheckIssue(checkId, IssueSeverity.ERROR, INTERNAL_CHECK_ERROR, 1,
                    "Pengecekan gagal karena kesalahan di server, bukan karena file-mu.", null, null,
                    "Coba upload ulang file yang sama beberapa saat lagi.", null));
        }
        boolean passed = !crashed && result.passed();
        check.setStatus(passed ? CheckStatus.PASSED : CheckStatus.FAILED);
        check.setStage(CheckStage.DONE);
        check.setFinishedAt(now);

        template.setStatus(passed ? TemplateStatus.DRAFT : TemplateStatus.CHECK_FAILED);
        template.setWarningCount((int) result.warningCount());
        if (result.techInfo() != null) {
            template.setTechInfo(result.techInfo());
        }
        // Lolos → lanjut ke langkah 3 (Info template). Gagal → tetap di langkah 2.
        template.setWizardStep(passed ? Math.max(3, template.getWizardStep()) : 2);
        template.touch(now);

        String file = template.getSourceFileName();
        if (passed) {
            // Masalah upload sebelumnya sudah beres: kartu notifikasi lama tidak lagi "perlu tindakan".
            notificationRepository.findByTemplateIdAndTypeAndResolvedAtIsNull(templateId, NotificationType.TEMPLATE_CHECK_FAILED)
                    .forEach(n -> n.setResolvedAt(now));
            notificationService.create(template.getProviderId(), NotificationType.UPLOAD_CHECK_PASSED, templateId,
                    "File lolos pengecekan", file + " siap dilanjutkan. Isi info template dari tab Upload.");
        } else {
            long errors = result.findings().stream().filter(f -> f.rule().severity() == IssueSeverity.ERROR).count()
                    + (crashed ? 1 : 0);
            notificationService.create(template.getProviderId(), NotificationType.TEMPLATE_CHECK_FAILED, templateId,
                    "File belum memenuhi standar", file + ": " + errors + " error · " + result.warningCount()
                            + " peringatan. Buka tab Upload untuk memperbaikinya.");
        }
    }

    private static String cut(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max - 1) + "…";
    }
}
