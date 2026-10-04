package com.aris.templateapp.ui.provider;

import static org.junit.Assert.assertEquals;

import com.aris.templateapp.ui.provider.RelativeTime.Unit;

import org.junit.Test;

import java.time.Duration;
import java.time.Instant;

/** Batas setiap satuan "Diperbarui ... lalu". */
public class RelativeTimeTest {

    private static final Instant NOW = Instant.parse("2026-10-04T10:00:00Z");

    @Test
    public void justNowIncludingSmallClockSkew() {
        assertRelative(Unit.JUST_NOW, 0, NOW.minusSeconds(59));
        assertRelative(Unit.JUST_NOW, 0, NOW.plusSeconds(30));
    }

    @Test
    public void minutesHoursDays() {
        assertRelative(Unit.MINUTES, 1, NOW.minus(Duration.ofMinutes(1)));
        assertRelative(Unit.MINUTES, 59, NOW.minus(Duration.ofMinutes(59)));
        assertRelative(Unit.HOURS, 1, NOW.minus(Duration.ofMinutes(60)));
        assertRelative(Unit.HOURS, 23, NOW.minus(Duration.ofHours(23)));
        assertRelative(Unit.DAYS, 1, NOW.minus(Duration.ofHours(24)));
        assertRelative(Unit.DAYS, 6, NOW.minus(Duration.ofDays(6)));
    }

    @Test
    public void weeksMonthsYears() {
        assertRelative(Unit.WEEKS, 1, NOW.minus(Duration.ofDays(7)));
        assertRelative(Unit.WEEKS, 4, NOW.minus(Duration.ofDays(29)));
        assertRelative(Unit.MONTHS, 1, NOW.minus(Duration.ofDays(30)));
        assertRelative(Unit.MONTHS, 12, NOW.minus(Duration.ofDays(364)));
        assertRelative(Unit.YEARS, 1, NOW.minus(Duration.ofDays(365)));
    }

    private static void assertRelative(Unit unit, long amount, Instant then) {
        RelativeTime relative = RelativeTime.between(then, NOW);
        assertEquals(unit, relative.getUnit());
        assertEquals(amount, relative.getAmount());
    }
}
