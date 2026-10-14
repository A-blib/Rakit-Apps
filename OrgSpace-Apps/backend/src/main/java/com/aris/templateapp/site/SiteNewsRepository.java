package com.aris.templateapp.site;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SiteNewsRepository extends JpaRepository<SiteNews, UUID> {

    List<SiteNews> findByOrganizationIdOrderByPublishedAtDesc(UUID organizationId);
}
