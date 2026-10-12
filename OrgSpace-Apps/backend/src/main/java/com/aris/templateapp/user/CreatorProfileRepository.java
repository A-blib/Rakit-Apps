package com.aris.templateapp.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CreatorProfileRepository extends JpaRepository<CreatorProfile, UUID> {
}
