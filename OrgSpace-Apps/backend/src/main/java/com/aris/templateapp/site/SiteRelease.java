package com.aris.templateapp.site;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Rilis statis jadi hasil bangun website (alur-publish-website.md). */
@Entity
@Table(name = "site_releases")
@Getter
@Setter
@NoArgsConstructor
public class SiteRelease {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private UUID siteId;

    @Column(nullable = false)
    private int version;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> projectSnapshot;

    @Column(nullable = false, length = 20)
    private String kind; // publish, refresh, rollback

    private UUID publishedBy;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(nullable = false)
    private int fileCount;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public SiteRelease(UUID siteId, int version, Map<String, Object> projectSnapshot, String kind, UUID publishedBy) {
        this.siteId = siteId;
        this.version = version;
        this.projectSnapshot = projectSnapshot;
        this.kind = kind;
        this.publishedBy = publishedBy;
        this.createdAt = Instant.now();
    }
}
