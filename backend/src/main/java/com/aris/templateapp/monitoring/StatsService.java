package com.aris.templateapp.monitoring;

import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.monitoring.dto.ActivePlatformsResponse;
import com.aris.templateapp.monitoring.dto.DailyPoint;
import com.aris.templateapp.monitoring.dto.OverviewResponse;
import com.aris.templateapp.monitoring.dto.SignupMethodsResponse;
import com.aris.templateapp.monitoring.dto.TimeseriesResponse;
import com.aris.templateapp.monitoring.dto.TrafficResponse;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Angka untuk dashboard monitoring portal web. Semua dihitung langsung dari tabel (tanpa tabel ringkasan), cukup
 * cepat untuk skala awal; jika nanti lambat, tambahkan tabel ringkasan harian tanpa mengubah bentuk respons.
 */
@Service
@Transactional(readOnly = true)
public class StatsService {

    /** Banyak endpoint di daftar "paling sering dipanggil". */
    static final int TOP_ROUTES = 15;

    private final JdbcClient jdbc;
    private final Clock clock;
    private final ZoneId zone;

    public StatsService(JdbcClient jdbc, Clock clock, AppProperties properties) {
        this.jdbc = jdbc;
        this.clock = clock;
        this.zone = ZoneId.of(properties.timezone());
    }

    public StatsRange range(String period) {
        return StatsRange.parse(period, clock, zone);
    }

    public OverviewResponse overview(StatsRange range) {
        OverviewResponse.Totals totals = new OverviewResponse.Totals(
                count("SELECT count(*) FROM users"),
                count("SELECT count(*) FROM organizations WHERE type = 'organization'"),
                count("SELECT count(*) FROM organizations WHERE type = 'personal'"),
                count("SELECT count(*) FROM projects"),
                count("SELECT count(*) FROM sites WHERE status = 'online'"),
                count("SELECT count(*) FROM templates WHERE status = 'published'"),
                count("SELECT count(*) FROM provider_profiles"),
                count("SELECT count(*) FROM feedback WHERE status IN ('new', 'in_progress')"));
        OverviewResponse.Period period = new OverviewResponse.Period(
                change(StatsMetric.SIGNUPS, range),
                new OverviewResponse.Change(distinctActive(range.firstDay(), range.lastDay()),
                        distinctActive(range.previousFirstDay(), range.previousLastDay())),
                change(StatsMetric.NEW_ORGANIZATIONS, range),
                change(StatsMetric.NEW_WEBSITES, range),
                change(StatsMetric.PUBLISHES, range),
                change(StatsMetric.TEMPLATE_DOWNLOADS, range),
                change(StatsMetric.FEEDBACK, range),
                change(StatsMetric.API_REQUESTS, range),
                change(StatsMetric.API_ERRORS, range));
        LocalDate today = range.lastDay();
        OverviewResponse.Active active = new OverviewResponse.Active(
                distinctActive(today, today),
                distinctActive(today.minusDays(6), today),
                distinctActive(today.minusDays(29), today));
        return new OverviewResponse(range.firstDay(), range.lastDay(), totals, period, active);
    }

    public TimeseriesResponse timeseries(StatsMetric metric, StatsRange range) {
        Map<LocalDate, Long> counts = daily(metric, range.firstDay(), range.lastDay());
        List<DailyPoint> points = range.firstDay().datesUntil(range.lastDay().plusDays(1))
                .map(day -> new DailyPoint(day, counts.getOrDefault(day, 0L)))
                .toList();
        long total = points.stream().mapToLong(DailyPoint::count).sum();
        return new TimeseriesResponse(metric.value(), range.firstDay(), range.lastDay(), total, points);
    }

    public SignupMethodsResponse signupMethods(StatsRange range) {
        Map<String, Long> byProvider = new HashMap<>();
        // Metode daftar = identitas tertua milik user (identitas yang disambungkan belakangan tidak dihitung).
        jdbc.sql("""
                        SELECT first.provider, count(*) AS n
                          FROM users u
                          JOIN LATERAL (SELECT provider FROM user_identities i WHERE i.user_id = u.id
                                         ORDER BY i.created_at LIMIT 1) first ON true
                         WHERE (u.created_at AT TIME ZONE :tz)::date BETWEEN :from AND :to
                         GROUP BY first.provider""")
                .param("tz", zone.getId())
                .param("from", Date.valueOf(range.firstDay())).param("to", Date.valueOf(range.lastDay()))
                .query((rs, row) -> byProvider.put(rs.getString("provider"), rs.getLong("n")))
                .list();
        return new SignupMethodsResponse(range.firstDay(), range.lastDay(), byProvider.getOrDefault("local", 0L),
                byProvider.getOrDefault("google", 0L), byProvider.getOrDefault("github", 0L));
    }

