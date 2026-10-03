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
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/** Tabel {@code user_identities}: satu cara masuk (email, Google, atau GitHub) milik seorang user. */
@Entity
@Table(name = "user_identities")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // LAZY: data user baru diambil dari database jika memang dipakai.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private IdentityProvider provider;

    @Column(nullable = false)
    private String providerUserId;

    private String email;

    @Column(nullable = false)
    private boolean emailVerified;

    @Column(length = 100)
    private String passwordHash;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /** Identitas email + password. {@code providerUserId} untuk {@code local} adalah email huruf kecil. */
    public static UserIdentity local(User user, String email, String passwordHash) {
        UserIdentity identity = new UserIdentity();
        identity.user = user;
        identity.provider = IdentityProvider.LOCAL;
        identity.providerUserId = email;
        identity.email = email;
        identity.passwordHash = passwordHash;
        return identity;
    }
}
