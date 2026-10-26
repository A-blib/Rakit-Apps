package com.aris.templateapp.user;

import com.aris.templateapp.auth.GitHubAuthService;
import com.aris.templateapp.auth.dto.GitHubAuthorizeRequest;
import com.aris.templateapp.auth.dto.TicketExchangeRequest;
import com.aris.templateapp.auth.dto.UrlResponse;
import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.security.CurrentUser;
import com.aris.templateapp.user.dto.GoogleIdTokenRequest;
import com.aris.templateapp.user.dto.IdentityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Metode login", description = "Metode login yang tersambung ke akun (Pengaturan)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/users/me/identities")
@RequiredArgsConstructor
public class IdentityController {

    private final IdentityService identityService;
    private final GitHubAuthService gitHubAuthService;
    private final CurrentUser currentUser;

    @Operation(summary = "Daftar metode login yang tersambung")
    @GetMapping
    public List<IdentityResponse> list() {
        return identityService.list(currentUser.id());
    }

    @Operation(summary = "Sambungkan akun Google ke akun ini")
    @PostMapping("/google")
    public List<IdentityResponse> linkGoogle(@Valid @RequestBody GoogleIdTokenRequest request) {
        return identityService.linkGoogle(currentUser.id(), request.idToken());
    }

    @Operation(summary = "Buat URL login GitHub untuk menyambungkan GitHub ke akun ini",
            description = "Setelah login di GitHub, callback redirect ke ?linkTicket=...; app menukarnya lewat /github/exchange.")
    @PostMapping("/github/authorize-url")
    public UrlResponse linkGitHubUrl(@Valid @RequestBody GitHubAuthorizeRequest request) {
        return new UrlResponse(gitHubAuthService.authorizeUrlForLinking(currentUser.id(), request.codeChallenge()));
    }

    @Operation(summary = "Selesaikan penyambungan GitHub dengan tiket dari deep link",
            description = "400 TICKET_INVALID jika tiket/codeVerifier salah, 403 LINK_USER_MISMATCH jika tiket milik akun lain, "
                    + "409 IDENTITY_IN_USE jika GitHub itu sudah dipakai akun lain.")
    @PostMapping("/github/exchange")
    public List<IdentityResponse> completeGitHubLink(@Valid @RequestBody TicketExchangeRequest request) {
        UUID userId = currentUser.id();
        gitHubAuthService.completeLinkFromSettings(userId, request.ticket(), request.codeVerifier());
        return identityService.list(userId);
    }

    @Operation(summary = "Lepaskan metode login (local, google, atau github)",
            description = "Ditolak dengan 409 LAST_IDENTITY jika ini metode login terakhir.")
    @DeleteMapping("/{provider}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlink(@PathVariable String provider) {
        identityService.unlink(currentUser.id(), provider);
    }
}
