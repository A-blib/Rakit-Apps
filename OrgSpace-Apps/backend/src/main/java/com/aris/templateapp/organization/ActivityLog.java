package com.aris.templateapp.organization;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Tabel {@code activity_logs}: "siapa melakukan apa" di satu ruang kerja (Riwayat aktivitas). */
@Entity
@Table(name = "activity_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID organizationId;

    private UUID actorUserId;

    @Column(nullable = false, length = 50, updatable = false)
    private String action;

    @Column(length = 30)
    private String targetType;

    private UUID targetId;

    // Isi berbeda per jenis aksi (mis. peran lama & baru), jadi disimpan sebagai jsonb.
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata = new HashMap<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public ActivityLog(UUID organizationId, UUID actorUserId, String action, String targetType, UUID targetId,
                       Map<String, Object> metadata) {
        this.organizationId = organizationId;
        this.actorUserId = actorUserId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.metadata = metadata == null ? new HashMap<>() : new HashMap<>(metadata);
    }
}
