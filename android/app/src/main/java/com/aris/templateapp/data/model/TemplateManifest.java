package com.aris.templateapp.data.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Isi {@code manifest.json} di paket template (alur-buat-website-via-template.md bagian 11.3), dibaca Gson dari file
 * paket di HP. Elemen di HTML ditemukan lewat {@code data-key} yang sama dengan {@link Field#key}.
 */
public class TemplateManifest {

    public static final String TYPE_TEXT = "text";
    public static final String TYPE_PARAGRAPH = "paragraph";
    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_LINK = "link";
    public static final String TYPE_BUTTON = "button";

    public String templateId;
    public int version;
    public List<Page> pages = new ArrayList<>();
    public List<Section> sections = new ArrayList<>();
    public List<ThemeVar> theme = new ArrayList<>();
    public List<Field> fields = new ArrayList<>();

    public static class Page {
        public String file;
        public String name;
    }

    public static class Section {
        public String id;
        public String page;
        public String name;
    }

    public static class ThemeVar {
        public String var;
        public String label;
        /** "color" atau "size". */
        public String type;
        @Nullable
        @SerializedName("default")
        public String defaultValue;
    }

    public static class Field {
        public String key;
        public String label;
        public String type;
        @Nullable
        public String hint;
        @Nullable
        public Integer maxLength;
        public boolean required;
        public int order;
        @Nullable
        public String sectionId;
        /** Rasio gambar, mis. "16:9"; null = bebas. */
        @Nullable
        public String aspectRatio;
        /** Halaman tempat isian ini muncul (isian terhubung bisa lebih dari satu). */
        public List<String> pages = new ArrayList<>();
        /** Isi contoh provider: teks, URL (link), atau path gambar dari folder utama paket. */
        @Nullable
        public String sample;
        /** Link contoh untuk jenis tombol. */
        @Nullable
        public String sampleHref;
        public List<Style> styles = new ArrayList<>();

        public boolean hasText() {
            return !TYPE_IMAGE.equals(type) && !TYPE_LINK.equals(type);
        }

        public boolean hasHref() {
            return TYPE_LINK.equals(type) || TYPE_BUTTON.equals(type);
        }
    }

    /** prop: color · background-color · font-size · border-radius. */
    public static class Style {
        public String prop;
        @Nullable
        public Integer min;
        @Nullable
        public Integer max;
        @Nullable
        public String unit;

        public boolean isColor() {
            return prop != null && prop.endsWith("color");
        }
    }

    @Nullable
    public Field field(String key) {
        for (Field f : fields) {
            if (f.key.equals(key)) {
                return f;
            }
        }
        return null;
    }

    /** Nama halaman untuk pemilih halaman; file jika tidak ada namanya. */
    public String pageName(String file) {
        for (Page p : pages) {
            if (p.file.equals(file)) {
                return p.name == null || p.name.isEmpty() ? p.file : p.name;
            }
        }
        return file;
    }
}
