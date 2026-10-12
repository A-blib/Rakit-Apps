package com.aris.templateapp.ui.editor;

/** Kode error lokal editor template mode (bukan dari server). */
final class EditorErrors {

    /** Project dibuka dari daftar, tetapi barisnya sudah tidak ada atau bukan project template. */
    static final String PROJECT_NOT_FOUND = "PROJECT_NOT_FOUND";
    /** Paket template project ini tidak ada lagi di HP (mis. data app dibersihkan sebagian). */
    static final String PACKAGE_MISSING = "EDITOR_PACKAGE_MISSING";

    private EditorErrors() {
    }
}
