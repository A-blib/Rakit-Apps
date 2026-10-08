package com.aris.templateapp.template;

import com.aris.templateapp.upload.check.TechInfo;
import com.aris.templateapp.upload.marking.MarkingData;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

    // Null selama Info template (langkah 3 Upload) belum diisi.
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

    // ---------- Upload (alur-fitur-upload.md) ----------

    @Column(length = 300)
    private String description;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false, columnDefinition = "text[]")
    private List<String> keywords = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private TechInfo techInfo;

    private String sourceFileName;

    private Long sourceSize;

    // Langkah wizard terakhir (1–6), agar draft dilanjutkan tepat di langkah itu.
    @Column(nullable = false)
    private int wizardStep = 1;

    private Instant expiryNotifiedAt;

    // auto · section · custom, dan tampilan asal thumbnail (mobile · desktop), bagian 6.2.
    @Column(length = 10)
    private String thumbnailSource;

    @Column(length = 10)
    private String thumbnailView;

    // Tandaan provider (langkah 4) dan jumlah isiannya.
    @JdbcTypeCode(SqlTypes.JSON)
    private MarkingData marking;

    @Column(nullable = false)
    private int markingFieldCount;

    public Template(UUID providerId, String name, WebsitePurpose category) {
        this.providerId = providerId;
        this.name = name;
        this.category = category;
    }

    /**
     * Draft disentuh provider: "terakhir diperbarui" maju dan hitungan 30 hari draft dimulai ulang,
     * sehingga pemberitahuan "akan dihapus" perlu dikirim lagi nanti.
     */
    public void touch(Instant now) {
        updatedAt = now;
        expiryNotifiedAt = null;
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
