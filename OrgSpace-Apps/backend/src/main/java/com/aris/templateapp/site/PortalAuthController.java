package com.aris.templateapp.site;

import com.aris.templateapp.auth.AuthService;
import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.organization.AttemptLimiter;
import com.aris.templateapp.organization.OrganizationMember;
import com.aris.templateapp.organization.OrganizationMemberRepository;
import com.aris.templateapp.security.JwtService;
import com.aris.templateapp.user.User;
import com.aris.templateapp.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Login Portal Pengurus ({@code {hosting}/{slug}/admin}) dengan akun OrgSpace (email + password). */
@RestController
@RequiredArgsConstructor
public class PortalAuthController {

    private final PortalAccess portal;
    private final AuthService authService;
    private final AttemptLimiter attemptLimiter;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final OrganizationMemberRepository memberRepository;

    @PostMapping("/api/public/sites/{slug}/auth/login")
    public Map<String, Object> siteLogin(@PathVariable String slug, @RequestBody Map<String, String> payload,
                                         HttpServletRequest request) {
        String identifier = payload.get("identifier") != null ? payload.get("identifier") : payload.get("email");
        String password = payload.get("password");
        if (identifier == null || password == null || identifier.isBlank() || password.isBlank()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Email/username dan password wajib diisi");
        }
        // Endpoint publik yang memeriksa password akun OrgSpace mana pun: batasnya sama dengan login app.
        attemptLimiter.checkLogin(request.getRemoteAddr(), identifier);
        Site site = portal.site(slug);

        // Satu jawaban untuk semua kegagalan. Dulu "bukan pengurus" (NOT_ORG_MEMBER) hanya muncul jika password BENAR,
        // sehingga login Portal website mana pun bisa dipakai menguji tebakan password akun OrgSpace siapa saja.
        ApiException rejected = new ApiException(ErrorCode.INVALID_CREDENTIALS,
                "Email atau password salah, atau akun ini bukan pengurus website ini.");
        UUID userId = authService.verifyLocalPassword(identifier, password).orElseThrow(() -> rejected);
        User user = userRepository.findById(userId).orElseThrow(() -> rejected);
        OrganizationMember member = memberRepository.findByOrganizationIdAndUserId(site.getOrganizationId(), user.getId())
                .orElseThrow(() -> rejected);

        String token = jwtService.createSiteToken(user.getId(), site.getId());
        return Map.of(
                "success", true,
                "token", token,
                "user", userInfo(user, member),
                "site", Map.of(
                        "slug", site.getSlug(),
                        "organizationId", site.getOrganizationId()
                )
        );
    }

    @GetMapping("/api/public/sites/{slug}/auth/me")
    public Map<String, Object> siteAuthMe(@PathVariable String slug,
                                          @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        Site site = portal.site(slug);
        UUID userId = portal.requireManager(authHeader, site);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User tidak ditemukan"));
        OrganizationMember member = memberRepository.findByOrganizationIdAndUserId(site.getOrganizationId(), userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_ORG_MEMBER));
        return Map.of("authenticated", true, "user", userInfo(user, member));
    }

    private static Map<String, Object> userInfo(User user, OrganizationMember member) {
        return Map.of(
                "id", user.getId(),
                "name", user.getDisplayName() != null ? user.getDisplayName() : user.getEmail(),
                "email", user.getEmail(),
                "role", member.getRole().name().toLowerCase(Locale.ROOT)
        );
    }
}
