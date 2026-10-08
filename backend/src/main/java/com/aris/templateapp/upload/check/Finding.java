package com.aris.templateapp.upload.check;

/**
 * Satu masalah hasil pengecekan: apa yang salah, letaknya (file & baris jika ada), dan cara memperbaikinya
 * (bagian 5.4). {@code snippet} = potongan kode yang tertangkap, ikut disimpan untuk laporan "Ini keliru?".
 */
public record Finding(CheckRule rule, String message, String file, Integer line, String suggestion, String snippet) {
}
