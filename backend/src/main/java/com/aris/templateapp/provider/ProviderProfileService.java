package com.aris.templateapp.provider;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.provider.dto.ProviderProfileResponse;
import com.aris.templateapp.provider.dto.ProviderProfileUpdateRequest;
import com.aris.templateapp.template.ProviderTemplateQueries;
import com.aris.templateapp.user.User;
import com.aris.templateapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Tab Profil provider versi awal (alur-provider.md bagian 7). */
@Service
@RequiredArgsConstructor
public class ProviderProfileService {

    private final ProviderAccess providerAccess;
    private final UserRepository userRepository;
    private final ProviderTemplateQueries queries;

    @Transactional(readOnly = true)
    public ProviderProfileResponse get(UUID userId) {
        return toResponse(userId, providerAccess.requireActive(userId));
    }

    @Transactional
    public ProviderProfileResponse update(UUID userId, ProviderProfileUpdateRequest request) {
        ProviderProfile profile = providerAccess.requireActive(userId);
        profile.setCreatorName(request.creatorName().trim());
        profile.setBio(blankToNull(request.bio()));
        profile.setPortfolioUrl(blankToNull(request.portfolioUrl()));
        profile.setSpecialties(request.specialties() == null ? List.of()
                : request.specialties().stream().map(String::trim).distinct().toList());
        return toResponse(userId, profile);
    }

    private ProviderProfileResponse toResponse(UUID userId, ProviderProfile profile) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
        return new ProviderProfileResponse(profile.getCreatorName(), profile.getBio(), profile.getPortfolioUrl(),
                profile.getSpecialties(), user.getAvatarUrl(), user.getEmail(), profile.getStatus(),
                queries.countPublished(userId), queries.totalDownloads(userId));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
