package com.aris.templateapp.upload.publish;

import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.notification.NotificationRepository;
import com.aris.templateapp.notification.NotificationService;
import com.aris.templateapp.notification.NotificationType;
import com.aris.templateapp.template.CheckStatus;
import com.aris.templateapp.template.IssueSeverity;
import com.aris.templateapp.template.Template;
import com.aris.templateapp.template.TemplateCheck;
import com.aris.templateapp.template.TemplateCheckIssue;
import com.aris.templateapp.template.TemplateCheckIssueRepository;
import com.aris.templateapp.template.TemplateCheckRepository;
import com.aris.templateapp.template.TemplateRepository;
import com.aris.templateapp.template.TemplateStatus;
import com.aris.templateapp.upload.UploadAsyncConfig;
import com.aris.templateapp.upload.UploadStorage;
import com.aris.templateapp.upload.check.CheckRule;
import com.aris.templateapp.upload.check.CheckStage;
import com.aris.templateapp.upload.check.Finding;
import com.aris.templateapp.upload.check.TemplateChecker;
import com.aris.templateapp.upload.check.TemplateFiles;
import com.aris.templateapp.upload.marking.MarkingData;
import com.aris.templateapp.upload.marking.MarkingValidator;
import com.aris.templateapp.upload.marking.TemplateNumbering;
import com.aris.templateapp.common.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Pengecekan akhir + pembuatan paket setelah Kirim (alur-fitur-upload.md bagian 9):
 * aturan file dijalankan sekali lagi, tandaan dicek lagi, library CDN disalin, atribut penandaan disisipkan.
 * Lolos → {@code published}; ada masalah (jarang) → kembali ke {@code draft} dengan daftar masalah.
 */
@Slf4j
@Component
public class PublishRunner {

    static final String LIBRARY_COPY_FAILED = "LIBRARY_COPY_FAILED";
    static final String MARKING_INVALID = "MARKING_INVALID";

    private final TemplateChecker checker;
    private final UploadStorage storage;
    private final LibraryFetcher fetcher;
    private final AppProperties.Upload settings;
    private final TemplateRepository templateRepository;
    private final TemplateCheckRepository checkRepository;
    private final TemplateCheckIssueRepository issueRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final TransactionTemplate tx;
    private final Clock clock;

