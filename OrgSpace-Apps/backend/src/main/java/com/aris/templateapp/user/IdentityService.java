package com.aris.templateapp.user;

import com.aris.templateapp.auth.AccountLinkingService;
import com.aris.templateapp.auth.IdentityProvider;
import com.aris.templateapp.auth.UserIdentity;
import com.aris.templateapp.auth.UserIdentityRepository;
import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.security.GoogleTokenVerifier;
import com.aris.templateapp.user.dto.IdentityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** Metode login terhubung milik user yang sedang login: lihat, sambungkan Google, dan lepaskan. */
@Service
@RequiredArgsConstructor
public class IdentityService {

    private final UserRepository userRepository;
    private final UserIdentityRepository identityRepository;
    private final AccountLinkingService accountLinkingService;
    private final GoogleTokenVerifier googleTokenVerifier;

    @Transactional(readOnly = true)
    public List<IdentityResponse> list(UUID userId) {
        return identityRepository.findByUserId(userId).stream()
                .sorted(Comparator.comparing(UserIdentity::getCreatedAt))
                .map(i -> new IdentityResponse(i.getProvider(), i.getEmail(), i.getCreatedAt()))
                .toList();
    }

    @Transactional
    public List<IdentityResponse> linkGoogle(UUID userId, String idToken) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
        accountLinkingService.linkToUser(user, googleTokenVerifier.verify(idToken));
        return list(userId);
    }

    /** Melepas satu metode login. Metode terakhir ditolak, karena tanpa itu user tidak bisa masuk lagi. */
    @Transactional
    public void unlink(UUID userId, String providerValue) {
        IdentityProvider provider;
        try {
            provider = PersistableEnum.fromValue(IdentityProvider.class, providerValue);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.NOT_FOUND);
        }
        // Kunci baris user: dua request "lepas" bersamaan tidak boleh sama-sama lolos cek "bukan yang terakhir".
        userRepository.findByIdForUpdate(userId).orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));

        List<UserIdentity> identities = identityRepository.findByUserId(userId);
        UserIdentity target = identities.stream()
                .filter(i -> i.getProvider() == provider)
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (identities.size() <= 1) {
            throw new ApiException(ErrorCode.LAST_IDENTITY);
        }
        identityRepository.delete(target);
    }
}
