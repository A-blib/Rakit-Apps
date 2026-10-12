package com.aris.templateapp.template;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.template.dto.TemplateEventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

/**
 * Mencatat "dilihat" dan "didownload" (alur-provider.md bagian 3.5). Aturan:
 * - hanya template yang sedang tayang yang dihitung
 * - event dari pemilik template sendiri diabaikan
 * - download untuk project yang sama hanya dihitung sekali (dijaga database)
 * Semua event yang diabaikan tetap dijawab "diterima", agar app tidak perlu mengirim ulang.
 */
@Service
@RequiredArgsConstructor
public class TemplateEventService {

    /** Jam HP bisa sedikit maju; lebih dari ini dianggap tidak masuk akal. */
    private static final Duration MAX_CLOCK_SKEW = Duration.ofDays(1);

    private final TemplateRepository templateRepository;
    private final ProviderTemplateQueries queries;
    private final Clock clock;

    /** @param userId null untuk tamu */
    @Transactional
    public void record(UUID templateId, TemplateEventRequest request, UUID userId) {
        if (request.type() == TemplateEventType.DOWNLOAD && (request.projectId() == null || request.projectId().isBlank())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "projectId wajib untuk event download.");
        }
        if (request.occurredAt().isAfter(clock.instant().plus(MAX_CLOCK_SKEW))) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "occurredAt tidak boleh di masa depan.");
        }
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (template.getStatus() != TemplateStatus.PUBLISHED || template.getProviderId().equals(userId)) {
            return;
        }
        String projectId = request.type() == TemplateEventType.DOWNLOAD ? request.projectId() : null;
        queries.insertEvent(templateId, request.type(), projectId, request.installId(), userId, request.occurredAt());
    }
}
