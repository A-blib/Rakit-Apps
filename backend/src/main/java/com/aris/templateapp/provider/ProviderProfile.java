package com.aris.templateapp.provider;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Tabel {@code provider_profiles}: ada jika user sudah mendaftar sebagai penyedia template. */
@Entity
@Table(name = "provider_profiles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProviderProfile {

    @Id
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String creatorName;

    @Column(length = 300)
    private String bio;

    private String portfolioUrl;

    // Kolom text[] PostgreSQL dipetakan langsung ke List<String>.
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false, columnDefinition = "text[]")
    private List<String> specialties = new ArrayList<>();

    @Column(nullable = false)
    private ProviderStatus status = ProviderStatus.PENDING;

    @Column(length = 300)
    private String rejectionReason;

    @Column(nullable = false)
    private Instant agreedTermsAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
