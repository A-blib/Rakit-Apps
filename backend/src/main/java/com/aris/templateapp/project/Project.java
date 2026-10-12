package com.aris.templateapp.project;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tabel {@code projects}: satu website milik ruang kerja (K6). Isi editor ({@code values.json}) disimpan di
 * {@code content}; HP hanya menyimpan salinannya (cache Room).
 */
@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID organizationId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, updatable = false)
    private ProjectMode mode;

    private UUID sourceTemplateId;

    private Integer templateVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> content = new HashMap<>();

    @Column(nullable = false)
    private int contentVersion;

    @Column(nullable = false)
    private Instant contentUpdatedAt;

    @Column(nullable = false)
    private int missingCount;

    private UUID thumbnailAssetId;

    private UUID createdBy;

    private UUID updatedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Diatur manual (bukan {@code @UpdateTimestamp}) agar hanya perubahan yang dilihat user (nama, isi) yang
     * menggeser "Diubah 2 jam lalu", bukan perubahan teknis seperti thumbnail.
     */
    @Column(nullable = false)
    private Instant updatedAt;

    public Project(UUID organizationId, String name, ProjectMode mode, UUID createdBy) {
        Instant now = Instant.now();
        this.organizationId = organizationId;
        this.name = name;
        this.mode = mode;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
        this.contentUpdatedAt = now;
        this.updatedAt = now;
    }

    /** Dicatat setiap user mengubah sesuatu yang terlihat di kartu website. */
    public void touch(UUID userId) {
        this.updatedBy = userId;
        this.updatedAt = Instant.now();
    }
}
