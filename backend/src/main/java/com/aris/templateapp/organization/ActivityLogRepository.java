package com.aris.templateapp.organization;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    // Slice (bukan Page) karena app hanya butuh "masih ada lagi?", tanpa menghitung total baris.
    Slice<ActivityLog> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId, Pageable pageable);
}
