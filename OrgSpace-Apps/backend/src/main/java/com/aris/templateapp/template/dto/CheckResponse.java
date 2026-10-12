package com.aris.templateapp.template.dto;

import com.aris.templateapp.template.CheckStatus;
import com.aris.templateapp.upload.check.CheckStage;

import java.time.Instant;
import java.util.List;

/**
 * Hasil pengecekan terakhir, dipisah menjadi error dan peringatan. Dipakai layar detail (alur-provider.md 4.5)
 * dan layar pengecekan Upload (alur-fitur-upload.md 5.8).
 *
 * @param stage tahap yang sedang berjalan; null untuk data lama sebelum fitur Upload
 */
public record CheckResponse(int version, CheckStatus status, CheckStage stage, Instant finishedAt,
                            List<IssueResponse> errors, List<IssueResponse> warnings) {
}
