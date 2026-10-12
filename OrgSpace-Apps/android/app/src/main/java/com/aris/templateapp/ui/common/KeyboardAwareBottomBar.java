package com.aris.templateapp.ui.common;

import android.view.View;
import android.view.ViewTreeObserver;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Menyembunyikan bottom navigation (dan garisnya) selama keyboard terbuka, misalnya saat mengetik di kolom cari,
 * supaya ruang layar yang tersisa dipakai untuk isi, bukan untuk tab. Dipakai kedua dashboard.
 * <p>
 * Keadaan keyboard dibaca dari {@link WindowInsetsCompat} setiap kali layout berubah (keyboard muncul/hilang
 * selalu mengubah ukuran layar app karena MainActivity memberi jarak sebesar keyboard).
 */
public final class KeyboardAwareBottomBar implements ViewTreeObserver.OnGlobalLayoutListener {

    private final View root;
    private final View[] bars;

    private KeyboardAwareBottomBar(View root, View[] bars) {
        this.root = root;
        this.bars = bars;
    }

    /** Panggil di onViewCreated; simpan hasilnya lalu panggil {@link #detach()} di onDestroyView. */
    public static KeyboardAwareBottomBar attach(View root, View... bars) {
        KeyboardAwareBottomBar listener = new KeyboardAwareBottomBar(root, bars);
        root.getViewTreeObserver().addOnGlobalLayoutListener(listener);
        return listener;
    }

    public void detach() {
        root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(root);
        int visibility = insets != null && insets.isVisible(WindowInsetsCompat.Type.ime()) ? View.GONE : View.VISIBLE;
        for (View bar : bars) {
            if (bar.getVisibility() != visibility) {
                bar.setVisibility(visibility);
            }
        }
    }
}
