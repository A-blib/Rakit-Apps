package com.aris.templateapp.organization;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Satu-satunya tempat pengecekan peran di ruang kerja. Setiap endpoint organisasi (dan nanti website, konten,
 * data masuk) memanggil {@link #require} lebih dulu; tampilan Android hanya menyembunyikan tombol.
 * <p>
 * Aturan pengelolaan anggota dibuat {@code static} tanpa database supaya bisa dites per baris tabel 3.1.
 */
@Component
@RequiredArgsConstructor
public class OrganizationAccess {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;

    /** Memastikan organisasi ada, user anggotanya, dan perannya cukup untuk {@code action}. */
    public Membership require(UUID organizationId, UUID userId, OrgAction action) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ApiException(ErrorCode.ORG_NOT_FOUND));
        OrganizationMember member = memberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_ORG_MEMBER));
        if (!action.allowedFor(member.getRole())) {
            throw new ApiException(ErrorCode.ROLE_NOT_ALLOWED);
        }
        return new Membership(organization, member);
    }

    /**
     * Boleh mengubah peran anggota {@code target} menjadi {@code newRole}? Pemilik hanya dibuat lewat serah terima,
     * dan Admin tidak boleh menyentuh Pemilik.
     */
    public static boolean canChangeRole(MemberRole actor, MemberRole target, MemberRole newRole) {
        if (!OrgAction.MANAGE_MEMBERS.allowedFor(actor) || newRole == MemberRole.OWNER) {
            return false;
        }
        return actor == MemberRole.OWNER || target != MemberRole.OWNER;
    }

    /** Boleh mengeluarkan anggota {@code target}? Admin tidak boleh mengeluarkan Pemilik. */
    public static boolean canRemove(MemberRole actor, MemberRole target) {
        if (!OrgAction.MANAGE_MEMBERS.allowedFor(actor)) {
            return false;
        }
        return actor == MemberRole.OWNER || target != MemberRole.OWNER;
    }

    /** Peran yang boleh diberikan lewat undangan: Admin hanya boleh dibuat oleh Admin/Pemilik, Pemilik tidak pernah. */
    public static boolean canInvite(MemberRole actor, MemberRole invitedRole) {
        return OrgAction.MANAGE_MEMBERS.allowedFor(actor) && invitedRole != MemberRole.OWNER;
    }

    /** Pemilik hanya boleh keluar jika masih ada Pemilik lain (LAST_OWNER). */
    public static boolean canLeave(MemberRole role, long ownerCount) {
        return role != MemberRole.OWNER || ownerCount > 1;
    }

    public record Membership(Organization organization, OrganizationMember member) {
        public MemberRole role() {
            return member.getRole();
        }
    }
}
