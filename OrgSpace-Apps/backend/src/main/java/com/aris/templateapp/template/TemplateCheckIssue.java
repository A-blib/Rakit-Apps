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

import java.util.UUID;

/**
 * Tabel {@code template_check_issues}. Setiap masalah wajib punya pesan jelas, letak (file & baris bila ada),
 * dan saran cara memperbaiki (bagian 4.2).
 */
@Entity
@Table(name = "template_check_issues")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TemplateCheckIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID checkId;

    @Column(nullable = false)
    private IssueSeverity severity;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 300)
    private String message;

    private String file;

    private Integer line;

    @Column(length = 300)
    private String suggestion;

    // Versi aturan saat dicek (bagian 5.7) dan potongan kode yang tertangkap, untuk laporan "Ini keliru?".
    @Column(nullable = false)
    private int ruleVersion = 1;

    @Column(length = 300)
    private String snippet;

    public TemplateCheckIssue(UUID checkId, IssueSeverity severity, String code, String message, String file,
                              Integer line, String suggestion) {
        this(checkId, severity, code, 1, message, file, line, suggestion, null);
    }

    public TemplateCheckIssue(UUID checkId, IssueSeverity severity, String code, int ruleVersion, String message,
                              String file, Integer line, String suggestion, String snippet) {
        this.checkId = checkId;
        this.severity = severity;
        this.code = code;
        this.ruleVersion = ruleVersion;
        this.message = message;
        this.file = file;
        this.line = line;
        this.suggestion = suggestion;
        this.snippet = snippet;
    }
}
