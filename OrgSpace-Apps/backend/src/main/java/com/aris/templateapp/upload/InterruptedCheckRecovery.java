package com.aris.templateapp.upload;

import com.aris.templateapp.template.CheckStatus;
import com.aris.templateapp.template.Template;
import com.aris.templateapp.template.TemplateCheck;
import com.aris.templateapp.template.TemplateCheckRepository;
import com.aris.templateapp.template.TemplateRepository;
import com.aris.templateapp.template.TemplateStatus;
import com.aris.templateapp.upload.publish.PublishRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

/**
 * Pengecekan upload dan Kirim berjalan di thread latar belakang; jika server mati di tengah jalan, template tertinggal
 * di status {@code checking} selamanya (memakan kuota draft dan tidak bisa dihapus). Saat server siap, pengecekan yang
 * dimulai sebelum server ini menyala pasti sudah terputus, jadi ditutup sebagai gagal karena server.
 */
@Slf4j
@Component
public class InterruptedCheckRecovery {

    private final TemplateCheckRepository checkRepository;
    private final TemplateRepository templateRepository;
    private final TemplateCheckRunner checkRunner;
    private final PublishRunner publishRunner;
    // Dicatat saat bean dibuat, sebelum seeder dev berjalan: contoh "sedang dicek" dari seeder tidak ikut ditutup.
    private final Instant startedAt;

    public InterruptedCheckRecovery(TemplateCheckRepository checkRepository, TemplateRepository templateRepository,
                                    TemplateCheckRunner checkRunner, PublishRunner publishRunner, Clock clock) {
        this.checkRepository = checkRepository;
        this.templateRepository = templateRepository;
        this.checkRunner = checkRunner;
        this.publishRunner = publishRunner;
        this.startedAt = clock.instant();
    }

    // shortcut: menganggap hanya satu server backend; jika nanti ada beberapa server, tandai pemilik tiap pengecekan.
    @EventListener(ApplicationReadyEvent.class)
    public void recover() {
        for (TemplateCheck check : checkRepository.findByStatusAndCreatedAtBefore(CheckStatus.RUNNING, startedAt)) {
            Template template = templateRepository.findById(check.getTemplateId()).orElse(null);
            if (template == null || template.getStatus() != TemplateStatus.CHECKING) {
                continue;
            }
            log.warn("Pengecekan template {} terputus saat server mati; ditutup sebagai gagal", template.getId());
            // Kirim selalu menaruh draft di langkah 6; upload (baru atau file perbaikan) di langkah 2.
            if (template.getWizardStep() == 6) {
                publishRunner.recoverInterrupted(template.getId(), check.getId());
            } else {
                checkRunner.recoverInterrupted(template.getId(), check.getId());
            }
        }
    }
}
