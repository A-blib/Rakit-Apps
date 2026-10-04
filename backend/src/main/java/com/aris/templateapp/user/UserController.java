package com.aris.templateapp.user;

import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.security.CurrentUser;
import com.aris.templateapp.user.dto.ActiveModeRequest;
import com.aris.templateapp.user.dto.CreatorOnboardingRequest;
import com.aris.templateapp.user.dto.ProviderOnboardingRequest;
import com.aris.templateapp.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "Data user yang sedang login, onboarding, dan mode")
// Semua endpoint di sini butuh access token; ikon gembok muncul di Swagger UI.
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CurrentUser currentUser;

    @Operation(summary = "Ambil data user yang sedang login")
    @GetMapping
    public UserResponse me() {
        return userService.getMe(currentUser.id());
    }

    @Operation(summary = "Selesaikan onboarding pembuat website (tombol Mulai maupun Lewati)")
    @PostMapping("/onboarding/creator")
    public UserResponse onboardCreator(@Valid @RequestBody CreatorOnboardingRequest request) {
        return userService.completeCreatorOnboarding(currentUser.id(), request);
    }

    @Operation(summary = "Daftar sebagai penyedia template (langsung aktif)",
            description = "409 PROVIDER_PROFILE_EXISTS jika sudah pernah mendaftar.")
    @PostMapping("/onboarding/provider")
    public UserResponse onboardProvider(@Valid @RequestBody ProviderOnboardingRequest request) {
        return userService.becomeProvider(currentUser.id(), request);
    }

    @Operation(summary = "Beralih mode (creator / provider)",
            description = "403 MODE_NOT_ALLOWED jika belum punya profil provider atau provider ditangguhkan.")
    @PatchMapping("/active-mode")
    public UserResponse changeActiveMode(@Valid @RequestBody ActiveModeRequest request) {
        return userService.changeActiveMode(currentUser.id(), request.mode());
    }
}
