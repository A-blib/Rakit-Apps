package com.aris.templateapp.provider;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Periode ringkasan statistik: 7 atau 30 hari terakhir, TERMASUK hari ini (bagian 3.1 & 3.7).
 * "Hari" dihitung di zona waktu app (bukan UTC), agar download jam 01.00 WIB masuk ke tanggal yang benar.
 */
public record StatsPeriod(int days, LocalDate firstDay, LocalDate lastDay, Instant from) {

    public static StatsPeriod parse(String value, Clock clock, ZoneId zone) {
        int days = switch (value == null ? "7d" : value) {
            case "7d" -> 7;
            case "30d" -> 30;
            default -> throw new ApiException(ErrorCode.VALIDATION_ERROR, "Periode harus 7d atau 30d.");
        };
        LocalDate today = LocalDate.now(clock.withZone(zone));
        LocalDate first = today.minusDays(days - 1L);
        return new StatsPeriod(days, first, today, first.atStartOfDay(zone).toInstant());
    }
}
