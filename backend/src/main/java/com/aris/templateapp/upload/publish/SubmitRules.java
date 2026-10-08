package com.aris.templateapp.upload.publish;

import com.aris.templateapp.template.Template;

/**
 * Syarat Kirim yang juga dicek app sebelum tombol aktif: Info template lengkap (alur-fitur-upload.md bagian 6.4)
 * dan minimal 3 isian (bagian 7.12).
 */
public final class SubmitRules {

    public static final int MIN_FIELDS = 3;

    private SubmitRules() {
    }

    /** Pesan untuk isian Info template yang belum memenuhi syarat, atau null jika lengkap. */
    public static String infoProblem(Template t) {
        String name = t.getName() == null ? "" : t.getName().strip();
        if (name.length() < 3 || name.length() > 60) {
            return "Nama template harus 3–60 karakter.";
        }
        if (t.getCategory() == null) {
            return "Pilih kategori template.";
        }
        String description = t.getDescription() == null ? "" : t.getDescription().strip();
        if (description.length() < 20 || description.length() > 300) {
            return "Deskripsi harus 20–300 karakter.";
        }
        if (t.getKeywords() == null || t.getKeywords().isEmpty() || t.getKeywords().size() > 5) {
            return "Isi 1–5 kata kunci.";
        }
        return null;
    }
}
