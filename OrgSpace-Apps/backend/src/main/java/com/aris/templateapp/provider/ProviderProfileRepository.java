package com.aris.templateapp.provider;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProviderProfileRepository extends JpaRepository<ProviderProfile, UUID> {
}
