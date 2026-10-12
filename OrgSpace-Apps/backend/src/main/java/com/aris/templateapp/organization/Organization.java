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
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Tabel {@code organizations}: ruang kerja tempat website dibuat. Kolom profil (alamat, kontak, media sosial)
 * kelak mengisi otomatis kolom template yang dihubungkan kreator (K10).
 */
@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private OrganizationType type;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 40)
    private String shortName;

    private OrgCategory category;

    private OrgLevel level;

    @Column(length = 150)
    private String institutionName;

    @Column(length = 100)
    private String city;

    private Short periodStartYear;

    private Short periodEndYear;

    private String logoUrl;

    private String address;

    private String email;

    @Column(length = 20)
    private String whatsapp;

    @Column(length = 100)
    private String instagram;

    @Column(length = 100)
    private String tiktok;

    @Column(length = 100)
    private String youtube;

    @Column(nullable = false, updatable = false)
    private UUID createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public Organization(OrganizationType type, String name, UUID createdBy) {
        this.type = type;
        this.name = name;
        this.createdBy = createdBy;
    }

    public boolean isPersonal() {
        return type == OrganizationType.PERSONAL;
    }

    /** Nama untuk kalimat sapaan ({org}): nama singkat jika ada. */
    public String displayShortName() {
        return shortName != null ? shortName : name;
    }
}
