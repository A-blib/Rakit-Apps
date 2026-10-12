package com.aris.templateapp.monitoring.dto;

import java.time.LocalDate;
import java.util.List;

public record TimeseriesResponse(String metric, LocalDate from, LocalDate to, long total, List<DailyPoint> points) {
}
