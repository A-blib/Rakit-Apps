package com.aris.templateapp.template.dto;

/** Satu masalah hasil pengecekan: pesan, letak (file & baris bila ada), dan saran perbaikan. */
public record IssueResponse(String code, String message, String file, Integer line, String suggestion) {
}
