package com.aris.templateapp.ui.common;

import java.time.Duration;
import java.time.Instant;

/**
 * Menghitung "berapa lama yang lalu" untuk teks "Diperbarui 2 hari lalu" (provider) dan "Diedit 2 jam lalu" (project).
 * Hanya menghitung satuan & jumlah; teksnya dipilih layar dari strings.xml. Tanpa Android agar mudah diuji.
 */
public final class RelativeTime {

    public enum Unit { JUST_NOW, MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS }

    private final Unit unit;
    private final long amount;

    private RelativeTime(Unit unit, long amount) {
        this.unit = unit;
        this.amount = amount;
    }

    public Unit getUnit() {
        return unit;
    }

    public long getAmount() {
        return amount;
    }

    /** Waktu di masa depan (jam HP sedikit terlambat dari server) dianggap "baru saja". */
    public static RelativeTime between(Instant then, Instant now) {
        long minutes = Math.max(0, Duration.between(then, now).toMinutes());
        if (minutes < 1) {
            return new RelativeTime(Unit.JUST_NOW, 0);
        }
        if (minutes < 60) {
            return new RelativeTime(Unit.MINUTES, minutes);
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return new RelativeTime(Unit.HOURS, hours);
        }
        long days = hours / 24;
        if (days < 7) {
            return new RelativeTime(Unit.DAYS, days);
        }
        if (days < 30) {
            return new RelativeTime(Unit.WEEKS, days / 7);
        }
        if (days < 365) {
            return new RelativeTime(Unit.MONTHS, days / 30);
        }
        return new RelativeTime(Unit.YEARS, days / 365);
    }
}
