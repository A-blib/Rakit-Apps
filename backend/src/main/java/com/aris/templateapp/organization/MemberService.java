package com.aris.templateapp.organization;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.organization.dto.MemberResponse;
import com.aris.templateapp.organization.dto.MemberUpdateRequest;
import com.aris.templateapp.organization.dto.OrganizationResponse;
import com.aris.templateapp.organization.dto.TransferOwnershipRequest;
import com.aris.templateapp.user.User;
import com.aris.templateapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Daftar anggota, ubah peran, keluarkan, keluar, dan serah terima (revisi-fondasi-orgspace.md 3.1 & 4.4).
 * Aksi yang menyentuh aturan "minimal satu Pemilik" selalu mengunci semua baris anggota organisasi lebih dulu.
 */
@Service
@RequiredArgsConstructor
public class MemberService {

    private final OrganizationMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final OrganizationAccess access;
    private final ActivityService activityService;
    private final OrganizationService organizationService;

    @Transactional(readOnly = true)
    public List<MemberResponse> list(UUID organizationId, UUID userId) {
        access.require(organizationId, userId, OrgAction.VIEW);
        List<OrganizationMember> members = memberRepository.findByOrganizationId(organizationId);
        Map<UUID, User> users = users(members);
        return members.stream()
                .sorted(Comparator.comparing(OrganizationMember::getRole)
                        .thenComparing(m -> users.get(m.getUserId()).getDisplayName(), String.CASE_INSENSITIVE_ORDER))
                .map(m -> toResponse(m, users.get(m.getUserId())))
                .toList();
    }

    @Transactional
    public MemberResponse update(UUID organizationId, UUID actorId, UUID targetUserId, MemberUpdateRequest request) {
        OrganizationAccess.Membership actor = access.require(organizationId, actorId, OrgAction.MANAGE_MEMBERS);
        List<OrganizationMember> members = memberRepository.lockAllByOrganizationId(organizationId);
        OrganizationMember target = find(members, targetUserId);
        // Admin tidak boleh menyentuh Pemilik sama sekali, termasuk jabatannya.
        if (!OrganizationAccess.canRemove(actor.role(), target.getRole())) {
            throw new ApiException(ErrorCode.ROLE_NOT_ALLOWED);
        }

        MemberRole oldRole = target.getRole();
        if (request.role() != null && request.role() != oldRole) {
            if (!OrganizationAccess.canChangeRole(actor.role(), oldRole, request.role())) {
                throw new ApiException(ErrorCode.ROLE_NOT_ALLOWED);
            }
            if (oldRole == MemberRole.OWNER && ownerCount(members) <= 1) {
                throw new ApiException(ErrorCode.LAST_OWNER);
            }
            target.setRole(request.role());
        }
        if (request.positionTitle() != null) {
            target.setPositionTitle(OrganizationService.blankToNull(request.positionTitle()));
        }

        User targetUser = user(targetUserId);
        if (target.getRole() != oldRole) {
            activityService.log(organizationId, actorId, "member.role_changed", "user", targetUserId,
                    Map.of("memberName", targetUser.getDisplayName(), "from", oldRole.value(), "to", target.getRole().value()));
        }
        return toResponse(target, targetUser);
    }

    @Transactional
    public void remove(UUID organizationId, UUID actorId, UUID targetUserId) {
        if (actorId.equals(targetUserId)) {
            leave(organizationId, actorId);
            return;
        }
        OrganizationAccess.Membership actor = access.require(organizationId, actorId, OrgAction.MANAGE_MEMBERS);
        List<OrganizationMember> members = memberRepository.lockAllByOrganizationId(organizationId);
        OrganizationMember target = find(members, targetUserId);
        if (!OrganizationAccess.canRemove(actor.role(), target.getRole())) {
            throw new ApiException(ErrorCode.ROLE_NOT_ALLOWED);
        }
        if (target.getRole() == MemberRole.OWNER && ownerCount(members) <= 1) {
            throw new ApiException(ErrorCode.LAST_OWNER);
        }
        User targetUser = user(targetUserId);
        memberRepository.delete(target);
        forgetLastOrganization(targetUser, organizationId);
        // Akses langsung tercabut: semua endpoint memeriksa keanggotaan di setiap request.
        activityService.log(organizationId, actorId, "member.removed", "user", targetUserId,
                Map.of("memberName", targetUser.getDisplayName()));
    }

