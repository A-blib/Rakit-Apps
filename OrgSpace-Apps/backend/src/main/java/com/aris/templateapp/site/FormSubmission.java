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

/** Kiriman formulir dari pengunjung website (modul dinamis formulir). */
@Entity
@Table(name = "form_submissions")
@Getter
@Setter
@NoArgsConstructor
public class FormSubmission {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private UUID siteId;

    @Column(nullable = false, length = 60)
    private String formName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    private Map<String, Object> data;

    @Column(length = 45)
    private String ipAddress;

    private String userAgent;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public FormSubmission(UUID siteId, String formName, Map<String, Object> data, String ipAddress, String userAgent) {
        this.siteId = siteId;
        this.formName = formName;
        this.data = data;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = Instant.now();
    }
}
