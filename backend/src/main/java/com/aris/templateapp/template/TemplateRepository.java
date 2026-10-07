package com.aris.templateapp.template;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemplateRepository extends JpaRepository<Template, UUID> {

    Optional<Template> findByIdAndProviderId(UUID id, UUID providerId);

    boolean existsByProviderId(UUID providerId);

    long countByProviderIdAndStatusIn(UUID providerId, Collection<TemplateStatus> statuses);

    List<Template> findByProviderIdAndStatusOrderByUpdatedAtDesc(UUID providerId, TemplateStatus status);

    List<Template> findByStatusAndUpdatedAtBefore(TemplateStatus status, Instant before);

    boolean existsByProviderIdAndNameIgnoreCaseAndIdNot(UUID providerId, String name, UUID id);
}
