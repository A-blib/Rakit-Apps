package com.aris.templateapp.site;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface FormSubmissionRepository extends JpaRepository<FormSubmission, UUID> {

    List<FormSubmission> findBySiteIdOrderByCreatedAtDesc(UUID siteId);

    List<FormSubmission> findBySiteIdAndFormNameOrderByCreatedAtDesc(UUID siteId, String formName);

    /** Jumlah isian per form: [nama form, jumlah]. Hanya angka, isi form tidak pernah tampil publik. */
    @Query("SELECT f.formName, COUNT(f) FROM FormSubmission f WHERE f.siteId = :siteId GROUP BY f.formName")
    List<Object[]> countByFormName(UUID siteId);
}
