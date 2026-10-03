package com.aris.templateapp.user;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.provider.ProviderProfile;
import com.aris.templateapp.provider.ProviderProfileRepository;
import com.aris.templateapp.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public UserResponse getMe(UUID userId) {
        // Token sah tetapi user sudah tidak ada (mis. dihapus): perlakukan seperti belum login.
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
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
}
