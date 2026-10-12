package com.aris.templateapp.monitoring.dto;

import java.time.LocalDate;

/** Satu titik grafik. Hari tanpa data tetap dikirim dengan count 0, jadi web tidak perlu mengisi celah. */
public record DailyPoint(LocalDate date, long count) {
}
