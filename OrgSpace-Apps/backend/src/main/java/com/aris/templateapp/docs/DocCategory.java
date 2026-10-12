package com.aris.templateapp.docs;

import java.util.Arrays;
import java.util.Optional;

/**
 * Kategori pusat dokumentasi, berurutan seperti tampil di menu Docs. {@code value} = kontrak API (cocok dengan CHECK
 * di tabel doc_articles); menambah kategori butuh migrasi baru.
 */
public enum DocCategory {
    MULAI("mulai", "Memulai"),
    RUANG_KERJA("ruang-kerja", "Ruang kerja & anggota"),
    WEBSITE("website", "Membuat website"),
    TEMPLATE("template", "Template"),
    TERBIT("terbit", "Menerbitkan website"),
    PROVIDER("provider", "Penyedia template"),
    AKUN("akun", "Akun & keamanan"),
    FAQ("faq", "Tanya jawab");

    private final String value;
    private final String label;

    DocCategory(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String value() {
        return value;
    }

    public String label() {
        return label;
    }

    public static Optional<DocCategory> of(String value) {
        return Arrays.stream(values()).filter(c -> c.value.equals(value)).findFirst();
    }
}
