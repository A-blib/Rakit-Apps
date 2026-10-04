package com.aris.templateapp.user.dto;

import com.aris.templateapp.provider.ProviderStatus;
import com.aris.templateapp.user.ActiveMode;
import com.aris.templateapp.user.WebsitePurpose;

import java.util.List;
import java.util.UUID;

/**
 * Data user untuk app. Selalu dibuat dari Entity lewat {@code UserService}, tidak pernah mengirim Entity langsung.
 *
 * @param roles          mode yang dimiliki user: "creator" jika punya profil pembuat website,
 *                       "provider" jika punya profil provider. Kosong sebelum onboarding.
 * @param providerStatus null jika user belum mendaftar sebagai provider
 * @param creatorProfile null jika user belum punya profil pembuat website
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
        CreatorProfileResponse creatorProfile) {

    public record CreatorProfileResponse(WebsitePurpose websitePurpose, String organizationName) {
    }
}
