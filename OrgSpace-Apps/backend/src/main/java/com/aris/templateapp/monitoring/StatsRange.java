package com.aris.templateapp.monitoring;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Rentang statistik dashboard: N hari terakhir TERMASUK hari ini, di zona waktu app, beserta rentang sebelumnya yang
 * sama panjang (untuk persentase naik/turun).
 */
public record StatsRange(int days, LocalDate firstDay, LocalDate lastDay, LocalDate previousFirstDay,
                         LocalDate previousLastDay) {

    public static StatsRange parse(String value, Clock clock, ZoneId zone) {
        int days = switch (value == null ? "30d" : value) {
            case "7d" -> 7;
            case "30d" -> 30;
            case "90d" -> 90;
            default -> throw new ApiException(ErrorCode.VALIDATION_ERROR, "Periode harus 7d, 30d, atau 90d.");
        };
        LocalDate today = LocalDate.now(clock.withZone(zone));
        LocalDate first = today.minusDays(days - 1L);
        return new StatsRange(days, first, today, first.minusDays(days), first.minusDays(1));
    }
}
