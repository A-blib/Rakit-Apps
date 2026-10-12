package com.aris.templateapp.data.model;

/** Cara project dibuat: dari template jadi atau disusun sendiri (alur-pembuatan-website.md bagian 1). */
public enum ProjectMode {
    TEMPLATE("template"),
    CUSTOM("custom");

    private final String value;

    ProjectMode(String value) {
        this.value = value;
    }

    /** Nilai yang disimpan di database. */
    public String value() {
        return value;
    }

    public static ProjectMode fromValue(String value) {
        for (ProjectMode mode : values()) {
            if (mode.value.equals(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Mode project tidak dikenal: " + value);
    }
}
