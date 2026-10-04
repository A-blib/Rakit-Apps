package com.aris.templateapp.template;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TemplateRepository extends JpaRepository<Template, UUID> {

    Optional<Template> findByIdAndProviderId(UUID id, UUID providerId);

    boolean existsByProviderId(UUID providerId);
}
