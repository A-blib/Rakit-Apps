package com.aris.templateapp.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, UUID> {

    // Nama method dibaca Spring Data lalu diubah menjadi query, mis. "... where user.id = ? and provider = ?".
    Optional<UserIdentity> findByUserIdAndProvider(UUID userId, IdentityProvider provider);

    List<UserIdentity> findByUserId(UUID userId);
}
