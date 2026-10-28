package com.aris.templateapp.site;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.organization.MemberRole;
import com.aris.templateapp.organization.OrgAction;
import com.aris.templateapp.organization.PortalCredentialService;
import com.aris.templateapp.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Satu tempat untuk "website mana" dan "peran apa yang sedang masuk" bagi semua controller Portal Pengurus
 * ({@link PortalAuthController}, {@link PortalContentController}).
 * <p>
 * Token Portal (bukan token app) dibuat per website dan membawa peran + versi kredensial
 * ({@link JwtService#createSiteToken}). Versi dicek di setiap request, jadi saat Pemilik mengganti password suatu
 * peran, semua sesi Portal peran itu langsung keluar walau tokennya belum kedaluwarsa. Aturan peran per aksi
 * (mis. data formulir hanya Pemilik/Admin) memakai {@link OrgAction}, sama dengan jalur app.
 */
@Component
@RequiredArgsConstructor
public class PortalAccess {

    private static final String BEARER = "Bearer ";

    private final SiteRepository siteRepository;
    private final PortalCredentialService credentials;
    private final JwtService jwtService;

    /** Peran yang sedang masuk ke Portal sebuah website. */
    public record PortalSession(MemberRole role, String username) {

        public void require(OrgAction action) {
            if (!action.allowedFor(role)) {
                throw new ApiException(ErrorCode.ROLE_NOT_ALLOWED);
            }
        }
    }

    public Site site(String slug) {
        return siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));
    }

    /** Sesi dari token Portal yang sah untuk website ini dan masih memakai kredensial peran yang berlaku. */
    public PortalSession requireSession(String authHeader, Site site) {
        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Token autentikasi pengurus diperlukan");
        }
        ApiException expired = new ApiException(ErrorCode.UNAUTHORIZED, "Sesi login pengurus tidak valid atau telah berakhir");
        JwtService.SiteClaims claims = jwtService.parseSiteClaims(authHeader.substring(BEARER.length()).trim(), site.getId())
                .orElseThrow(() -> expired);
        MemberRole role;
        try {
            role = PersistableEnum.fromValue(MemberRole.class, claims.role());
        } catch (IllegalArgumentException e) {
            throw expired;
        }
        PortalCredentialService.Match current = credentials.current(site.getOrganizationId(), role)
                .filter(c -> c.version() == claims.version())
                .orElseThrow(() -> expired);
        return new PortalSession(role, current.username());
    }
}
