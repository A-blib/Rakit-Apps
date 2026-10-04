package com.aris.templateapp.template.dto;

import com.aris.templateapp.template.CheckStatus;

import java.time.Instant;
import java.util.List;

/** Hasil pengecekan terakhir untuk layar detail (bagian 4.5), dipisah menjadi error dan peringatan. */
public record CheckResponse(int version, CheckStatus status, Instant finishedAt,
                            List<IssueResponse> errors, List<IssueResponse> warnings) {
}
