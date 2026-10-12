package com.aris.templateapp.monitoring.dto;

import java.time.LocalDate;

/**
 * Ringkasan dashboard monitoring.
 *
 * @param totals  jumlah sepanjang waktu (saat ini)
 * @param period  jumlah di rentang yang dipilih, dibandingkan rentang sebelumnya yang sama panjang
 * @param active  pengguna aktif: hari ini (DAU), 7 hari (WAU), 30 hari (MAU), dihitung mundur dari hari ini
 */
public record OverviewResponse(LocalDate from, LocalDate to, Totals totals, Period period, Active active) {

    public record Totals(long users, long organizations, long personalSpaces, long websites, long sitesOnline,
                         long templatesPublished, long providers, long feedbackOpen) {
    }

    public record Period(Change newUsers, Change activeUsers, Change newOrganizations, Change newWebsites,
                         Change publishes, Change templateDownloads, Change feedback, Change apiRequests,
                         Change apiErrors) {
    }

    /** {@code previous} = nilai di rentang sebelumnya; persentase dihitung web (hindari bagi nol di sini). */
    public record Change(long current, long previous) {
    }

    public record Active(long today, long last7Days, long last30Days) {
    }
}
