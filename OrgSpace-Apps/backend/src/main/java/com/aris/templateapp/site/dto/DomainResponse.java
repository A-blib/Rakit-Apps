package com.aris.templateapp.site.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DomainResponse(
        UUID id,
        UUID siteId,
        String hostname,
        String status, // pending, active, failed, removed
        String verificationToken,
        String targetCname,
        String targetA,
        Instant activatedAt,
        List<DomainInstructionDto> instructions,
        String lastError
) {}
