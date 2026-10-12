package com.aris.templateapp.monitoring.dto;

import java.time.LocalDate;

/** Pengguna unik yang aktif di rentang ini per platform (satu user bisa terhitung di dua platform). */
public record ActivePlatformsResponse(LocalDate from, LocalDate to, long android, long web, long other, long total) {
}
