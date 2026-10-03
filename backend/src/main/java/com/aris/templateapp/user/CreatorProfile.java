package com.aris.templateapp.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/** Tabel {@code creator_profiles}: ada jika user sudah punya mode pembuat website. */
@Entity
@Table(name = "creator_profiles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreatorProfile {

    // Primary key sekaligus ID user, karena satu user maksimal satu profil.
    @Id
    private UUID userId;

    private WebsitePurpose websitePurpose;

    @Column(length = 150)
    private String organizationName;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public CreatorProfile(UUID userId) {
        this.userId = userId;
    }
}
