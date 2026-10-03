package com.aris.templateapp.auth;

import com.aris.templateapp.auth.dto.AuthResponse;
import com.aris.templateapp.auth.dto.GitHubAuthorizeRequest;
import com.aris.templateapp.auth.dto.GoogleLoginRequest;
import com.aris.templateapp.auth.dto.LoginRequest;
import com.aris.templateapp.auth.dto.RefreshTokenRequest;
import com.aris.templateapp.auth.dto.RegisterRequest;
import com.aris.templateapp.auth.dto.TicketExchangeRequest;
import com.aris.templateapp.auth.dto.UrlResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint publik untuk masuk (email, Google, GitHub). Tidak butuh access token. */
@Tag(name = "Auth", description = "Daftar, masuk (email/Google/GitHub), refresh token, dan keluar")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final int DEVICE_NAME_MAX = 100;

    private final AuthService authService;
    private final GitHubAuthService gitHubAuthService;

    @Operation(summary = "Daftar akun baru dengan email + password")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request,
                                 @Parameter(hidden = true) @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent) {
        return authService.register(request, deviceName(userAgent));
    }

    @Operation(summary = "Masuk dengan email + password")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request,
                              @Parameter(hidden = true) @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent) {
        return authService.login(request, deviceName(userAgent));
    }

    @Operation(summary = "Masuk/daftar dengan idToken Google",
            description = "409 ACCOUNT_LINK_REQUIRED berisi linkToken jika email Google sudah dipakai akun lain.")
    @PostMapping("/google")
    public AuthResponse google(@Valid @RequestBody GoogleLoginRequest request,
                               @Parameter(hidden = true) @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent) {
        return authService.loginWithGoogle(request, deviceName(userAgent));
    }

    @Operation(summary = "Buat URL halaman login GitHub (dibuka app di Custom Tab)")
    @PostMapping("/github/authorize-url")
    public UrlResponse gitHubAuthorizeUrl(@RequestBody(required = false) GitHubAuthorizeRequest request) {
        return new UrlResponse(gitHubAuthService.authorizeUrlForLogin(request == null ? null : request.linkToken()));
    }

    @Operation(summary = "Callback dari GitHub (dipanggil browser, bukan app)",
            description = "Selalu redirect ke deep link: ?ticket=..., ?result=linked, atau ?error=KODE.")
    @GetMapping("/github/callback")
    public ResponseEntity<Void> gitHubCallback(@RequestParam(required = false) String code,
                                               @RequestParam(required = false) String state,
                                               @RequestParam(required = false) String error) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(gitHubAuthService.handleCallback(code, state, error))
                .build();
    }

    @Operation(summary = "Tukar tiket dari deep link GitHub menjadi token")
    @PostMapping("/github/exchange")
    public AuthResponse gitHubExchange(@Valid @RequestBody TicketExchangeRequest request,
                                       @Parameter(hidden = true) @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent) {
        return authService.exchangeLoginTicket(request.ticket(), deviceName(userAgent));
    }

    @Operation(summary = "Tukar refresh token dengan access token + refresh token baru (rotasi)")
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request,
                                @Parameter(hidden = true) @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent) {
        return authService.refresh(request, deviceName(userAgent));
    }

    @Operation(summary = "Keluar: cabut refresh token")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
    }

    /** User-Agent dipakai sebagai nama perangkat di tabel refresh_tokens (dipotong sesuai panjang kolom). */
    private static String deviceName(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return null;
        }
        return userAgent.length() <= DEVICE_NAME_MAX ? userAgent : userAgent.substring(0, DEVICE_NAME_MAX);
    }
}
