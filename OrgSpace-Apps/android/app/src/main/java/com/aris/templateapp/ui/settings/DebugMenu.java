package com.aris.templateapp.ui.settings;

import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

/**
 * Menu tambahan khusus build debug di bagian bawah Pengaturan (mis. "Isi project contoh").
 * Implementasinya hanya ada di source set {@code src/debug/}, sehingga tidak pernah ikut ke build release;
 * di release, {@code Optional<DebugMenu>} kosong (lihat {@code DebugMenuModule}).
 */
public interface DebugMenu {

    /** Menambahkan baris-baris menu debug ke {@code container}. */
    void addTo(ViewGroup container, Fragment host);
}
