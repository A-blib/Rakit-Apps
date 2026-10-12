package com.aris.templateapp.organization;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/** Tabel {@code organization_members}: satu baris = satu user di satu ruang kerja beserta perannya. */
@Entity
@Table(name = "organization_members")
// Primary key gabungan (organization_id, user_id), dipetakan lewat class Key di bawah.
@IdClass(OrganizationMember.Key.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrganizationMember {

    @Id
    private UUID organizationId;

    @Id
    private UUID userId;

    @Column(nullable = false)
    private MemberRole role;

    @Column(length = 60)
    private String positionTitle;

    @Column(nullable = false, updatable = false)
    private Instant joinedAt;

    public OrganizationMember(UUID organizationId, UUID userId, MemberRole role, String positionTitle, Instant joinedAt) {
        this.organizationId = organizationId;
        this.userId = userId;
        this.role = role;
        this.positionTitle = positionTitle;
        this.joinedAt = joinedAt;
    }

    public record Key(UUID organizationId, UUID userId) implements Serializable {
        // Hibernate butuh konstruktor tanpa argumen untuk class @IdClass.
        public Key() {
            this(null, null);
        }
    }
}
