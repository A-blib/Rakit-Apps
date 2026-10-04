package com.aris.templateapp.ui.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.aris.templateapp.data.remote.dto.ProviderDashboardDto.DailyCountDto;
import com.aris.templateapp.ui.common.AreaChartView;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Pemetaan tren download → titik grafik (alur-provider.md bagian 3.7). */
public class DownloadTrendChartTest {

    @Test
    public void sevenDaysUseDayNamesAndKeepZeroDays() {
        // 28 Sep 2026 = Senin.
        List<AreaChartView.Point> points = DownloadTrendChart.toPoints(days(LocalDate.of(2026, 9, 28), 3, 0, 5, 0, 0, 1, 2));

        assertEquals(7, points.size());
        assertEquals("Sen", points.get(0).getAxisLabel());
        assertEquals("Min", points.get(6).getAxisLabel());
        assertEquals(0, points.get(1).getValue());
        assertEquals(5, points.get(2).getValue());
        assertEquals("Rab, 30 Sep", points.get(2).getDetailLabel());
        assertEquals(1, DownloadTrendChart.labelEvery(points.size()));
    }

    @Test
    public void thirtyDaysUseShortDatesAndSparseLabels() {
        long[] counts = new long[30];
        List<AreaChartView.Point> points = DownloadTrendChart.toPoints(days(LocalDate.of(2026, 9, 5), counts));

        assertEquals(30, points.size());
        assertEquals("5/9", points.get(0).getAxisLabel());
        assertEquals("4/10", points.get(29).getAxisLabel());
        assertEquals(5, DownloadTrendChart.labelEvery(points.size()));
    }

    @Test
    public void emptyOnlyWhenEveryDayIsZero() {
        assertTrue(DownloadTrendChart.isEmpty(days(LocalDate.of(2026, 9, 28), 0, 0, 0)));
        assertTrue(DownloadTrendChart.isEmpty(null));
        assertFalse(DownloadTrendChart.isEmpty(days(LocalDate.of(2026, 9, 28), 0, 1, 0)));
    }

    @Test
    public void nullTrendGivesNoPoints() {
        assertTrue(DownloadTrendChart.toPoints(null).isEmpty());
    }

    private static List<DailyCountDto> days(LocalDate first, long... counts) {
        List<DailyCountDto> days = new ArrayList<>();
        for (int i = 0; i < counts.length; i++) {
            DailyCountDto day = new DailyCountDto();
            day.date = first.plusDays(i).toString();
            day.count = counts[i];
            days.add(day);
        }
        return days;
    }
}
