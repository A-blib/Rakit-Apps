package com.aris.templateapp.organization;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.organization.dto.PortalCredentialRequest;
import com.aris.templateapp.organization.dto.PortalCredentialResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Kredensial Portal Pengurus per organisasi: satu username + password untuk tiap peran, berlaku di Portal semua
 * website organisasi itu. Hanya Pemilik organisasi yang boleh melihat username dan mengaturnya.
 * <p>
 * Login Portal tidak memakai akun OrgSpace. Peran dikenali dari username ("kartu nama"), lalu password hanya
 * dicocokkan dengan hash peran itu: username Editor + password Admin ditolak walau password Admin benar.
 */
@Service
public class PortalCredentialService {

    private final JdbcClient jdbc;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationAccess access;
    private final ActivityService activityService;
    private final Clock clock;
    /** Dicocokkan saat username tidak ada, agar lama respons tidak membocorkan username mana yang terdaftar. */
    private final String dummyPasswordHash;

    public PortalCredentialService(JdbcClient jdbc, PasswordEncoder passwordEncoder, OrganizationAccess access,
                                   ActivityService activityService, Clock clock) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.access = access;
        this.activityService = activityService;
        this.clock = clock;
        this.dummyPasswordHash = passwordEncoder.encode("password-palsu-untuk-penyeimbang-waktu");
    }

    /** Hasil login Portal yang cocok: peran dan versi kredensial yang ditanam di token. */
    public record Match(MemberRole role, String username, int version) {
    }

    @Transactional(readOnly = true)
    public List<PortalCredentialResponse> list(UUID organizationId, UUID userId) {
        access.require(organizationId, userId, OrgAction.MANAGE_PORTAL_CREDENTIALS);
        // Diatur sebelum serah terima terakhir = password masih diketahui pengurus lama, jadi perlu diganti.
        return jdbc.sql("""
                        SELECT c.role, c.username, c.updated_at,
                               c.updated_at < (SELECT max(a.created_at) FROM activity_logs a
                                                WHERE a.organization_id = c.organization_id
                                                  AND a.action = 'org.ownership_transferred') AS change_recommended
                          FROM portal_credentials c WHERE c.organization_id = :org
                         ORDER BY CASE c.role WHEN 'owner' THEN 0 WHEN 'admin' THEN 1 ELSE 2 END""")
                .param("org", organizationId)
                .query((rs, row) -> new PortalCredentialResponse(role(rs.getString("role")), rs.getString("username"),
                        rs.getTimestamp("updated_at").toInstant(), rs.getBoolean("change_recommended")))
                .list();
    }

    /** Membuat atau mengganti kredensial satu peran. Setiap simpan menaikkan versi: semua sesi Portal peran itu keluar. */
    @Transactional
    public PortalCredentialResponse save(UUID organizationId, UUID userId, MemberRole role, PortalCredentialRequest request) {
        access.require(organizationId, userId, OrgAction.MANAGE_PORTAL_CREDENTIALS);
        String username = normalize(request.username());
        boolean usedByOtherRole = jdbc.sql("""
                        SELECT count(*) > 0 FROM portal_credentials
                         WHERE organization_id = :org AND username = :username AND role <> :role""")
                .param("org", organizationId).param("username", username).param("role", role.value())
                .query(Boolean.class).single();
        if (usedByOtherRole) {
            throw new ApiException(ErrorCode.PORTAL_USERNAME_TAKEN);
        }
        Instant now = clock.instant();
        try {
            jdbc.sql("""
                            INSERT INTO portal_credentials (organization_id, role, username, password_hash, created_at, updated_at)
                            VALUES (:org, :role, :username, :hash, :now, :now)
                            ON CONFLICT (organization_id, role) DO UPDATE
                               SET username = EXCLUDED.username, password_hash = EXCLUDED.password_hash,
                                   version = portal_credentials.version + 1, updated_at = EXCLUDED.updated_at""")
                    .param("org", organizationId).param("role", role.value()).param("username", username)
                    .param("hash", passwordEncoder.encode(request.password())).param("now", Timestamp.from(now))
                    .update();
        } catch (DuplicateKeyException e) {
            // Dua simpan bersamaan memakai username yang sama untuk peran berbeda.
            throw new ApiException(ErrorCode.PORTAL_USERNAME_TAKEN);
        }
        activityService.log(organizationId, userId, "portal_credential.saved", null, null, Map.of("role", role.value()));
        return new PortalCredentialResponse(role, username, now, false);
    }

    /**
     * Serah terima: kredensial Pemilik dihapus karena masih diketahui Pemilik lama, sehingga semua sesi Pemilik di
     * Portal langsung keluar dan Pemilik baru wajib membuat yang baru. Admin/Editor tetap jalan (ditandai perlu diganti).
     */
    void removeOwnerAfterHandover(UUID organizationId) {
        jdbc.sql("DELETE FROM portal_credentials WHERE organization_id = :org AND role = 'owner'")
                .param("org", organizationId).update();
    }

    /** Dipakai aturan undangan: peran Admin/Editor hanya bisa diundang setelah kredensial Portal-nya diatur. */
    @Transactional(readOnly = true)
    public boolean exists(UUID organizationId, MemberRole role) {
        return current(organizationId, role).isPresent();
    }

    @Transactional(readOnly = true)
    public boolean anySet(UUID organizationId) {
        return jdbc.sql("SELECT count(*) > 0 FROM portal_credentials WHERE organization_id = :org")
                .param("org", organizationId).query(Boolean.class).single();
    }

    /** Kosong jika username tidak dikenal di organisasi ini atau password bukan milik peran username itu. */
    @Transactional(readOnly = true)
    public Optional<Match> verify(UUID organizationId, String username, String password) {
        String normalized = normalize(username);
        Optional<Map<String, Object>> row = jdbc.sql("""
                        SELECT role, password_hash, version FROM portal_credentials
                         WHERE organization_id = :org AND username = :username""")
                .param("org", organizationId).param("username", normalized)
                .query().listOfRows().stream().findFirst();
        if (row.isEmpty()) {
            passwordEncoder.matches(password, dummyPasswordHash);
            return Optional.empty();
        }
        if (!passwordEncoder.matches(password, (String) row.get().get("password_hash"))) {
            return Optional.empty();
        }
        return Optional.of(new Match(role((String) row.get().get("role")), normalized, (Integer) row.get().get("version")));
    }

    /** Username dan versi kredensial peran saat ini; dipakai untuk memeriksa token Portal di setiap request. */
    @Transactional(readOnly = true)
    public Optional<Match> current(UUID organizationId, MemberRole role) {
        return jdbc.sql("SELECT username, version FROM portal_credentials WHERE organization_id = :org AND role = :role")
                .param("org", organizationId).param("role", role.value())
                .query((rs, row) -> new Match(role, rs.getString("username"), rs.getInt("version")))
                .optional();
    }

    private static String normalize(String username) {
        return username == null ? "" : username.strip().toLowerCase(Locale.ROOT);
    }

    private static MemberRole role(String value) {
        return PersistableEnum.fromValue(MemberRole.class, value);
    }
}
