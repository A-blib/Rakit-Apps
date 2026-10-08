package com.aris.templateapp.upload.publish;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.provider.ProviderAccess;
import com.aris.templateapp.template.CheckStatus;
import com.aris.templateapp.template.Template;
import com.aris.templateapp.template.TemplateCheck;
import com.aris.templateapp.template.TemplateCheckRepository;
import com.aris.templateapp.template.TemplateRepository;
import com.aris.templateapp.template.TemplateStatus;
import com.aris.templateapp.upload.check.CheckStage;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Langkah 6 Upload: Kirim (alur-fitur-upload.md bagian 9). Syarat yang bisa dicek langsung ditolak di sini
 * (info template, minimal 3 isian, konfirmasi hak pakai aset); pengecekan akhir dan pembuatan paket berjalan di
 * latar belakang ({@link PublishRunner}).
 */
@Service
public class PublishService {

    private final ProviderAccess providerAccess;
    private final TemplateRepository templateRepository;
    private final TemplateCheckRepository checkRepository;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public PublishService(ProviderAccess providerAccess, TemplateRepository templateRepository,
                          TemplateCheckRepository checkRepository, ApplicationEventPublisher events, Clock clock) {
        this.providerAccess = providerAccess;
        this.templateRepository = templateRepository;
        this.checkRepository = checkRepository;
        this.events = events;
        this.clock = clock;
    }

    @Transactional
    public void submit(UUID userId, UUID templateId, SubmitRequest request) {
        providerAccess.requireActive(userId);
        Template template = templateRepository.findByIdAndProviderId(templateId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (template.getStatus() != TemplateStatus.DRAFT) {
            throw new ApiException(ErrorCode.TEMPLATE_NOT_EDITABLE, "Hanya draft yang bisa dikirim.");
        }
        if (!request.agreedAssetRights()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Centang konfirmasi hak pakai gambar, font, dan isi.");
        }
        String infoProblem = SubmitRules.infoProblem(template);
        if (infoProblem != null) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, infoProblem);
        }
        if (template.getMarkingFieldCount() < SubmitRules.MIN_FIELDS) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR,
                    "Tandai minimal " + SubmitRules.MIN_FIELDS + " isian sebelum mengirim.");
        }

        template.setStatus(TemplateStatus.CHECKING);
        template.setWizardStep(6);
        template.touch(clock.instant());
        int version = checkRepository.findFirstByTemplateIdOrderByVersionDesc(templateId)
                .map(c -> c.getVersion() + 1).orElse(1);
        TemplateCheck check = new TemplateCheck(templateId, version, CheckStatus.RUNNING, null);
        check.setStage(CheckStage.UPLOADED);
        check = checkRepository.save(check);
        events.publishEvent(new SubmittedEvent(templateId, check.getId()));
    }
}
