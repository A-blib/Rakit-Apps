package com.aris.templateapp.upload.marking;

import java.util.ArrayList;
import java.util.Comparator;
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

    /**
     * Isian diurutkan menurut posisi elemen pertamanya di situs (urutan halaman, lalu data-tpl-id yang mengikuti urutan
     * HTML), dan {@code order} dinomori ulang. Form pembuat website jadi tersusun dari atas ke bawah seperti halamannya,
     * apa pun urutan provider menandai.
     */
    public MarkingData inPageOrder() {
        if (fields == null || fields.isEmpty()) {
            return this;
        }
        List<String> pageOrder = new ArrayList<>();
        if (pages != null) {
            pages.forEach(p -> pageOrder.add(p.file()));
        }
        Comparator<Field> byPosition = Comparator
                .comparingInt((Field f) -> f.elements() == null || f.elements().isEmpty() ? Integer.MAX_VALUE
                        : pageIndex(pageOrder, f.elements().get(0).page()))
                .thenComparingInt(f -> f.elements() == null || f.elements().isEmpty() ? Integer.MAX_VALUE
                        : f.elements().get(0).tplId());
        List<Field> sorted = new ArrayList<>(fields);
        sorted.sort(byPosition);
        List<Field> numbered = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            Field f = sorted.get(i);
            numbered.add(new Field(f.key(), f.label(), f.type(), f.hint(), f.maxLength(), f.required(), i + 1,
                    f.aspectRatio(), f.styles(), f.sectionId(), f.elements()));
        }
        return new MarkingData(pages, sections, numbered, theme);
    }

    private static int pageIndex(List<String> pageOrder, String page) {
        int index = pageOrder.indexOf(page);
        return index < 0 ? Integer.MAX_VALUE : index;
    }

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
