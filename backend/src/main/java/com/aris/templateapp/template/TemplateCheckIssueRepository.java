package com.aris.templateapp.template;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TemplateCheckIssueRepository extends JpaRepository<TemplateCheckIssue, UUID> {

    List<TemplateCheckIssue> findByCheckId(UUID checkId);

    // Urutan tetap (per kode aturan, lalu file & baris) agar daftar masalah tidak berpindah-pindah setiap kali dimuat ulang.
    List<TemplateCheckIssue> findByCheckIdOrderBySeverityAscCodeAscFileAscLineAsc(UUID checkId);
}
