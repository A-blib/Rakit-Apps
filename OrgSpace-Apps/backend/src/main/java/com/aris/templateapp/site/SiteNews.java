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

/** Berita dan kegiatan untuk modul dinamis website organisasi (data-ws-list="berita"). */
@Entity
@Table(name = "site_news")
@Getter
@Setter
@NoArgsConstructor
public class SiteNews {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private UUID organizationId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String summary;

    private String content;

    private String imageUrl;

    @Column(nullable = false)
    private Instant publishedAt = Instant.now();

    private UUID createdBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public SiteNews(UUID organizationId, String title, String summary, String content, String imageUrl, UUID createdBy) {
        this.organizationId = organizationId;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.imageUrl = imageUrl;
        this.createdBy = createdBy;
        this.publishedAt = Instant.now();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
