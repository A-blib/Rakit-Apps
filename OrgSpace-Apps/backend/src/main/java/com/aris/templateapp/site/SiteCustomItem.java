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

/**
 * Item konten modul koleksi kustom dinamis untuk website yang tayang
 * (misalnya koleksi prestasi, agenda, kegiatan, testimoni, donatur, dsb.).
 */
@Entity
@Table(name = "site_custom_items")
@Getter
@Setter
@NoArgsConstructor
public class SiteCustomItem {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private UUID siteId;

    @Column(nullable = false, length = 60)
    private String collectionName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> data;

    @Column(nullable = false)
    private int sortOrder = 0;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public SiteCustomItem(UUID siteId, String collectionName, Map<String, Object> data, int sortOrder) {
        this.siteId = siteId;
        this.collectionName = collectionName;
        this.data = data;
        this.sortOrder = sortOrder;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
