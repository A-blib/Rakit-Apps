package com.aris.templateapp.user;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // Ditulis manual agar memakai lower(email), sama dengan index unik di V1.
    @Query("select u from User u where lower(u.email) = lower(:email)")
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Mengambil user sambil mengunci barisnya sampai transaksi selesai. Dipakai saat aturan bergantung
     * pada data lain milik user (mis. "jangan hapus identitas terakhir"), agar dua request bersamaan
     * tidak sama-sama lolos pengecekan.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(UUID id);
}
