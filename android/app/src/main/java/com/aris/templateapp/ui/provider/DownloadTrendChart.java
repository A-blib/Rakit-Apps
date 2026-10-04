package com.aris.templateapp.ui.provider;

import com.aris.templateapp.data.remote.dto.ProviderDashboardDto.DailyCountDto;
import com.aris.templateapp.ui.common.AreaChartView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Mengubah tren download dari backend menjadi titik grafik (alur-provider.md bagian 3.7).
 * Sumbu X: 7 hari = nama hari ("Sen"), 30 hari = tanggal ("4/10"). Fungsi murni tanpa Android agar mudah diuji.
 */
public final class DownloadTrendChart {

    static final Locale INDONESIA = Locale.forLanguageTag("id-ID");
    private static final DateTimeFormatter DETAIL = DateTimeFormatter.ofPattern("EEE, d MMM", INDONESIA);
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("d/M", INDONESIA);
    /** Lebih dari ini dianggap periode panjang (30 hari): label tanggal, tidak semua titik diberi label. */
    private static final int WEEK = 7;

    private DownloadTrendChart() {
    }

    public static List<AreaChartView.Point> toPoints(List<DailyCountDto> trend) {
        List<AreaChartView.Point> points = new ArrayList<>();
        if (trend == null) {
            return points;
        }
        boolean week = trend.size() <= WEEK;
        for (DailyCountDto day : trend) {
            LocalDate date = LocalDate.parse(day.date);
            String axis = week ? date.getDayOfWeek().getDisplayName(TextStyle.SHORT, INDONESIA) : date.format(SHORT_DATE);
            points.add(new AreaChartView.Point(axis, date.format(DETAIL), day.count));
        }
        return points;
    }

    /** 7 titik: semua diberi label; 30 titik: setiap 5 hari (ditambah hari terakhir) agar tidak berdempetan. */
    public static int labelEvery(int size) {
        return size <= WEEK ? 1 : 5;
    }

    /** true jika semua hari bernilai 0: tampilkan teks "Belum ada download di periode ini." */
    public static boolean isEmpty(List<DailyCountDto> trend) {
        if (trend != null) {
            for (DailyCountDto day : trend) {
                if (day.count > 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
