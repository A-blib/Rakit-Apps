package com.aris.templateapp.template;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TemplateFolderRepository extends JpaRepository<TemplateFolder, UUID> {

    Optional<TemplateFolder> findByIdAndProviderId(UUID id, UUID providerId);

    long countByProviderId(UUID providerId);

    boolean existsByProviderIdAndNameIgnoreCase(UUID providerId, String name);

    boolean existsByProviderIdAndNameIgnoreCaseAndIdNot(UUID providerId, String name, UUID id);
}
