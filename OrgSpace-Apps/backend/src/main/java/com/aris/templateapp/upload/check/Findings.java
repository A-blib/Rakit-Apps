package com.aris.templateapp.upload.check;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Penampung semua masalah. Semua aturan tetap dijalankan dan dilaporkan sekaligus (bagian 5.6), tetapi satu aturan
 * paling banyak menampilkan {@value #MAX_PER_RULE} lokasi agar layar "Belum memenuhi standar" tidak banjir;
 * sisanya diringkas menjadi satu baris "Dan N lokasi lain".
 */
public final class Findings {

    static final int MAX_PER_RULE = 10;

    private final List<Finding> items = new ArrayList<>();
    private final Map<CheckRule, Integer> counts = new EnumMap<>(CheckRule.class);
    // Mencegah masalah yang sama persis tercatat dua kali (mis. file yang sama dirujuk dua halaman).
    private final Set<String> seen = new HashSet<>();

    public void add(CheckRule rule, String message, String file, Integer line, String suggestion) {
        add(rule, message, file, line, suggestion, null);
    }

    public void add(CheckRule rule, String message, String file, Integer line, String suggestion, String snippet) {
        if (!seen.add(rule + "|" + message + "|" + file + "|" + line)) {
            return;
        }
        int count = counts.merge(rule, 1, Integer::sum);
        if (count <= MAX_PER_RULE) {
            items.add(new Finding(rule, message, file, line, suggestion, Texts.snippet(snippet)));
        }
    }

    public boolean has(CheckRule rule) {
        return counts.containsKey(rule);
    }

    public boolean hasFatal() {
        return counts.keySet().stream().anyMatch(CheckRule::fatal);
    }

    /** Daftar akhir, ditambah ringkasan untuk aturan yang lokasinya melebihi batas. */
    public List<Finding> toList() {
        List<Finding> result = new ArrayList<>(items);
        counts.forEach((rule, count) -> {
            if (count > MAX_PER_RULE) {
                result.add(new Finding(rule, "Dan " + (count - MAX_PER_RULE) + " lokasi lain dengan masalah yang sama.",
                        null, null, null, null));
            }
        });
        return result;
    }
}
