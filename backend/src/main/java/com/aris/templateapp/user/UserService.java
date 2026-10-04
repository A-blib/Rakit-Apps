package com.aris.templateapp.user;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.provider.ProviderProfile;
import com.aris.templateapp.provider.ProviderProfileRepository;
import com.aris.templateapp.user.dto.CreatorOnboardingRequest;
import com.aris.templateapp.user.dto.ProviderOnboardingRequest;
import com.aris.templateapp.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CreatorProfileRepository creatorProfileRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public UserResponse getMe(UUID userId) {
        return toResponse(findUser(userId));
    }

    /**
     * Onboarding pembuat website (juga dipakai tombol "Lewati"). Membuat atau memperbarui profil creator,
     * menandai onboarding selesai, dan membuka mode pembuat website.
     */
    @Transactional
    public UserResponse completeCreatorOnboarding(UUID userId, CreatorOnboardingRequest request) {
        User user = findUser(userId);
        user.setDisplayName(request.displayName().trim());

        CreatorProfile profile = creatorProfileRepository.findById(userId).orElseGet(() -> new CreatorProfile(userId));
        profile.setWebsitePurpose(request.websitePurpose());
        profile.setOrganizationName(blankToNull(request.organizationName()));
        creatorProfileRepository.save(profile);

        user.setOnboardingCompleted(true);
        user.setActiveMode(ActiveMode.CREATOR);
        return toResponse(user);
    }

    /**
     * Mendaftar sebagai penyedia template, baik dari onboarding akun baru maupun dari tombol
     * "Jadi penyedia template" milik user lama. Provider langsung {@code active} (tanpa verifikasi admin)
     * dan mode langsung pindah ke provider.
     */
    @Transactional
    public UserResponse becomeProvider(UUID userId, ProviderOnboardingRequest request) {
        // Baris user dikunci agar dua request bersamaan tidak sama-sama lolos cek "belum punya profil".
        User user = userRepository.findByIdForUpdate(userId).orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
        if (providerProfileRepository.existsById(userId)) {
            throw new ApiException(ErrorCode.PROVIDER_PROFILE_EXISTS);
        }

        ProviderProfile profile = new ProviderProfile(userId, request.creatorName().trim(), clock.instant());
        profile.setBio(blankToNull(request.bio()));
        profile.setPortfolioUrl(blankToNull(request.portfolioUrl()));
        if (request.specialties() != null) {
            profile.setSpecialties(request.specialties().stream().map(String::trim).distinct().toList());
        }
        providerProfileRepository.save(profile);

        user.setOnboardingCompleted(true);
        user.setActiveMode(ActiveMode.PROVIDER);
        return toResponse(user);
    }

    /**
     * Beralih mode. Mode pembuat website selalu boleh (dashboard ini juga dipakai tamu dan
     * menjadi tujuan saat provider ditangguhkan). Mode provider butuh profil provider yang tidak ditangguhkan.
     */
    @Transactional
    public UserResponse changeActiveMode(UUID userId, ActiveMode mode) {
        User user = findUser(userId);
        if (mode == ActiveMode.PROVIDER) {
            ProviderProfile provider = providerProfileRepository.findById(userId)
                    .orElseThrow(() -> new ApiException(ErrorCode.MODE_NOT_ALLOWED,
                            "Daftar sebagai penyedia template terlebih dahulu."));
            if (provider.isSuspended()) {
                throw new ApiException(ErrorCode.MODE_NOT_ALLOWED, "Akun provider ditangguhkan.");
            }
        }
        user.setActiveMode(mode);
        return toResponse(user);
    }

    /** Menyusun UserResponse lengkap: data user + peran dan profil yang dimiliki. */
    @Transactional(readOnly = true)
    public UserResponse toResponse(User user) {
        Optional<CreatorProfile> creator = creatorProfileRepository.findById(user.getId());
        Optional<ProviderProfile> provider = providerProfileRepository.findById(user.getId());

        List<ActiveMode> roles = new ArrayList<>();
        creator.ifPresent(c -> roles.add(ActiveMode.CREATOR));
        provider.ifPresent(p -> roles.add(ActiveMode.PROVIDER));

        return new UserResponse(
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getActiveMode(),
                user.isOnboardingCompleted(),
                roles,
                provider.map(ProviderProfile::getStatus).orElse(null),
                creator.map(c -> new UserResponse.CreatorProfileResponse(c.getWebsitePurpose(), c.getOrganizationName()))
                        .orElse(null));
    }

    private User findUser(UUID userId) {
        // Token sah tetapi user sudah tidak ada (mis. dihapus): perlakukan seperti belum login.
        return userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
