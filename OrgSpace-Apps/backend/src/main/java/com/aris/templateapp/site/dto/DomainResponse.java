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
        String lastError,
        // Link bawaan orgspace.app/{slug}; tetap berlaku walau domain kustom aktif.
        String defaultUrl
) {}
