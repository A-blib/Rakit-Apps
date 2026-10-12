package com.aris.templateapp.user.dto;

import com.aris.templateapp.provider.ProviderStatus;
import com.aris.templateapp.user.ActiveMode;

import java.util.List;
import java.util.UUID;

/**
 * Data user untuk app. Selalu dibuat dari Entity lewat {@code UserService}, tidak pernah mengirim Entity langsung.
 *
 * @param roles          mode yang dimiliki user: "creator" jika punya profil pembuat website,
 *                       "provider" jika punya profil provider. Kosong sebelum onboarding.
 * @param providerStatus null jika user belum mendaftar sebagai provider
 * @param lastOrganizationId ruang kerja terakhir yang dibuka; null jika belum punya ruang kerja
 */
public record UserResponse(
        UUID id,
        String displayName,
        String email,
        String avatarUrl,
        ActiveMode activeMode,
        boolean onboardingCompleted,
        List<ActiveMode> roles,
        ProviderStatus providerStatus,
        UUID lastOrganizationId) {
}
