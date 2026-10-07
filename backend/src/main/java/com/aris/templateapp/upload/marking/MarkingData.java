package com.aris.templateapp.upload.marking;

import java.util.List;

/**
 * Data tandaan provider (alur-fitur-upload.md bagian 11, sketsa). Disimpan sebagai JSON di {@code templates.marking}.
 *
 * @param pages    halaman HTML beserta nama yang tampil di pemilih halaman
 * @param sections section per halaman (hasil deteksi + koreksi provider); hanya alat navigasi
 * @param fields   isian yang boleh diedit pembuat website
 * @param theme    variabel CSS yang dijadikan pengaturan tema global (bagian 7.11)
 */
public record MarkingData(List<Page> pages, List<Section> sections, List<Field> fields, List<ThemeVar> theme) {

    public record Page(String file, String name) {
    }

    /** @param tplId nomor elemen pembungkus section, null untuk section "Halaman" (seluruh halaman) */
    public record Section(String id, String page, String name, Integer tplId) {
    }

    /**
     * @param type        text · paragraph · image · link · button
     * @param maxLength   batas karakter (teks), null = tanpa batas
     * @param aspectRatio rasio gambar, mis. "16:9" (jenis image)
     * @param elements    satu isian bisa mengubah beberapa elemen (versi HP & desktop, atau lintas halaman)
     */
    public record Field(String key, String label, String type, String hint, Integer maxLength, boolean required,
                        int order, String aspectRatio, List<Style> styles, String sectionId, List<Element> elements) {
    }

    /** @param prop color · background-color · font-size · border-radius */
    public record Style(String prop, Integer min, Integer max, String unit) {
    }

    /** @param visibleIn mobile dan/atau desktop */
    public record Element(String page, int tplId, List<String> visibleIn) {
    }

    /** @param type color · size */
    public record ThemeVar(String var, String label, String type) {
    }
}
