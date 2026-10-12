package com.aris.templateapp.data.model;

/**
 * Peran di ruang kerja (revisi-fondasi-orgspace.md 3.1). Urutan konstanta = urutan hak: Pemilik paling tinggi.
 * App hanya memakai peran untuk menyembunyikan tombol; aturan sebenarnya dijaga backend.
 */
public enum WorkspaceRole {
    OWNER("owner"),
    ADMIN("admin"),
    EDITOR("editor");

    private final String value;

    WorkspaceRole(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    /** true jika peran ini sama atau lebih tinggi dari {@code minimum}. */
    public boolean atLeast(WorkspaceRole minimum) {
        return ordinal() <= minimum.ordinal();
    }

    /** Nilai tidak dikenal dianggap EDITOR, peran dengan hak paling sedikit. */
    public static WorkspaceRole fromValue(String value) {
        for (WorkspaceRole role : values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        return EDITOR;
    }
}
