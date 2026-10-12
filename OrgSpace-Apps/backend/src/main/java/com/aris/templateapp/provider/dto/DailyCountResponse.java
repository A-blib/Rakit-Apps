package com.aris.templateapp.provider.dto;

import java.time.LocalDate;

/** Satu titik grafik tren. Hari tanpa download tetap dikirim dengan count 0 (bagian 3.7). */
public record DailyCountResponse(LocalDate date, long count) {
}
