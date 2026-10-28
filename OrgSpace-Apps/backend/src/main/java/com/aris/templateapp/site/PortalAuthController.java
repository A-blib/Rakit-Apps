package com.aris.templateapp.site;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.organization.AttemptLimiter;
import com.aris.templateapp.organization.MemberRole;
import com.aris.templateapp.organization.PortalCredentialService;
import com.aris.templateapp.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Login Portal Pengurus ({@code {hosting}/{slug}/admin}) dengan kredensial Portal organisasi pemilik website
 * (username + password per peran, diatur Pemilik di app). Akun OrgSpace tidak bisa dipakai di sini.
 */
@RestController
@RequiredArgsConstructor
public class PortalAuthController {

    private final PortalAccess portal;
    private final PortalCredentialService credentials;
    private final AttemptLimiter attemptLimiter;
    private final JwtService jwtService;

    @PostMapping("/api/public/sites/{slug}/auth/login")
    public Map<String, Object> siteLogin(@PathVariable String slug, @RequestBody Map<String, String> payload,
                                         HttpServletRequest request) {
        String username = payload.get("username");
        String password = payload.get("password");
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Username dan password wajib diisi");
        }
        Site site = portal.site(slug);
        attemptLimiter.checkPortalLogin(request.getRemoteAddr(), site.getOrganizationId(), username);
        if (!credentials.anySet(site.getOrganizationId())) {
            throw new ApiException(ErrorCode.PORTAL_CREDENTIALS_NOT_SET);
        }
        // Satu jawaban untuk username tak dikenal dan password salah (termasuk password peran lain).
        PortalCredentialService.Match match = credentials.verify(site.getOrganizationId(), username, password)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS, "Username atau password salah."));

        return Map.of(
                "success", true,
                "token", jwtService.createSiteToken(site.getId(), match.role().value(), match.version()),
                "user", userInfo(match.role(), match.username()),
                "site", Map.of(
                        "slug", site.getSlug(),
                        "organizationId", site.getOrganizationId()
                )
        );
    }

    @GetMapping("/api/public/sites/{slug}/auth/me")
    public Map<String, Object> siteAuthMe(@PathVariable String slug,
                                          @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        PortalAccess.PortalSession session = portal.requireSession(authHeader, portal.site(slug));
        return Map.of("authenticated", true, "user", userInfo(session.role(), session.username()));
    }

    private static Map<String, Object> userInfo(MemberRole role, String username) {
        return Map.of("username", username, "role", role.value());
    }
}
