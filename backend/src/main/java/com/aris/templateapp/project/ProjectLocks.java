package com.aris.templateapp.project;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Kunci edit (alur-buat-website-via-template.md 11.5, K17): satu website hanya diedit satu orang dalam satu waktu.
 * <ul>
 *   <li>Kunci milik sendiri boleh diperpanjang walau sudah kedaluwarsa, <b>selama belum diambil orang lain</b>
 *       (HP yang lama offline tetap bisa menyinkron perubahannya).</li>
 *   <li>Kunci orang lain yang sudah kedaluwarsa boleh diambil langsung.</li>
 *   <li>Kunci orang lain yang masih aktif hanya bisa diambil alih Admin/Pemilik ({@link #takeOver}).</li>
 * </ul>
 * Pemanggil wajib berada di dalam transaksi; baris kunci dikunci {@code FOR UPDATE} agar dua HP tidak sama-sama
 * mendapat kunci.
 */
@Component
@RequiredArgsConstructor
class ProjectLocks {

    /** Keadaan kunci saat ini. */
    record Holder(UUID userId, String name, Instant expiresAt) {
    }

    private final JdbcClient jdbc;
    private final AppProperties properties;
    private final Clock clock;

    Optional<Holder> current(UUID projectId, boolean forUpdate) {
        return jdbc.sql("""
                        SELECT l.user_id, u.display_name, l.expires_at
                          FROM project_locks l LEFT JOIN users u ON u.id = l.user_id
                         WHERE l.project_id = :id""" + (forUpdate ? " FOR UPDATE OF l" : ""))
                .param("id", projectId)
                .query((rs, row) -> new Holder(rs.getObject("user_id", UUID.class), rs.getString("display_name"),
                        rs.getTimestamp("expires_at").toInstant()))
                .optional();
    }

    /** Mengambil atau memperpanjang kunci. Kunci orang lain yang masih aktif → {@code PROJECT_LOCKED}. */
    Instant acquire(UUID projectId, UUID userId, String deviceId) {
        Instant now = clock.instant();
        Optional<Holder> holder = current(projectId, true);
        if (holder.isPresent() && !holder.get().userId().equals(userId) && holder.get().expiresAt().isAfter(now)) {
            throw locked(holder.get());
        }
        return write(projectId, userId, deviceId, now);
    }

    /** Admin/Pemilik mengambil alih kunci orang lain (pemegang lama mendapat LOCK_LOST saat menyinkron). */
    Instant takeOver(UUID projectId, UUID userId, String deviceId) {
        current(projectId, true);
        return write(projectId, userId, deviceId, clock.instant());
    }

    /** Menyimpan isi hanya boleh oleh pemegang kunci terakhir; kunci yang diambil orang lain → {@code LOCK_LOST}. */
    void requireHeldBy(UUID projectId, UUID userId) {
        Optional<Holder> holder = current(projectId, true);
        if (holder.isEmpty() || !holder.get().userId().equals(userId)) {
            Map<String, Object> details = new HashMap<>();
            holder.ifPresent(h -> details.put("holderName", h.name()));
            throw new ApiException(ErrorCode.LOCK_LOST).withDetails(details);
        }
    }

    void release(UUID projectId, UUID userId) {
        jdbc.sql("DELETE FROM project_locks WHERE project_id = :id AND user_id = :user")
                .param("id", projectId).param("user", userId)
                .update();
    }

    private Instant write(UUID projectId, UUID userId, String deviceId, Instant now) {
        Instant expires = now.plus(Duration.ofSeconds(properties.projects().lockTtlSeconds()));
        jdbc.sql("""
                        INSERT INTO project_locks (project_id, user_id, device_id, acquired_at, expires_at)
                        VALUES (:id, :user, :device, :now, :expires)
                        ON CONFLICT (project_id) DO UPDATE
                           SET acquired_at = CASE WHEN project_locks.user_id = EXCLUDED.user_id
                                                  THEN project_locks.acquired_at ELSE EXCLUDED.acquired_at END,
                               user_id = EXCLUDED.user_id, device_id = EXCLUDED.device_id,
                               expires_at = EXCLUDED.expires_at""")
                .param("id", projectId).param("user", userId).param("device", deviceId)
                .param("now", Timestamp.from(now)).param("expires", Timestamp.from(expires))
                .update();
        return expires;
    }

    private ApiException locked(Holder holder) {
        Map<String, Object> details = new HashMap<>();
        details.put("holderName", holder.name());
        // Kunci diperpanjang tiap 45 detik selama editor terbuka, jadi "terakhir aktif" ≈ perpanjangan terakhir.
        details.put("lastActiveAt", holder.expiresAt().minusSeconds(properties.projects().lockTtlSeconds()).toString());
        return new ApiException(ErrorCode.PROJECT_LOCKED,
                (holder.name() == null ? "Anggota lain" : holder.name()) + " sedang mengedit website ini.")
                .withDetails(details);
    }
}
