package com.aris.templateapp.upload;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Tabel {@code check_reports}: laporan "Ini keliru? Laporkan" (alur-fitur-upload.md bagian 5.9).
 * Belum ada panel admin, jadi laporan dibaca langsung dari database; status diubah manual lewat SQL.
 */
@Entity
@Table(name = "check_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CheckReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID templateId;

    @Column(nullable = false)
    private UUID checkId;

    @Column(nullable = false, unique = true)
    private UUID issueId;

    @Column(nullable = false, length = 50)
    private String ruleCode;

    @Column(nullable = false)
    private int ruleVersion;

    @Column(length = 300)
    private String location;

    @Column(length = 300)
    private String snippet;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false)
    private UUID providerId;

    // Nilai: baru · dibaca · aturan_diperbaiki · tidak_berubah (CHECK constraint V12).
    @Column(nullable = false, length = 20)
    private String status = "baru";

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public CheckReport(UUID templateId, UUID checkId, UUID issueId, String ruleCode, int ruleVersion, String location,
                       String snippet, String reason, UUID providerId) {
        this.templateId = templateId;
        this.checkId = checkId;
        this.issueId = issueId;
        this.ruleCode = ruleCode;
        this.ruleVersion = ruleVersion;
        this.location = location;
        this.snippet = snippet;
        this.reason = reason;
        this.providerId = providerId;
    }
}
