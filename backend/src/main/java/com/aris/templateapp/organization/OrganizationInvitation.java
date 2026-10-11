package com.aris.templateapp.organization;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Tabel {@code organization_invitations}. Satu kode bisa dipakai beberapa orang sampai kedaluwarsa atau
 * dicabut (F2), jadi tidak ada kolom "sudah dipakai".
 */
@Entity
@Table(name = "organization_invitations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrganizationInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID organizationId;

    @Column(nullable = false, length = 64, updatable = false)
    private String codeHash;

    @Column(nullable = false, updatable = false)
    private MemberRole role;

    @Column(nullable = false, updatable = false)
    private UUID createdBy;

    @Column(nullable = false, updatable = false)
    private Instant expiresAt;

    private Instant revokedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public OrganizationInvitation(UUID organizationId, String codeHash, MemberRole role, UUID createdBy, Instant expiresAt) {
        this.organizationId = organizationId;
        this.codeHash = codeHash;
        this.role = role;
        this.createdBy = createdBy;
        this.expiresAt = expiresAt;
    }

    public boolean isUsable(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }

    public void revoke(Instant now) {
        revokedAt = now;
    }
}
