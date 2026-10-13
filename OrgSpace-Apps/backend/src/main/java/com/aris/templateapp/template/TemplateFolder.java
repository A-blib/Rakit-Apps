package com.aris.templateapp.template;

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

import java.time.Instant;
import java.util.UUID;

/**
 * Tabel {@code template_folders}: folder buatan provider untuk mengelompokkan "Template Anda" (ala Google Drive).
 * Isi folder disimpan di kolom {@code templates.folder_id}, bukan di sini, sehingga satu template paling banyak
 * berada di satu folder.
 */
@Entity
@Table(name = "template_folders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TemplateFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID providerId;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public TemplateFolder(UUID providerId, String name) {
        this.providerId = providerId;
        this.name = name;
    }

    public void rename(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }

    @PrePersist
    void fillTimestamps() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }
}
