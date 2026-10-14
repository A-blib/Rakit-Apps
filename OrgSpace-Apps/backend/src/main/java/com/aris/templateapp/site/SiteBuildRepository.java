package com.aris.templateapp.site;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SiteBuildRepository extends JpaRepository<SiteBuild, UUID> {

    Optional<SiteBuild> findFirstByProjectIdOrderByCreatedAtDesc(UUID projectId);

    Optional<SiteBuild> findFirstBySiteIdOrderByCreatedAtDesc(UUID siteId);

    @Query(value = """
            SELECT * FROM site_builds
            WHERE status = 'queued' AND (not_before IS NULL OR not_before <= :now)
            ORDER BY created_at ASC
            LIMIT 1
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    Optional<SiteBuild> findNextQueued(Instant now);

    List<SiteBuild> findByStatus(String status);
}
