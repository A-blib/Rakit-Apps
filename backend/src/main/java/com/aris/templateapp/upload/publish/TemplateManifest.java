package com.aris.templateapp.upload.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

/**
 * Isi {@code manifest.json} di dalam paket template (alur-buat-website-via-template.md bagian 11.3). Dibaca HP pembuat
 * website untuk menyusun form editor; elemennya ditemukan lewat atribut {@code data-key} di HTML paket.
 * <p>
 * Berbeda dari {@code MarkingData} (milik provider), manifest tidak memuat nomor {@code data-tpl-id}: nomor itu hanya
 * berlaku untuk HTML asli dan sudah dihapus dari paket.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TemplateManifest(UUID templateId, int version, List<Page> pages, List<Section> sections,
                               List<ThemeVar> theme, List<Field> fields) {

    public record Page(String file, String name) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Section(String id, String page, String name) {
    }

    /** @param defaultValue nilai variabel di {@code :root} CSS template, ditulis sebagai "default" di JSON */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ThemeVar(String var, String label, String type, @JsonProperty("default") String defaultValue) {
    }

    /**
     * @param pages      halaman tempat isian ini muncul (isian terhubung bisa lebih dari satu halaman)
     * @param sample     isi contoh provider: teks (text, paragraph, button), URL (link), atau path gambar dari folder
     *                   utama paket (image). Dipakai aturan "masih teks/foto contoh".
     * @param sampleHref link contoh untuk jenis button
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Field(String key, String label, String type, String hint, Integer maxLength, boolean required,
                        int order, String sectionId, String aspectRatio, List<String> pages, String sample,
                        String sampleHref, List<Style> styles) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Style(String prop, Integer min, Integer max, String unit) {
    }
}
