package com.aris.templateapp.ui.editor;

import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.DialogFragment;

/**
 * Dialog layar penuh (Kelengkapan, Export, Selesai) tampil seperti layar biasa: latar sampai ke balik status bar dan
 * bar navigasi, warna ikon status bar mengikuti tema terang/gelap, isi tidak tertutup bar sistem.
 * Tanpa ini status bar dialog tampil terang walau app dalam mode gelap.
 */
final class FullScreenDialogs {

    private FullScreenDialogs() {
    }

    /** Dipanggil di {@code onStart()}, saat window dialog sudah ada. */
    static void apply(DialogFragment fragment, View root) {
        Window window = fragment.getDialog() == null ? null : fragment.getDialog().getWindow();
        if (window == null) {
            return;
        }
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        WindowCompat.setDecorFitsSystemWindows(window, false);
        boolean night = (root.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        controller.setAppearanceLightStatusBars(!night);
        controller.setAppearanceLightNavigationBars(!night);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(root);
    }
}
