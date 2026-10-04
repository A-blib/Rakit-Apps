package com.aris.templateapp.template;

import com.aris.templateapp.user.WebsitePurpose;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Tabel {@code templates}. Kategori memakai enum {@link WebsitePurpose} karena pilihannya sama dengan
 * tujuan website saat onboarding pembuat website.
 * <p>
 * {@code updatedAt} diatur sendiri (bukan @UpdateTimestamp) karena "terakhir diperbarui" berarti
 * template diubah provider, bukan setiap kali baris ini tersentuh (mis. jumlah peringatan dihitung ulang).
 */
@Entity
@Table(name = "templates")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID providerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private WebsitePurpose category;

    private String thumbnailUrl;

    @Column(nullable = false)
    private TemplateStatus status = TemplateStatus.DRAFT;

    @Column(nullable = false)
    private int warningCount;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private Instant publishedAt;

    public Template(UUID providerId, String name, WebsitePurpose category) {
        this.providerId = providerId;
        this.name = name;
        this.category = category;
    }

    @PrePersist
    void fillTimestamps() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }
}
