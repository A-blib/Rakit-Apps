package com.aris.templateapp.auth;

import com.aris.templateapp.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/** Tabel {@code refresh_tokens}. Yang disimpan hanya hash token, bukan token aslinya. */
@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 64)
    private String tokenHash;

    @Column(length = 100)
    private String deviceName;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant revokedAt;

    private UUID replacedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public RefreshToken(User user, String tokenHash, String deviceName, Instant expiresAt) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.deviceName = deviceName;
        this.expiresAt = expiresAt;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public void revoke(Instant now) {
        if (revokedAt == null) {
            revokedAt = now;
        }
    }

    /** Dicabut karena sudah ditukar dengan token baru (bukan karena logout). */
    public void rotateTo(UUID newTokenId, Instant now) {
        revoke(now);
        replacedBy = newTokenId;
    }
}
