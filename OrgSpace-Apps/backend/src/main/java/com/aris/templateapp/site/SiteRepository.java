package com.aris.templateapp.site;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SiteRepository extends JpaRepository<Site, UUID> {

    Optional<Site> findByProjectId(UUID projectId);

    Optional<Site> findBySlug(String slug);

    boolean existsBySlug(String slug);

    long countByOrganizationId(UUID organizationId);
}
