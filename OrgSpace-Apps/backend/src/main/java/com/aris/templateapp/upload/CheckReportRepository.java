package com.aris.templateapp.upload;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CheckReportRepository extends JpaRepository<CheckReport, UUID> {

    boolean existsByIssueId(UUID issueId);

    List<CheckReport> findByIssueIdIn(Collection<UUID> issueIds);
}
