package com.aris.templateapp.provider;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Penjaga semua endpoint {@code /providers/me/**}: user harus punya profil provider, dan profil itu tidak
 * boleh ditangguhkan (mode provider terkunci, alur-provider.md bagian 3.2).
 */
@Component
@RequiredArgsConstructor
public class ProviderAccess {

    private final ProviderProfileRepository providerProfileRepository;

    public ProviderProfile requireActive(UUID userId) {
        ProviderProfile profile = providerProfileRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.PROVIDER_REQUIRED));
        if (profile.isSuspended()) {
            throw new ApiException(ErrorCode.PROVIDER_SUSPENDED);
        }
        return profile;
    }
}
