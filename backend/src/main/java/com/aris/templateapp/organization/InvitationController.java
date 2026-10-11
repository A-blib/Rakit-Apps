package com.aris.templateapp.organization;

import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.organization.dto.AcceptInvitationRequest;
import com.aris.templateapp.organization.dto.InvitationCodeRequest;
import com.aris.templateapp.organization.dto.InvitationPreviewResponse;
import com.aris.templateapp.organization.dto.OrganizationResponse;
import com.aris.templateapp.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Organisasi", description = "Ruang kerja: profil, anggota, undangan, serah terima, riwayat aktivitas")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;
    private final CurrentUser currentUser;

    @Operation(summary = "Pratinjau kode undangan (nama, logo, peran)",
            description = "400 INVITATION_INVALID untuk kode salah/kedaluwarsa/dicabut; 429 setelah 10 percobaan per menit.")
    @PostMapping("/preview")
    public InvitationPreviewResponse preview(@Valid @RequestBody InvitationCodeRequest request) {
        return invitationService.preview(currentUser.id(), request.code());
    }

    @Operation(summary = "Gabung organisasi lewat kode undangan")
    @PostMapping("/accept")
    public OrganizationResponse accept(@Valid @RequestBody AcceptInvitationRequest request) {
        return invitationService.accept(currentUser.id(), request.code(), request.positionTitle());
    }
}
