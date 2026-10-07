package com.aris.templateapp.template;

import com.aris.templateapp.upload.check.CheckStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface TemplateCheckRepository extends JpaRepository<TemplateCheck, UUID> {

    /** Pengecekan terakhir = versi tertinggi. */
    Optional<TemplateCheck> findFirstByTemplateIdOrderByVersionDesc(UUID templateId);

    /** Mencatat tahap yang sedang berjalan tanpa memuat entity (dipanggil berkali-kali selama pengecekan). */
    @Modifying
    @Query("update TemplateCheck c set c.stage = :stage where c.id = :id")
    void updateStage(UUID id, CheckStage stage);
}
