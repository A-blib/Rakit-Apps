package com.aris.templateapp.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/** Tabel {@code users}: satu akun, apa pun metode login yang dipakai. */
@Entity
@Table(name = "users")
@Getter
@Setter
// Konstruktor kosong hanya untuk Hibernate; kode lain memakai konstruktor di bawah.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String displayName;

    private String email;

    private String avatarUrl;

    @Column(name = "is_admin", nullable = false)
    private boolean admin;

    @Column(nullable = false)
    private ActiveMode activeMode = ActiveMode.CREATOR;

    @Column(nullable = false)
    private boolean onboardingCompleted;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public User(String displayName, String email) {
        this.displayName = displayName;
        this.email = email;
    }
}
