package com.aris.templateapp.template.dto;

import java.util.UUID;

/**
 * Satu masalah hasil pengecekan: pesan, letak (file & baris bila ada), dan saran perbaikan.
 *
 * @param id          dipakai untuk "Ini keliru? Laporkan" (alur-fitur-upload.md bagian 5.9)
 * @param code        kode aturan; app memakainya untuk membuka artikel Panduan (bagian 5.11)
 * @param title       judul singkat aturan, mis. "File index.html tidak ada"; null untuk kode di luar daftar aturan
 * @param ruleVersion versi aturan saat dicek
 * @param reported    true jika provider sudah melaporkan masalah ini sebagai keliru
 */
public record IssueResponse(UUID id, String code, String title, int ruleVersion, String message, String file,
                            Integer line, String suggestion, boolean reported) {
}
