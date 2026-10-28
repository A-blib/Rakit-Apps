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
 * Tabel {@code organization_invitations}. Satu kode untuk satu orang: berlaku sampai dipakai, kedaluwarsa (1 jam),
 * atau dicabut (keputusan Aris, 28 Oktober 2026; sebelumnya F2: beberapa orang selama 7 hari).
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

    private Instant usedAt;

    private UUID usedBy;

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
        return revokedAt == null && usedAt == null && expiresAt.isAfter(now);
    }

    public void markUsed(UUID userId, Instant now) {
        usedBy = userId;
        usedAt = now;
    }

    public void revoke(Instant now) {
        revokedAt = now;
    }
}
