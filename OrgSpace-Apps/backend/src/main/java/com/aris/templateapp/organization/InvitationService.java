package com.aris.templateapp.organization;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.organization.dto.InvitationCreatedResponse;
import com.aris.templateapp.organization.dto.InvitationPreviewResponse;
import com.aris.templateapp.organization.dto.InvitationResponse;
import com.aris.templateapp.organization.dto.OrganizationResponse;
import com.aris.templateapp.security.TokenGenerator;
import com.aris.templateapp.user.User;
import com.aris.templateapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Kode undangan anggota (F1–F3). Kode hanya dikirim sekali saat dibuat; database menyimpan hash-nya,
 * sehingga daftar undangan tidak bisa dipakai untuk melihat ulang kode.
 */
@Service
@RequiredArgsConstructor
public class InvitationService {

    private final OrganizationInvitationRepository invitationRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final OrganizationAccess access;
    private final OrganizationService organizationService;
    private final ActivityService activityService;
    private final PortalCredentialService portalCredentials;
    private final InvitationCodes codes;
    private final TokenGenerator tokenGenerator;
    private final AttemptLimiter attemptLimiter;
    private final AppProperties properties;
    private final Clock clock;

    @Transactional
    public InvitationCreatedResponse create(UUID organizationId, UUID userId, MemberRole role) {
        OrganizationAccess.Membership membership = access.require(organizationId, userId, OrgAction.MANAGE_MEMBERS);
        if (membership.organization().isPersonal()) {
            throw new ApiException(ErrorCode.ROLE_NOT_ALLOWED, "Ruang pribadi tidak bisa mengundang anggota.");
        }
        if (!OrganizationAccess.canInvite(membership.role(), role)) {
            throw new ApiException(ErrorCode.ROLE_NOT_ALLOWED);
        }
        // Pengurus yang diundang masuk Portal dengan kredensial peran itu, jadi kredensialnya harus ada dulu.
        if (!portalCredentials.exists(organizationId, role)) {
            throw new ApiException(ErrorCode.PORTAL_CREDENTIAL_REQUIRED, "Atur dulu username & password Portal untuk peran "
                    + (role == MemberRole.ADMIN ? "Admin" : "Editor") + " di Kelola organisasi, lalu undang lagi.");
        }
        String code = codes.newCode();
        Instant expiresAt = clock.instant().plus(Duration.ofMinutes(properties.org().inviteTtlMinutes()));
        OrganizationInvitation invitation = invitationRepository.save(new OrganizationInvitation(
                organizationId, hash(code), role, userId, expiresAt));
        activityService.log(organizationId, userId, "invitation.created", "invitation", invitation.getId(),
                Map.of("role", role.value()));
        return new InvitationCreatedResponse(invitation.getId(), code, role, expiresAt);
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> listActive(UUID organizationId, UUID userId) {
        access.require(organizationId, userId, OrgAction.MANAGE_MEMBERS);
        List<OrganizationInvitation> invitations = invitationRepository.findActive(organizationId, clock.instant());
        Map<UUID, String> names = userRepository.findAllById(
                        invitations.stream().map(OrganizationInvitation::getCreatedBy).distinct().toList())
                .stream().collect(Collectors.toMap(User::getId, User::getDisplayName));
        return invitations.stream()
                .map(i -> new InvitationResponse(i.getId(), i.getRole(), names.get(i.getCreatedBy()),
                        i.getCreatedAt(), i.getExpiresAt()))
                .toList();
    }

    @Transactional
    public void revoke(UUID organizationId, UUID userId, UUID invitationId) {
        access.require(organizationId, userId, OrgAction.MANAGE_MEMBERS);
        OrganizationInvitation invitation = invitationRepository.findById(invitationId)
                .filter(i -> i.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (invitation.getRevokedAt() == null) {
            invitation.revoke(clock.instant());
            activityService.log(organizationId, userId, "invitation.revoked", "invitation", invitationId, null);
        }
    }

    @Transactional(readOnly = true)
    public InvitationPreviewResponse preview(UUID userId, String code) {
        attemptLimiter.check(userId);
        OrganizationInvitation invitation = findUsable(code, false);
        Organization organization = organizationRepository.findById(invitation.getOrganizationId())
                .orElseThrow(() -> new ApiException(ErrorCode.INVITATION_INVALID));
        return new InvitationPreviewResponse(organization.getName(), organization.getLogoUrl(), invitation.getRole());
    }

    /** Bergabung lewat kode. Ruang kerja yang baru dimasuki langsung menjadi ruang kerja terakhir. */
    @Transactional
    public OrganizationResponse accept(UUID userId, String code, String positionTitle) {
        attemptLimiter.check(userId);
        OrganizationInvitation invitation = findUsable(code, true);
        UUID organizationId = invitation.getOrganizationId();
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ApiException(ErrorCode.INVITATION_INVALID));

        // Dikunci agar dua orang yang bergabung bersamaan tidak melewati batas anggota.
        List<OrganizationMember> members = memberRepository.lockAllByOrganizationId(organizationId);
        if (members.stream().anyMatch(m -> m.getUserId().equals(userId))) {
            throw new ApiException(ErrorCode.ALREADY_MEMBER);
        }
        if (members.size() >= properties.org().maxMembers()) {
            throw new ApiException(ErrorCode.ORG_MEMBER_LIMIT);
        }
        memberRepository.save(new OrganizationMember(organizationId, userId, invitation.getRole(),
                OrganizationService.blankToNull(positionTitle), clock.instant()));
        // Sekali pakai: anggota yang sudah ada (ALREADY_MEMBER di atas) tidak menghabiskan kode.
        invitation.markUsed(userId, clock.instant());
        userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED))
                .setLastOrganizationId(organizationId);
        activityService.log(organizationId, userId, "member.joined", "invitation", invitation.getId(),
                Map.of("role", invitation.getRole().value()));
        return organizationService.toResponse(organization, invitation.getRole());
    }

    /** Kode salah, kedaluwarsa, dicabut, dan sudah dipakai dibalas sama, agar tidak membocorkan kode mana yang pernah ada. */
    private OrganizationInvitation findUsable(String code, boolean lock) {
        String normalized = InvitationCodes.normalize(code);
        if (normalized.length() != InvitationCodes.LENGTH) {
            throw new ApiException(ErrorCode.INVITATION_INVALID);
        }
        String codeHash = hash(normalized);
        return (lock ? invitationRepository.lockByCodeHash(codeHash) : invitationRepository.findByCodeHash(codeHash))
                .filter(i -> i.isUsable(clock.instant()))
                .orElseThrow(() -> new ApiException(ErrorCode.INVITATION_INVALID));
    }

    private String hash(String code) {
        return tokenGenerator.hash(InvitationCodes.normalize(code));
    }
}
