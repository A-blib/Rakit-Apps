package com.aris.templateapp.site;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FormSubmissionRepository extends JpaRepository<FormSubmission, UUID> {

    List<FormSubmission> findBySiteIdOrderByCreatedAtDesc(UUID siteId);

    List<FormSubmission> findBySiteIdAndFormNameOrderByCreatedAtDesc(UUID siteId, String formName);
}
