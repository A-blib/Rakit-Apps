package com.aris.templateapp.site;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/** Website yang punya alamat publik (alur-publish-website.md). */
@Entity
@Table(name = "sites")
@Getter
@Setter
@NoArgsConstructor
public class Site {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false, unique = true)
    private UUID projectId;

    @Column(nullable = false)
    private UUID organizationId;

    @Column(nullable = false, unique = true, length = 40)
    private String slug;

    @Column(nullable = false, length = 20)
    private String status = "online"; // online, offline, suspended

    private UUID currentReleaseId;

    private String suspensionReason;

    @Column(length = 70)
    private String seoTitle;

    @Column(length = 160)
    private String seoDescription;

    private UUID faviconAssetId;

    private UUID shareImageAssetId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    private Instant publishedAt;

    public Site(UUID projectId, UUID organizationId, String slug) {
        this.projectId = projectId;
        this.organizationId = organizationId;
        this.slug = slug.toLowerCase();
        this.status = "online";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
