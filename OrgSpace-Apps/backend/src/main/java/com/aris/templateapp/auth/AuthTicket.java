package com.aris.templateapp.auth;

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

/** Tabel {@code auth_tickets}: tiket sekali pakai berumur pendek. Yang disimpan hanya hash tiketnya. */
@Entity
@Table(name = "auth_tickets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private AuthTicketType type;

    @Column(nullable = false, length = 64)
    private String tokenHash;

    // Disimpan sebagai UUID saja (tanpa relasi @ManyToOne) karena tiket hanya butuh ID-nya.
    private UUID userId;

    // Kolom jsonb dipetakan ke Map: isi tiap jenis tiket berbeda, jadi tidak dibuat kolom satu per satu.
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> payload = new HashMap<>();

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant usedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public AuthTicket(AuthTicketType type, String tokenHash, UUID userId, Map<String, Object> payload,
                      Instant expiresAt) {
        this.type = type;
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.payload = payload == null ? new HashMap<>() : new HashMap<>(payload);
        this.expiresAt = expiresAt;
    }

    public boolean isUsable(Instant now) {
        return usedAt == null && expiresAt.isAfter(now);
    }

    public void markUsed(Instant now) {
        usedAt = now;
    }

    public String payloadString(String key) {
        Object value = payload.get(key);
        return value == null ? null : value.toString();
    }
}