    @Transactional
    public void leave(UUID organizationId, UUID userId) {
        access.require(organizationId, userId, OrgAction.VIEW);
        List<OrganizationMember> members = memberRepository.lockAllByOrganizationId(organizationId);
        OrganizationMember me = find(members, userId);
        if (!OrganizationAccess.canLeave(me.getRole(), ownerCount(members))) {
            throw new ApiException(ErrorCode.LAST_OWNER);
        }
        memberRepository.delete(me);
        forgetLastOrganization(user(userId), organizationId);
        activityService.log(organizationId, userId, "member.left");
    }

    /**
     * Serah terima: anggota penerima menjadi Pemilik, lalu Pemilik lama menjadi Admin/Editor atau keluar.
     * Dipakai saat pergantian kepengurusan; website dan datanya tetap milik organisasi.
     */
    @Transactional
    public OrganizationResponse transferOwnership(UUID organizationId, UUID ownerId, TransferOwnershipRequest request) {
        OrganizationAccess.Membership owner = access.require(organizationId, ownerId, OrgAction.TRANSFER_OWNERSHIP);
        if (owner.organization().isPersonal()) {
            throw new ApiException(ErrorCode.ROLE_NOT_ALLOWED, "Ruang pribadi tidak bisa diserahterimakan.");
        }
        if (ownerId.equals(request.userId())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Pilih anggota lain sebagai Pemilik baru.");
        }
        List<OrganizationMember> members = memberRepository.lockAllByOrganizationId(organizationId);
        OrganizationMember receiver = find(members, request.userId());
        OrganizationMember me = find(members, ownerId);
        receiver.setRole(MemberRole.OWNER);

        User receiverUser = user(request.userId());
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("memberName", receiverUser.getDisplayName());
        metadata.put("previousOwnerNow", request.keepMeAs());
        activityService.log(organizationId, ownerId, "org.ownership_transferred", "user", request.userId(), metadata);

        if ("leave".equals(request.keepMeAs())) {
            memberRepository.delete(me);
            forgetLastOrganization(user(ownerId), organizationId);
            activityService.log(organizationId, ownerId, "member.left");
            return null;
        }
        me.setRole("admin".equals(request.keepMeAs()) ? MemberRole.ADMIN : MemberRole.EDITOR);
        return organizationService.toResponse(owner.organization(), me.getRole());
    }

    private static OrganizationMember find(List<OrganizationMember> members, UUID userId) {
        return members.stream().filter(m -> m.getUserId().equals(userId)).findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Anggota tidak ditemukan."));
    }

    private static long ownerCount(List<OrganizationMember> members) {
        return members.stream().filter(m -> m.getRole() == MemberRole.OWNER).count();
    }

    /** Ruang kerja terakhir dikosongkan agar app tidak mencoba membuka ruang kerja yang sudah bukan miliknya. */
    private static void forgetLastOrganization(User user, UUID organizationId) {
        if (organizationId.equals(user.getLastOrganizationId())) {
            user.setLastOrganizationId(null);
        }
    }

    private User user(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
    }

    private Map<UUID, User> users(List<OrganizationMember> members) {
        return userRepository.findAllById(members.stream().map(OrganizationMember::getUserId).toList()).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private static MemberResponse toResponse(OrganizationMember m, User user) {
        return new MemberResponse(m.getUserId(), user.getDisplayName(), user.getAvatarUrl(), m.getRole(),
                m.getPositionTitle(), m.getJoinedAt());
    }
}
