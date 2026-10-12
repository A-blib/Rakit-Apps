package com.aris.templateapp.monitoring;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;

/**
 * Metrik yang bisa digambar sebagai grafik harian di dashboard. Nilai {@code value} adalah kontrak API dengan web.
 * <p>
 * {@code daily} = query yang menghasilkan kolom {@code day} (DATE) dan {@code n} (jumlah) untuk hari
 * {@code :from}..{@code :to}; waktu dikonversi ke zona app ({@code :tz}) sebelum diambil tanggalnya.
 */
public enum StatsMetric {
    SIGNUPS("signups", """
            SELECT (created_at AT TIME ZONE :tz)::date AS day, count(*) AS n FROM users
             WHERE (created_at AT TIME ZONE :tz)::date BETWEEN :from AND :to GROUP BY 1"""),
    ACTIVE_USERS("active_users", """
            SELECT day, count(DISTINCT user_id) AS n FROM user_daily_activity
             WHERE day BETWEEN :from AND :to GROUP BY 1"""),
    NEW_ORGANIZATIONS("new_organizations", """
            SELECT (created_at AT TIME ZONE :tz)::date AS day, count(*) AS n FROM organizations
             WHERE type = 'organization' AND (created_at AT TIME ZONE :tz)::date BETWEEN :from AND :to GROUP BY 1"""),
    NEW_WEBSITES("new_websites", """
            SELECT (created_at AT TIME ZONE :tz)::date AS day, count(*) AS n FROM projects
             WHERE (created_at AT TIME ZONE :tz)::date BETWEEN :from AND :to GROUP BY 1"""),
    PUBLISHES("publishes", """
            SELECT (created_at AT TIME ZONE :tz)::date AS day, count(*) AS n FROM site_releases
             WHERE kind = 'publish' AND (created_at AT TIME ZONE :tz)::date BETWEEN :from AND :to GROUP BY 1"""),
    TEMPLATE_VIEWS("template_views", """
            SELECT (occurred_at AT TIME ZONE :tz)::date AS day, count(*) AS n FROM template_events
             WHERE type = 'view' AND (occurred_at AT TIME ZONE :tz)::date BETWEEN :from AND :to GROUP BY 1"""),
    TEMPLATE_DOWNLOADS("template_downloads", """
            SELECT (occurred_at AT TIME ZONE :tz)::date AS day, count(*) AS n FROM template_events
             WHERE type = 'download' AND (occurred_at AT TIME ZONE :tz)::date BETWEEN :from AND :to GROUP BY 1"""),
    FEEDBACK("feedback", """
            SELECT (created_at AT TIME ZONE :tz)::date AS day, count(*) AS n FROM feedback
             WHERE (created_at AT TIME ZONE :tz)::date BETWEEN :from AND :to GROUP BY 1"""),
    API_REQUESTS("api_requests", """
            SELECT day, sum(request_count) AS n FROM api_traffic_daily
             WHERE day BETWEEN :from AND :to GROUP BY 1"""),
    API_ERRORS("api_errors", """
            SELECT day, sum(error_count) AS n FROM api_traffic_daily
             WHERE day BETWEEN :from AND :to GROUP BY 1""");

    private final String value;
    private final String daily;

    StatsMetric(String value, String daily) {
        this.value = value;
        this.daily = daily;
    }

    public String value() {
        return value;
    }

    String daily() {
        return daily;
    }

    public static StatsMetric parse(String value) {
        for (StatsMetric metric : values()) {
            if (metric.value.equals(value)) {
                return metric;
            }
        }
        throw new ApiException(ErrorCode.VALIDATION_ERROR, "Metrik tidak dikenal: " + value);
    }
}
