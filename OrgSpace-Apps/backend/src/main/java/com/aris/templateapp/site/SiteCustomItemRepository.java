package com.aris.templateapp.site;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SiteCustomItemRepository extends JpaRepository<SiteCustomItem, UUID> {

    List<SiteCustomItem> findBySiteIdAndCollectionNameOrderBySortOrderAscCreatedAtDesc(UUID siteId, String collectionName);

    List<SiteCustomItem> findBySiteIdOrderByCollectionNameAscSortOrderAscCreatedAtDesc(UUID siteId);

    void deleteBySiteIdAndCollectionName(UUID siteId, String collectionName);
}
