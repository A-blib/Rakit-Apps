package com.aris.templateapp.security;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Mengambil ID user yang sedang login (hasil {@link JwtAuthFilter}) untuk dipakai controller. */
@Component
public class CurrentUser {

    /** ID user jika request membawa token sah; null untuk tamu (dipakai endpoint publik). */
    public UUID optionalId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof UUID userId ? userId : null;
    }

    public UUID id() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UUID userId) {
            return userId;
        }
        throw new ApiException(ErrorCode.UNAUTHORIZED);
    }
}
