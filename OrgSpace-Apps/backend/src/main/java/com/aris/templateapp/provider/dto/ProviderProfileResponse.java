package com.aris.templateapp.provider.dto;

import com.aris.templateapp.provider.ProviderStatus;

import java.util.List;

/** Profil provider untuk tab Profil (alur-provider.md bagian 7). */
public record ProviderProfileResponse(
        String creatorName,
        String bio,
        String portfolioUrl,
        List<String> specialties,
        String avatarUrl,
        String email,
        ProviderStatus status,
        long publishedCount,
        long totalDownloads) {
}
