package com.aris.templateapp.data.remote.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Data tandaan provider (alur-fitur-upload.md bagian 11), sama bentuknya dengan {@code MarkingData} di backend.
 * Disimpan ke server lewat tombol Simpan, dan dicadangkan di HP selama belum disimpan.
 */
public class MarkingDto {
    public List<Page> pages = new ArrayList<>();
    public List<Section> sections = new ArrayList<>();
    public List<Field> fields = new ArrayList<>();
    public List<ThemeVar> theme = new ArrayList<>();

    public static class Page {
        public String file;
        public String name;

        public Page(String file, String name) {
            this.file = file;
            this.name = name;
        }
    }

    /** {@code tplId} null = seluruh halaman (section "Halaman"). */
    public static class Section {
        public String id;
        public String page;
        public String name;
        public Integer tplId;

        public Section(String id, String page, String name, Integer tplId) {
            this.id = id;
            this.page = page;
            this.name = name;
            this.tplId = tplId;
        }
    }

    public static class Field {
        public String key;
        public String label;
        /** text · paragraph · image · link · button */
        public String type;
        public String hint;
        public Integer maxLength;
        public boolean required;
        public int order;
        public String aspectRatio;
        public List<Style> styles = new ArrayList<>();
        public String sectionId;
        public List<Element> elements = new ArrayList<>();
    }

    public static class Style {
        /** color · background-color · font-size · border-radius */
        public String prop;
        public Integer min;
        public Integer max;
        public String unit;

        public Style(String prop, Integer min, Integer max, String unit) {
            this.prop = prop;
            this.min = min;
            this.max = max;
            this.unit = unit;
        }
    }

    public static class Element {
        public String page;
        public int tplId;
        /** mobile dan/atau desktop */
        public List<String> visibleIn = new ArrayList<>();

        public Element(String page, int tplId, List<String> visibleIn) {
            this.page = page;
            this.tplId = tplId;
            this.visibleIn = visibleIn;
        }
    }

    public static class ThemeVar {
        public String var;
        public String label;
        /** color · size */
        public String type;

        public ThemeVar(String var, String label, String type) {
            this.var = var;
            this.label = label;
            this.type = type;
        }
    }
}
