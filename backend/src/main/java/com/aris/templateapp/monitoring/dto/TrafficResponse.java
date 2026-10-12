package com.aris.templateapp.monitoring.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Lalu lintas API. {@code errors} = respons 5xx (kesalahan server), {@code clientErrors} = 4xx (mis. salah input,
 * belum login). Data hari ini bisa tertinggal hingga {@code PORTAL_USAGE_FLUSH_SECONDS} detik.
 */
public record TrafficResponse(LocalDate from, LocalDate to, long requests, long errors, long clientErrors,
                              List<Day> days, List<Route> topRoutes) {

    public record Day(LocalDate date, long requests, long errors, long clientErrors) {
    }

    public record Route(String method, String route, long requests, long errors, long clientErrors, long avgMs,
                        int maxMs) {
    }
}
