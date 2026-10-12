package com.aris.templateapp.organization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    boolean existsByCreatedByAndType(UUID createdBy, OrganizationType type);

    /** Ruang kerja milik user beserta perannya, urut nama (dipakai pemilih ruang kerja). */
    @Query("""
            select o as organization, m.role as role from Organization o, OrganizationMember m
            where m.organizationId = o.id and m.userId = :userId
            order by o.type asc, lower(o.name) asc""")
    List<OrganizationWithRole> findAllForUser(UUID userId);

    interface OrganizationWithRole {
        Organization getOrganization();

        MemberRole getRole();
    }
}
