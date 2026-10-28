package com.aris.templateapp.organization;

import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationInvitationRepository extends JpaRepository<OrganizationInvitation, UUID> {

    Optional<OrganizationInvitation> findByCodeHash(String codeHash);

    /** Dikunci saat menerima undangan: dua orang yang memakai kode yang sama bersamaan tidak boleh sama-sama masuk. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from OrganizationInvitation i where i.codeHash = :codeHash")
    Optional<OrganizationInvitation> lockByCodeHash(String codeHash);

    @Query("""
            select i from OrganizationInvitation i
            where i.organizationId = :organizationId and i.revokedAt is null and i.usedAt is null and i.expiresAt > :now
            order by i.createdAt desc""")
    List<OrganizationInvitation> findActive(UUID organizationId, Instant now);
}
