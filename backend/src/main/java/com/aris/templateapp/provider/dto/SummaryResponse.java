package com.aris.templateapp.provider.dto;

/**
 * Angka ringkasan (bagian 3.4).
 *
 * @param active    template yang sedang tayang (tidak terpengaruh periode)
 * @param views     detail template dibuka dalam periode
 * @param downloads website dari template diexport dalam periode
 * @param period    "7d" atau "30d"
 */
public record SummaryResponse(long active, long views, long downloads, String period) {
}
