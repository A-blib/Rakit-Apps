package com.aris.templateapp.monitoring;

import com.aris.templateapp.config.AppProperties;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Menghitung lalu lintas API dan pengguna aktif di memori, lalu menulisnya ke database secara berkala
 * ({@code app.portal.usage-flush-seconds}). Satu INSERT per request akan membebani database; dengan cara ini satu
 * endpoint yang dipanggil ribuan kali per menit tetap hanya menjadi satu UPSERT.
 * <p>
 * Penambahan dan pengambilan memakai {@link ConcurrentHashMap#compute}/{@code remove} per kunci, jadi tidak ada hitungan
 * yang hilang di antara keduanya. Jika database gagal, hitungan dikembalikan ke memori dan dicoba lagi berikutnya.
 * Hitungan yang belum ditulis saat backend mati mendadak (bukan shutdown normal) memang hilang: ini data statistik,
 * bukan data transaksi.
 */
@Slf4j
@Component
public class UsageRecorder {

    record TrafficKey(LocalDate day, String method, String route) {
    }

    static final class Traffic {
        long requests;
        long errors;
        long clientErrors;
        long totalMs;
        int maxMs;

        void add(Traffic other) {
            requests += other.requests;
            errors += other.errors;
            clientErrors += other.clientErrors;
            totalMs += other.totalMs;
            maxMs = Math.max(maxMs, other.maxMs);
        }
    }

    record ActivityKey(UUID userId, LocalDate day, ClientPlatform platform) {
    }

    static final class Activity {
        int requests;
        Instant first;
        Instant last;

        void add(Activity other) {
            requests += other.requests;
            first = first == null || other.first.isBefore(first) ? other.first : first;
            last = last == null || other.last.isAfter(last) ? other.last : last;
        }
    }

    private final JdbcClient jdbc;
    private final Clock clock;
    private final ZoneId zone;
    private final Map<TrafficKey, Traffic> traffic = new ConcurrentHashMap<>();
    private final Map<ActivityKey, Activity> activity = new ConcurrentHashMap<>();

    public UsageRecorder(JdbcClient jdbc, Clock clock, AppProperties properties) {
        this.jdbc = jdbc;
        this.clock = clock;
        this.zone = ZoneId.of(properties.timezone());
    }

    /**
     * @param route  pola endpoint (mis. {@code /api/projects/{id}}), bukan path asli yang berisi ID
     * @param userId null untuk tamu / token tidak sah
     */
    public void record(String method, String route, int status, long durationMs, UUID userId, ClientPlatform platform) {
        Instant now = clock.instant();
        LocalDate day = LocalDate.ofInstant(now, zone);
        int ms = (int) Math.min(Integer.MAX_VALUE, Math.max(0, durationMs));
        traffic.compute(new TrafficKey(day, method, route), (key, value) -> {
            Traffic t = value == null ? new Traffic() : value;
            t.requests++;
            if (status >= 500) {
                t.errors++;
            } else if (status >= 400) {
                t.clientErrors++;
            }
            t.totalMs += ms;
            t.maxMs = Math.max(t.maxMs, ms);
            return t;
        });
        if (userId != null) {
            activity.compute(new ActivityKey(userId, day, platform), (key, value) -> {
                Activity a = value == null ? new Activity() : value;
                a.requests++;
                a.first = a.first == null ? now : a.first;
                a.last = now;
                return a;
            });
        }
    }

    @Scheduled(fixedDelayString = "${app.portal.usage-flush-seconds}", initialDelayString = "${app.portal.usage-flush-seconds}",
            timeUnit = TimeUnit.SECONDS)
    public void flush() {
        flushTraffic();
        flushActivity();
    }

    /** Shutdown normal (Ctrl+C, deploy ulang): hitungan terakhir tetap tersimpan. */
    @PreDestroy
    void flushOnShutdown() {
        flush();
    }

    private void flushTraffic() {
        List<Map.Entry<TrafficKey, Traffic>> taken = take(traffic);
        for (Map.Entry<TrafficKey, Traffic> entry : taken) {
            TrafficKey key = entry.getKey();
            Traffic t = entry.getValue();
            try {
                jdbc.sql("""
                                INSERT INTO api_traffic_daily (day, method, route, request_count, error_count,
                                                               client_error_count, total_ms, max_ms)
                                VALUES (:day, :method, :route, :requests, :errors, :clientErrors, :totalMs, :maxMs)
                                ON CONFLICT (day, method, route) DO UPDATE SET
                                    request_count = api_traffic_daily.request_count + EXCLUDED.request_count,
                                    error_count = api_traffic_daily.error_count + EXCLUDED.error_count,
                                    client_error_count = api_traffic_daily.client_error_count + EXCLUDED.client_error_count,
                                    total_ms = api_traffic_daily.total_ms + EXCLUDED.total_ms,
                                    max_ms = GREATEST(api_traffic_daily.max_ms, EXCLUDED.max_ms)
                                """)
                        .param("day", Date.valueOf(key.day())).param("method", key.method()).param("route", key.route())
                        .param("requests", t.requests).param("errors", t.errors).param("clientErrors", t.clientErrors)
                        .param("totalMs", t.totalMs).param("maxMs", t.maxMs)
                        .update();
            } catch (DataAccessException e) {
                log.warn("Statistik lalu lintas belum tersimpan, dicoba lagi nanti: {}", e.getMessage());
                traffic.merge(key, t, (current, failed) -> {
                    current.add(failed);
                    return current;
                });
            }
        }
    }

    private void flushActivity() {
        List<Map.Entry<ActivityKey, Activity>> taken = take(activity);
        for (Map.Entry<ActivityKey, Activity> entry : taken) {
            ActivityKey key = entry.getKey();
            Activity a = entry.getValue();
            try {
                jdbc.sql("""
                                INSERT INTO user_daily_activity (user_id, day, platform, request_count, first_seen_at,
                                                                 last_seen_at)
                                SELECT :user, :day, :platform, :requests, :first, :last
                                 WHERE EXISTS (SELECT 1 FROM users WHERE id = :user)
                                ON CONFLICT (user_id, day, platform) DO UPDATE SET
                                    request_count = user_daily_activity.request_count + EXCLUDED.request_count,
                                    first_seen_at = LEAST(user_daily_activity.first_seen_at, EXCLUDED.first_seen_at),
                                    last_seen_at = GREATEST(user_daily_activity.last_seen_at, EXCLUDED.last_seen_at)
                                """)
                        .param("user", key.userId()).param("day", Date.valueOf(key.day()))
                        .param("platform", key.platform().value()).param("requests", a.requests)
                        .param("first", Timestamp.from(a.first)).param("last", Timestamp.from(a.last))
                        .update();
            } catch (DataAccessException e) {
                log.warn("Statistik pengguna aktif belum tersimpan, dicoba lagi nanti: {}", e.getMessage());
                activity.merge(key, a, (current, failed) -> {
                    current.add(failed);
                    return current;
                });
            }
        }
    }

    /** Mengambil semua hitungan dari memori; hitungan baru setelah ini masuk ke entri baru. */
    private static <K, V> List<Map.Entry<K, V>> take(Map<K, V> map) {
        List<Map.Entry<K, V>> taken = new ArrayList<>();
        for (K key : new ArrayList<>(map.keySet())) {
            V value = map.remove(key);
            if (value != null) {
                taken.add(Map.entry(key, value));
            }
        }
        return taken;
    }
}
