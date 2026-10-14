package com.aris.templateapp.site;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SiteReleaseRepository extends JpaRepository<SiteRelease, UUID> {

    Optional<SiteRelease> findBySiteIdAndVersion(UUID siteId, int version);

    List<SiteRelease> findBySiteIdOrderByVersionDesc(UUID siteId);

    Optional<SiteRelease> findFirstBySiteIdOrderByVersionDesc(UUID siteId);
}
