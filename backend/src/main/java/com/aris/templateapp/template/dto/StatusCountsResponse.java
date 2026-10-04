package com.aris.templateapp.template.dto;

/**
 * Jumlah template per chip filter status, mis. "Tayang (4)" (bagian 6.3).
 *
 * @param needsFix tidak lolos pengecekan + tayang dengan peringatan
 */
public record StatusCountsResponse(long all, long published, long needsFix, long checking, long draft, long disabled) {
}
