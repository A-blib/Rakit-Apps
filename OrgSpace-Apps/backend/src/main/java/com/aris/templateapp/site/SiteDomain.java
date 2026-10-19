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

/**
 * Entitas domain kustom milik website (alur-publish-website.md 7.2 & 7.10).
 */
@Entity
@Table(name = "site_domains")
@Getter
@Setter
@NoArgsConstructor
public class SiteDomain {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private UUID siteId;

    @Column(nullable = false, unique = true, length = 253)
    private String hostname;

    @Column(nullable = false, length = 20)
    private String status = "pending"; // pending, active, failed, removed

    @Column(nullable = false, length = 64)
    private String verificationToken;

    private Instant lastCheckedAt;

    private Instant activatedAt;

    private Instant failingSince;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public SiteDomain(UUID siteId, String hostname, String verificationToken) {
        this.siteId = siteId;
        this.hostname = hostname.toLowerCase();
        this.verificationToken = verificationToken;
        this.status = "pending";
        this.createdAt = Instant.now();
    }
}
