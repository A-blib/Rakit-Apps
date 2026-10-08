package com.aris.templateapp.core.template;

import java.text.Normalizer;
import java.util.Locale;

/** Nama file ZIP export (alur-buat-website-via-template.md bagian 8.4): "Dapur Mama Rina" → dapur-mama-rina.zip. */
public final class ExportNames {

    private static final String FALLBACK = "website";

    private ExportNames() {
    }

    public static String fileName(String projectName) {
        return slug(projectName) + ".zip";
    }

    /** Huruf kecil, spasi → tanda hubung, hanya a–z 0–9 dan '-'. Huruf beraksen dibuang aksennya (é → e). */
    public static String slug(String text) {
        String plain = Normalizer.normalize(text == null ? "" : text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String slug = plain.toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
        return slug.isEmpty() ? FALLBACK : slug;
    }

    /** Nama yang diketik user di form: dibersihkan dengan aturan yang sama, ".zip" ditambahkan jika tidak ada. */
    public static String clean(String typed) {
        String base = typed == null ? "" : typed.trim();
        if (base.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            base = base.substring(0, base.length() - 4);
        }
        return fileName(base);
    }
}