    public ActivePlatformsResponse activePlatforms(StatsRange range) {
        Map<String, Long> byPlatform = new HashMap<>();
        jdbc.sql("""
                        SELECT platform, count(DISTINCT user_id) AS n FROM user_daily_activity
                         WHERE day BETWEEN :from AND :to GROUP BY platform""")
                .param("from", Date.valueOf(range.firstDay())).param("to", Date.valueOf(range.lastDay()))
                .query((rs, row) -> byPlatform.put(rs.getString("platform"), rs.getLong("n")))
                .list();
        return new ActivePlatformsResponse(range.firstDay(), range.lastDay(),
                byPlatform.getOrDefault(ClientPlatform.ANDROID.value(), 0L),
                byPlatform.getOrDefault(ClientPlatform.WEB.value(), 0L),
                byPlatform.getOrDefault(ClientPlatform.OTHER.value(), 0L),
                distinctActive(range.firstDay(), range.lastDay()));
    }

    public TrafficResponse traffic(StatsRange range) {
        Map<LocalDate, TrafficResponse.Day> byDay = new HashMap<>();
        jdbc.sql("""
                        SELECT day, sum(request_count) AS requests, sum(error_count) AS errors,
                               sum(client_error_count) AS client_errors
                          FROM api_traffic_daily WHERE day BETWEEN :from AND :to GROUP BY day""")
                .param("from", Date.valueOf(range.firstDay())).param("to", Date.valueOf(range.lastDay()))
                .query((rs, row) -> {
                    LocalDate day = rs.getDate("day").toLocalDate();
                    return byDay.put(day, new TrafficResponse.Day(day, rs.getLong("requests"), rs.getLong("errors"),
                            rs.getLong("client_errors")));
                })
                .list();
        List<TrafficResponse.Day> days = range.firstDay().datesUntil(range.lastDay().plusDays(1))
                .map(day -> byDay.getOrDefault(day, new TrafficResponse.Day(day, 0, 0, 0)))
                .toList();
        List<TrafficResponse.Route> routes = jdbc.sql("""
                        SELECT method, route, sum(request_count) AS requests, sum(error_count) AS errors,
                               sum(client_error_count) AS client_errors, sum(total_ms) AS total_ms, max(max_ms) AS max_ms
                          FROM api_traffic_daily WHERE day BETWEEN :from AND :to
                         GROUP BY method, route ORDER BY requests DESC, route LIMIT :limit""")
                .param("from", Date.valueOf(range.firstDay())).param("to", Date.valueOf(range.lastDay()))
                .param("limit", TOP_ROUTES)
                .query((rs, row) -> {
                    long requests = rs.getLong("requests");
                    return new TrafficResponse.Route(rs.getString("method"), rs.getString("route"), requests,
                            rs.getLong("errors"), rs.getLong("client_errors"),
                            requests == 0 ? 0 : rs.getLong("total_ms") / requests, rs.getInt("max_ms"));
                })
                .list();
        return new TrafficResponse(range.firstDay(), range.lastDay(),
                days.stream().mapToLong(TrafficResponse.Day::requests).sum(),
                days.stream().mapToLong(TrafficResponse.Day::errors).sum(),
                days.stream().mapToLong(TrafficResponse.Day::clientErrors).sum(),
                days, routes);
    }

    private OverviewResponse.Change change(StatsMetric metric, StatsRange range) {
        return new OverviewResponse.Change(sum(metric, range.firstDay(), range.lastDay()),
                sum(metric, range.previousFirstDay(), range.previousLastDay()));
    }

    private long sum(StatsMetric metric, LocalDate from, LocalDate to) {
        return daily(metric, from, to).values().stream().mapToLong(Long::longValue).sum();
    }

    private Map<LocalDate, Long> daily(StatsMetric metric, LocalDate from, LocalDate to) {
        Map<LocalDate, Long> counts = new HashMap<>();
        var spec = jdbc.sql(metric.daily())
                .param("from", Date.valueOf(from)).param("to", Date.valueOf(to));
        if (metric.daily().contains(":tz")) {
            spec = spec.param("tz", zone.getId());
        }
        spec.query((rs, row) -> counts.put(rs.getDate("day").toLocalDate(), rs.getLong("n"))).list();
        return counts;
    }

    private long distinctActive(LocalDate from, LocalDate to) {
        return jdbc.sql("SELECT count(DISTINCT user_id) FROM user_daily_activity WHERE day BETWEEN :from AND :to")
                .param("from", Date.valueOf(from)).param("to", Date.valueOf(to))
                .query(Long.class).single();
    }

    private long count(String sql) {
        return jdbc.sql(sql).query(Long.class).single();
    }
}
