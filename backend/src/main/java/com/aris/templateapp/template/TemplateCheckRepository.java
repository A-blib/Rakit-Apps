package com.aris.templateapp.template;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TemplateCheckRepository extends JpaRepository<TemplateCheck, UUID> {

    /** Pengecekan terakhir = versi tertinggi. */
    Optional<TemplateCheck> findFirstByTemplateIdOrderByVersionDesc(UUID templateId);
}
