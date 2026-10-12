package com.aris.templateapp.organization;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, OrganizationMember.Key> {

    Optional<OrganizationMember> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    List<OrganizationMember> findByOrganizationId(UUID organizationId);

    long countByOrganizationId(UUID organizationId);

    long countByOrganizationIdAndRole(UUID organizationId, MemberRole role);

    boolean existsByUserId(UUID userId);

    /**
     * Semua anggota satu organisasi dengan baris terkunci sampai transaksi selesai. Dipakai sebelum aturan
     * "minimal satu Pemilik" dan "batas anggota", agar dua request bersamaan tidak sama-sama lolos.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from OrganizationMember m where m.organizationId = :organizationId")
    List<OrganizationMember> lockAllByOrganizationId(UUID organizationId);
}
