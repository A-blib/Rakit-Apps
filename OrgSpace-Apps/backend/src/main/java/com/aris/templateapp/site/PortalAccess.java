package com.aris.templateapp.site;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.organization.OrganizationMemberRepository;
import com.aris.templateapp.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

/**
 * Satu tempat untuk "website mana" dan "siapa pengurusnya" bagi semua controller Portal Pengurus
 * ({@link PortalAuthController}, {@link PortalContentController}, {@link PortalMemberController}).
 * <p>
 * Token Portal (bukan token app) dibuat per website dan hanya berlaku untuk website itu
 * ({@link JwtService#createSiteToken}). Keanggotaan dicek di setiap request, jadi anggota yang dikeluarkan langsung
 * kehilangan akses walau tokennya belum kedaluwarsa. Aturan peran per aksi (mis. data formulir hanya Pemilik/Admin)
 * tetap di service masing-masing (OrgAction), sama dengan jalur app.
 */
@Component
@RequiredArgsConstructor
public class PortalAccess {

    private static final String BEARER = "Bearer ";

    private final SiteRepository siteRepository;
    private final OrganizationMemberRepository memberRepository;
    private final JwtService jwtService;

    public Site site(String slug) {
        return siteRepository.findBySlug(slug.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Website tidak ditemukan"));
    }

    /** ID user dari token Portal yang sah untuk website ini dan masih anggota organisasinya. */
    public UUID requireManager(String authHeader, Site site) {
        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Token autentikasi pengurus diperlukan");
        }
        UUID userId = jwtService.parseSiteUserId(authHeader.substring(BEARER.length()).trim(), site.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "Sesi login pengurus tidak valid atau telah berakhir"));
        memberRepository.findByOrganizationIdAndUserId(site.getOrganizationId(), userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_ORG_MEMBER, "Anda bukan pengurus atau anggota dari organisasi website ini"));
        return userId;
    }
}
