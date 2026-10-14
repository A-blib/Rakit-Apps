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

/** Antrean bangun website (alur-publish-website.md 7.2 & 7.6). */
@Entity
@Table(name = "site_builds")
@Getter
@Setter
@NoArgsConstructor
public class SiteBuild {

    @Id
    private UUID id = UUID.randomUUID();

    private UUID siteId;

    @Column(nullable = false)
    private UUID projectId;

    @Column(nullable = false, length = 20)
    private String kind; // publish, refresh, export

    @Column(nullable = false, length = 20)
    private String status; // queued, running, succeeded, failed

    private UUID requestedBy;

    private Instant notBefore;

    private Instant firstRequestedAt;

    private UUID releaseId;

    private String exportPath;

    private String exportTokenHash;

    private Instant exportExpiresAt;

    private String errorCode;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> errorDetail;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant startedAt;

    private Instant finishedAt;

    public SiteBuild(UUID projectId, UUID siteId, String kind, UUID requestedBy) {
        this.projectId = projectId;
        this.siteId = siteId;
        this.kind = kind;
        this.status = "queued";
        this.requestedBy = requestedBy;
        this.firstRequestedAt = Instant.now();
        this.createdAt = Instant.now();
        this.notBefore = Instant.now();
    }
}
