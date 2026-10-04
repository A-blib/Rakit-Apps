package com.aris.templateapp.template;

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

import java.time.Instant;
import java.util.UUID;

/** Tabel {@code template_checks}: satu kali pengecekan otomatis untuk satu versi template. */
@Entity
@Table(name = "template_checks")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TemplateCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID templateId;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false)
    private CheckStatus status;

    private Instant finishedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public TemplateCheck(UUID templateId, int version, CheckStatus status, Instant finishedAt) {
        this.templateId = templateId;
        this.version = version;
        this.status = status;
        this.finishedAt = finishedAt;
    }
}
