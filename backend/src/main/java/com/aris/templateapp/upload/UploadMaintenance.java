package com.aris.templateapp.upload;

import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.notification.NotificationService;
import com.aris.templateapp.notification.NotificationType;
import com.aris.templateapp.template.Template;
import com.aris.templateapp.template.TemplateRepository;
import com.aris.templateapp.template.TemplateStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Pekerjaan terjadwal fitur Upload (alur-provider.md bagian 5.4), berjalan tiap malam pukul 03.00:
 * <ul>
 *   <li>draft yang tidak disentuh {@code draft-expire-days} hari dihapus, dengan pemberitahuan
 *       {@code draft-warn-days} hari sebelumnya;</li>
 *   <li>sesi upload yang tidak pernah selesai dibersihkan beserta file sementaranya.</li>
 * </ul>
 */
@Slf4j
@Component
public class UploadMaintenance {

    private final TemplateRepository templateRepository;
    private final UploadSessionRepository sessionRepository;
    private final UploadStorage storage;
    private final NotificationService notificationService;
    private final TransactionTemplate tx;
    private final AppProperties.Upload settings;
    private final Clock clock;

    public UploadMaintenance(TemplateRepository templateRepository, UploadSessionRepository sessionRepository,
                             UploadStorage storage, NotificationService notificationService, TransactionTemplate tx,
                             AppProperties properties, Clock clock) {
        this.templateRepository = templateRepository;
        this.sessionRepository = sessionRepository;
        this.storage = storage;
        this.notificationService = notificationService;
        this.tx = tx;
        this.settings = properties.upload();
        this.clock = clock;
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "${app.timezone}")
    public void nightly() {
        expireDrafts();
        cleanupSessions();
    }

    void expireDrafts() {
        Instant now = clock.instant();
        Instant deleteBefore = now.minus(Duration.ofDays(settings.draftExpireDays()));
        Instant warnBefore = now.minus(Duration.ofDays(settings.draftExpireDays() - settings.draftWarnDays()));
        for (Template draft : templateRepository.findByStatusAndUpdatedAtBefore(TemplateStatus.DRAFT, warnBefore)) {
            tx.executeWithoutResult(s -> {
                Template t = templateRepository.findById(draft.getId()).orElse(null);
                if (t == null || t.getStatus() != TemplateStatus.DRAFT) {
                    return;
                }
                if (t.getUpdatedAt().isBefore(deleteBefore)) {
                    // template_id dikosongkan: baris template ikut terhapus, notifikasinya tetap harus ada.
                    notificationService.create(t.getProviderId(), NotificationType.DRAFT_DELETED, null,
                            "Draft dihapus", "Draft \"" + t.getName() + "\" dihapus karena tidak disentuh "
                                    + settings.draftExpireDays() + " hari.");
                    templateRepository.delete(t);
                    storage.deleteTemplate(t.getId());
                } else if (t.getExpiryNotifiedAt() == null) {
                    notificationService.create(t.getProviderId(), NotificationType.DRAFT_EXPIRING, t.getId(),
                            "Draft akan dihapus", "Draft \"" + t.getName() + "\" akan dihapus dalam "
                                    + settings.draftWarnDays() + " hari jika tidak dilanjutkan.");
                    t.setExpiryNotifiedAt(now);
                }
            });
        }
    }

    void cleanupSessions() {
        Instant before = clock.instant().minus(Duration.ofHours(settings.sessionExpireHours()));
        for (UploadSession session : sessionRepository.findByStatusAndUpdatedAtBefore(UploadSession.Status.UPLOADING, before)) {
            sessionRepository.delete(session);
            storage.deleteSession(session.getId());
        }
    }
}