    public PublishRunner(TemplateChecker checker, UploadStorage storage, LibraryFetcher fetcher, AppProperties properties,
                         TemplateRepository templateRepository, TemplateCheckRepository checkRepository,
                         TemplateCheckIssueRepository issueRepository, NotificationService notificationService,
                         NotificationRepository notificationRepository, TransactionTemplate tx, Clock clock) {
        this.checker = checker;
        this.storage = storage;
        this.fetcher = fetcher;
        this.settings = properties.upload();
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
    public void onSubmitted(SubmittedEvent event) {
        run(event.templateId(), event.checkId());
    }

    void run(UUID templateId, UUID checkId) {
        Template template = templateRepository.findById(templateId).orElse(null);
        if (template == null) {
            return;
        }
        List<Finding> findings = new ArrayList<>();
        List<String[]> extraErrors = new ArrayList<>();
        byte[] packageZip = null;
        try {
            byte[] zip = storage.readSource(templateId);
            TemplateChecker.Result result = checker.check(zip, template.getSourceFileName(),
                    stage -> tx.executeWithoutResult(s -> checkRepository.updateStage(checkId, stage)));
            findings.addAll(result.findings());
            MarkingData marking = template.getMarking();
            String markingProblem = markingProblem(zip, template, marking);
            if (markingProblem != null) {
                extraErrors.add(new String[]{MARKING_INVALID, markingProblem, "Buka langkah Tandai bagian, perbaiki, lalu Simpan."});
            }
            if (result.passed() && markingProblem == null) {
                TemplateFiles files = TemplateFiles.readZip(zip, settings.limits());
                packageZip = new PackageBuilder(settings, fetcher).build(files, marking);
            }
        } catch (PackageBuilder.LibraryCopyException e) {
            log.warn("Library template {} gagal disalin: {}", templateId, e.getMessage());
            extraErrors.add(new String[]{LIBRARY_COPY_FAILED, e.getMessage(),
                    "Biasanya karena CDN sedang tidak bisa dijangkau server. Coba Kirim lagi beberapa saat lagi."});
        } catch (IOException | RuntimeException e) {
            log.error("Pengecekan akhir template {} gagal karena kesalahan server", templateId, e);
            extraErrors.add(new String[]{"CHECK_INTERNAL_ERROR", "Pengecekan gagal karena kesalahan di server, bukan karena file-mu.",
                    "Coba Kirim lagi beberapa saat lagi."});
        }
        byte[] finalPackage = packageZip;
        tx.executeWithoutResult(s -> finish(templateId, checkId, findings, extraErrors, finalPackage));
    }

    /** Tandaan dicek ulang terhadap HTML asli; syarat minimal 3 isian dan info lengkap juga diulang. */
    private String markingProblem(byte[] zip, Template template, MarkingData marking) {
        if (marking == null || marking.fields() == null || marking.fields().size() < SubmitRules.MIN_FIELDS) {
            return "Tandai minimal " + SubmitRules.MIN_FIELDS + " isian.";
        }
        String info = SubmitRules.infoProblem(template);
        if (info != null) {
            return info;
        }
        TemplateFiles files = TemplateFiles.readZip(zip, settings.limits());
        if (files == null) {
            return "File template tidak bisa dibaca.";
        }
        Map<String, Set<Integer>> idsByPage = new HashMap<>();
        Map<String, Map<Integer, Integer>> parentsByPage = new HashMap<>();
        for (String page : files.pages()) {
            byte[] html = files.content(page);
            if (html != null) {
                Map<Integer, Integer> parents = TemplateNumbering.parents(new String(html, StandardCharsets.UTF_8));
                parentsByPage.put(page, parents);
                idsByPage.put(page, parents.keySet());
            }
        }
        Set<String> variables = new HashSet<>();
        if (template.getTechInfo() != null && template.getTechInfo().cssVariables() != null) {
            template.getTechInfo().cssVariables().forEach(v -> variables.add(v.name()));
        }
        try {
            MarkingValidator.validate(marking, idsByPage, variables);
            MarkingValidator.validateNesting(marking, parentsByPage);
            return null;
        } catch (ApiException e) {
            return e.getMessage();
        }
    }

    private void finish(UUID templateId, UUID checkId, List<Finding> findings, List<String[]> extraErrors,
                        byte[] packageZip) {
        Template template = templateRepository.findById(templateId).orElse(null);
        TemplateCheck check = checkRepository.findById(checkId).orElse(null);
        if (template == null || check == null) {
            return;
        }
        Instant now = clock.instant();
        for (Finding f : findings) {
            issueRepository.save(new TemplateCheckIssue(checkId, f.rule().severity(), f.rule().name(), f.rule().version(),
                    cut(f.message(), 300), cut(f.file(), 255), f.line(), cut(f.suggestion(), 300), cut(f.snippet(), 300)));
        }
        for (String[] error : extraErrors) {
            issueRepository.save(new TemplateCheckIssue(checkId, IssueSeverity.ERROR, error[0], 1, cut(error[1], 300),
                    null, null, cut(error[2], 300), null));
        }
        long warnings = findings.stream().filter(f -> f.rule().severity() == IssueSeverity.WARNING).count()
                + carryDeviceWarnings(templateId, checkId, check.getVersion());
        boolean passed = packageZip != null;
        check.setStatus(passed ? CheckStatus.PASSED : CheckStatus.FAILED);
        check.setStage(CheckStage.DONE);
        check.setFinishedAt(now);
        template.setWarningCount((int) warnings);
        template.touch(now);
        if (passed) {
            storage.writePackage(templateId, packageZip);
            template.setStatus(TemplateStatus.PUBLISHED);
            template.setPublishedAt(now);
            notificationRepository.findByTemplateIdAndTypeAndResolvedAtIsNull(templateId, NotificationType.TEMPLATE_CHECK_FAILED)
                    .forEach(n -> n.setResolvedAt(now));
            notificationService.create(template.getProviderId(), NotificationType.TEMPLATE_PUBLISHED, templateId,
                    "Template tayang", "\"" + template.getName() + "\" sudah tampil di galeri"
                            + (warnings > 0 ? ", dengan " + warnings + " peringatan yang sebaiknya diperbaiki." : "."));
        } else {
            // Kembali ke draft dengan daftar masalah (bagian 5.5); pekerjaan provider tidak hilang.
            template.setStatus(TemplateStatus.DRAFT);
            notificationService.create(template.getProviderId(), NotificationType.TEMPLATE_CHECK_FAILED, templateId,
                    "Template belum bisa tayang", "\"" + template.getName() + "\" gagal di pengecekan akhir. "
                            + "Buka draft-nya untuk melihat masalahnya.");
        }
    }

    /** Peringatan dari WebView HP (tahap C) di pengecekan sebelumnya ikut disalin, agar tidak hilang saat Kirim. */
    private long carryDeviceWarnings(UUID templateId, UUID checkId, int version) {
        long copied = 0;
        TemplateCheck previous = checkRepository.findByTemplateIdAndVersion(templateId, version - 1).orElse(null);
        if (previous == null) {
            return 0;
        }
        for (TemplateCheckIssue issue : issueRepository.findByCheckId(previous.getId())) {
            if (CheckRule.JS_RUNTIME_ERROR.name().equals(issue.getCode())
                    || CheckRule.HORIZONTAL_OVERFLOW.name().equals(issue.getCode())) {
                issueRepository.save(new TemplateCheckIssue(checkId, issue.getSeverity(), issue.getCode(),
                        issue.getRuleVersion(), issue.getMessage(), issue.getFile(), issue.getLine(),
                        issue.getSuggestion(), issue.getSnippet()));
                copied++;
            }
        }
        return copied;
    }

    private static String cut(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max - 1) + "…";
    }
}
