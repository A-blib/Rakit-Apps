package com.aris.templateapp.data.model;

/**
 * Tahap project (alur-pembuatan-website.md bagian 4.2): Draft → Siap export → Sudah diexport.
 * Status dihitung app setiap kali project disimpan; aturan cek detailnya ditentukan bersama editor.
 */
public enum ProjectStatus {
    DRAFT("draft"),
    READY("ready"),
    EXPORTED("exported");

    private final String value;

    ProjectStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static ProjectStatus fromValue(String value) {
        for (ProjectStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Status project tidak dikenal: " + value);
    }
}
