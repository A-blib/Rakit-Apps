package com.aris.templateapp.monitoring.dto;

import java.time.LocalDate;

/** Pendaftar baru per metode login pertama (identitas tertua akun itu). */
public record SignupMethodsResponse(LocalDate from, LocalDate to, long email, long google, long github) {
}
