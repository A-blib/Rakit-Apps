package com.aris.templateapp.project.dto;

import com.aris.templateapp.project.ProjectMode;
import com.aris.templateapp.project.WebsiteStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Satu kartu di tab Website (alur-pembuatan-website.md 4.3).
 *
 * @param siteUrl  alamat website jika pernah diterbitkan, selain itu null
 * @param lockedBy diisi hanya jika website sedang diedit anggota <b>lain</b> (kunci belum kedaluwarsa)
 */
public record WebsiteSummaryResponse(
        UUID id,
        String name,
        ProjectMode mode,
        WebsiteStatus status,
        boolean hasUnpublishedChanges,
        String siteUrl,
        int missingCount,
        String thumbnailUrl,
        UUID sourceTemplateId,
        Instant createdAt,
        Instant updatedAt,
        String updatedByName,
        LockHolder lockedBy) {

    public record LockHolder(String name, Instant expiresAt) {
    }
}
