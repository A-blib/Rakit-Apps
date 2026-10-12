package com.aris.templateapp.organization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationInvitationRepository extends JpaRepository<OrganizationInvitation, UUID> {

    Optional<OrganizationInvitation> findByCodeHash(String codeHash);

    @Query("""
            select i from OrganizationInvitation i
            where i.organizationId = :organizationId and i.revokedAt is null and i.expiresAt > :now
            order by i.createdAt desc""")
    List<OrganizationInvitation> findActive(UUID organizationId, Instant now);
}
