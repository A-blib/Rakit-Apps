package com.aris.templateapp.auth;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AuthTicketRepository extends JpaRepository<AuthTicket, UUID> {

    // Dikunci agar satu tiket tidak bisa dipakai dua kali oleh dua request yang datang bersamaan.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AuthTicket> findByTokenHash(String tokenHash);

    @Modifying
    @Query("delete from AuthTicket t where t.expiresAt < :before")
    int deleteExpiredBefore(Instant before);
}
