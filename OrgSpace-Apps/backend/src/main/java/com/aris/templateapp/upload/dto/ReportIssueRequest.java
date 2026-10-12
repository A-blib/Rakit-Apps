package com.aris.templateapp.upload.dto;

import jakarta.validation.constraints.Size;

/** Laporan "Ini keliru?" (bagian 5.9). Alasan boleh kosong. */
public record ReportIssueRequest(@Size(max = 500, message = "Alasan maksimal 500 karakter") String reason) {
}
