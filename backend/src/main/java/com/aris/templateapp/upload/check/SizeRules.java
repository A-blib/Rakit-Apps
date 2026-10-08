package com.aris.templateapp.upload.check;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Bagian 5.6 G: ukuran dan performa. Batas satu file (Error) sudah dicek saat ZIP diekstrak. */
final class SizeRules {

    private static final Set<String> IMAGES = Set.of("png", "jpg", "jpeg", "webp", "gif", "svg", "ico");

    private final CheckContext ctx;

    SizeRules(CheckContext ctx) {
        this.ctx = ctx;
    }

    void check() {
        long imageLimit = ctx.limits().imageWarnBytes();
        for (String path : ctx.files.paths()) {
            long size = ctx.files.size(path);
            if (IMAGES.contains(Texts.extension(path)) && size > imageLimit && size <= ctx.limits().maxFileBytes()) {
                ctx.findings.add(CheckRule.IMAGE_TOO_LARGE, Texts.fileName(path) + " " + Texts.size(size) + " (maks "
                        + Texts.size(imageLimit) + ").", path, null, "Kompres gambar atau ubah ke WebP (idealnya 100–500 KB).");
            }
        }

        // Berat halaman = HTML + semua file lokal yang dimuatnya, termasuk gambar/font dari CSS yang dimuat halaman itu.
        for (Map.Entry<String, Set<String>> entry : ctx.pageAssets.entrySet()) {
            Set<String> all = new LinkedHashSet<>(entry.getValue());
            for (String asset : entry.getValue()) {
                all.addAll(ctx.cssAssets.getOrDefault(asset, Set.of()));
            }
            all.addAll(ctx.cssAssets.getOrDefault(entry.getKey(), Set.of()));
            long total = ctx.files.size(entry.getKey()) + all.stream().mapToLong(ctx.files::size).sum();
            if (total > ctx.limits().pageWarnBytes()) {
                ctx.findings.add(CheckRule.PAGE_TOO_HEAVY, entry.getKey() + " beratnya " + Texts.size(total)
                                + " (maks " + Texts.size(ctx.limits().pageWarnBytes()) + "); akan lambat dibuka di HP.",
                        entry.getKey(), null, "Kompres gambar dan hapus CSS/JS yang tidak dipakai.");
            }
        }
    }
}
