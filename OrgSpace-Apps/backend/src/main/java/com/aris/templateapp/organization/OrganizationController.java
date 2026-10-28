package com.aris.templateapp.organization;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.organization.dto.ActivityPageResponse;
import com.aris.templateapp.organization.dto.CreateInvitationRequest;
import com.aris.templateapp.organization.dto.CreateOrganizationRequest;
import com.aris.templateapp.organization.dto.DeleteOrganizationRequest;
import com.aris.templateapp.organization.dto.InvitationCreatedResponse;
import com.aris.templateapp.organization.dto.InvitationResponse;
import com.aris.templateapp.organization.dto.MemberResponse;
import com.aris.templateapp.organization.dto.MemberUpdateRequest;
import com.aris.templateapp.organization.dto.OrganizationResponse;
import com.aris.templateapp.organization.dto.OrganizationSummaryResponse;
import com.aris.templateapp.organization.dto.PortalCredentialRequest;
import com.aris.templateapp.organization.dto.PortalCredentialResponse;
import com.aris.templateapp.organization.dto.TransferOwnershipRequest;
import com.aris.templateapp.organization.dto.UpdateOrganizationRequest;
import com.aris.templateapp.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Tag(name = "Organisasi", description = "Ruang kerja: profil, anggota, undangan, serah terima, riwayat aktivitas")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;
    private final MemberService memberService;
    private final InvitationService invitationService;
    private final ActivityService activityService;
    private final PortalCredentialService portalCredentialService;
    private final OrganizationAccess access;
    private final CurrentUser currentUser;

    @Operation(summary = "Buat organisasi atau ruang pribadi (pembuat menjadi Pemilik)",
            description = "409 PERSONAL_SPACE_EXISTS jika ruang pribadi sudah ada; 400 CATEGORY_LEVEL_MISMATCH.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationResponse create(@Valid @RequestBody CreateOrganizationRequest request) {
        return organizationService.create(currentUser.id(), request);
    }

    @Operation(summary = "Daftar ruang kerja milik user (untuk pemilih ruang kerja)")
    @GetMapping
    public List<OrganizationSummaryResponse> list() {
        return organizationService.list(currentUser.id());
    }

    @Operation(summary = "Profil ruang kerja (semua anggota)")
    @GetMapping("/{id}")
    public OrganizationResponse get(@PathVariable UUID id) {
        return organizationService.get(id, currentUser.id());
    }

    @Operation(summary = "Ubah Profil organisasi (Admin ke atas)", description = "Semua isian dikirim ulang.")
    @PatchMapping("/{id}")
    public OrganizationResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateOrganizationRequest request) {
        return organizationService.update(id, currentUser.id(), request);
    }

    @Operation(summary = "Unggah logo (PNG/JPG, Admin ke atas)",
            description = "Server memotong persegi dan memperkecil maks 512 px; file asli tidak disimpan.")
    @PostMapping(path = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public OrganizationResponse uploadLogo(@PathVariable UUID id, @RequestPart("file") MultipartFile file) throws IOException {
        return organizationService.uploadLogo(id, currentUser.id(), file.getBytes());
    }

    @Operation(summary = "Gambar logo organisasi (publik)")
    @GetMapping("/{id}/logo")
    public ResponseEntity<Resource> logo(@PathVariable UUID id) {
        // URL logo membawa ?v=<waktu>, jadi gambar boleh disimpan lama di cache.
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)))
                .contentType(MediaType.IMAGE_PNG)
                .body(new FileSystemResource(organizationService.logoFile(id)));
    }

    @Operation(summary = "Hapus organisasi (Pemilik)", description = "Ketik ulang nama organisasi di confirmName.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id, @Valid @RequestBody DeleteOrganizationRequest request) {
        organizationService.delete(id, currentUser.id(), request.confirmName());
    }

    @Operation(summary = "Daftar anggota")
    @GetMapping("/{id}/members")
    public List<MemberResponse> members(@PathVariable UUID id) {
        return memberService.list(id, currentUser.id());
    }

    @Operation(summary = "Ubah peran/jabatan anggota (Admin ke atas; Admin tidak bisa mengubah Pemilik)")
    @PatchMapping("/{id}/members/{userId}")
    public MemberResponse updateMember(@PathVariable UUID id, @PathVariable UUID userId,
                                       @Valid @RequestBody MemberUpdateRequest request) {
        return memberService.update(id, currentUser.id(), userId, request);
    }

    @Operation(summary = "Keluarkan anggota (Admin ke atas; Admin tidak bisa mengeluarkan Pemilik)")
    @DeleteMapping("/{id}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable UUID id, @PathVariable UUID userId) {
        memberService.remove(id, currentUser.id(), userId);
    }

    @Operation(summary = "Keluar dari organisasi", description = "409 LAST_OWNER jika kamu Pemilik terakhir.")
    @PostMapping("/{id}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(@PathVariable UUID id) {
        memberService.leave(id, currentUser.id());
    }

    @Operation(summary = "Serah terima kepemilikan (Pemilik)",
            description = "keepMeAs: admin / editor / leave. Jika leave, balasan 204 tanpa isi.")
    @PostMapping("/{id}/transfer-ownership")
    public ResponseEntity<OrganizationResponse> transferOwnership(@PathVariable UUID id,
                                                                  @Valid @RequestBody TransferOwnershipRequest request) {
        OrganizationResponse response = memberService.transferOwnership(id, currentUser.id(), request);
        return response == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    @Operation(summary = "Buat kode undangan (Admin ke atas)", description = "Kode hanya dikirim sekali di sini.")
    @PostMapping("/{id}/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    public InvitationCreatedResponse createInvitation(@PathVariable UUID id,
                                                      @Valid @RequestBody CreateInvitationRequest request) {
        return invitationService.create(id, currentUser.id(), request.role());
    }

    @Operation(summary = "Daftar undangan aktif (tanpa kode)")
    @GetMapping("/{id}/invitations")
    public List<InvitationResponse> invitations(@PathVariable UUID id) {
        return invitationService.listActive(id, currentUser.id());
    }

    @Operation(summary = "Cabut undangan")
    @DeleteMapping("/{id}/invitations/{invitationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeInvitation(@PathVariable UUID id, @PathVariable UUID invitationId) {
        invitationService.revoke(id, currentUser.id(), invitationId);
    }

    @Operation(summary = "Kredensial Portal Pengurus yang sudah diatur (Pemilik)",
            description = "Hanya peran dan username; password tidak pernah dikirim.")
    @GetMapping("/{id}/portal-credentials")
    public List<PortalCredentialResponse> portalCredentials(@PathVariable UUID id) {
        return portalCredentialService.list(id, currentUser.id());
    }

    @Operation(summary = "Atur username & password Portal untuk satu peran (Pemilik)",
            description = "role = owner/admin/editor. Menyimpan ulang membuat semua sesi Portal peran itu keluar. "
                    + "409 PORTAL_USERNAME_TAKEN jika username dipakai peran lain.")
    @PutMapping("/{id}/portal-credentials/{role}")
    public PortalCredentialResponse savePortalCredential(@PathVariable UUID id, @PathVariable String role,
                                                         @Valid @RequestBody PortalCredentialRequest request) {
        MemberRole memberRole;
        try {
            memberRole = PersistableEnum.fromValue(MemberRole.class, role);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Peran harus owner, admin, atau editor");
        }
        return portalCredentialService.save(id, currentUser.id(), memberRole, request);
    }

    @Operation(summary = "Riwayat aktivitas (20 per halaman, terbaru dulu)")
    @GetMapping("/{id}/activities")
    public ActivityPageResponse activities(@PathVariable UUID id, @RequestParam(defaultValue = "0") int page) {
        access.require(id, currentUser.id(), OrgAction.VIEW);
        return activityService.list(id, page);
    }
}
