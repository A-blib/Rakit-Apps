package com.aris.templateapp.ui.common;

import android.view.View;

import com.aris.templateapp.data.model.User;
import com.aris.templateapp.databinding.ViewAppBarAccountBinding;

import java.util.Locale;

/**
 * Bagian kanan app bar dashboard pembuat website: tombol "Masuk" hanya untuk tamu. Avatar di app bar dihapus
 * (keputusan Aris, 13 Oktober 2026); profil dibuka lewat tab Profil di bottom navigation.
 */
public final class AppBarAccount {

    private AppBarAccount() {
    }

    public static void bind(ViewAppBarAccountBinding binding, User user) {
        binding.signInButton.setVisibility(user != null ? View.GONE : View.VISIBLE);
    }

    public static String initialOf(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        return name.trim().substring(0, 1).toUpperCase(Locale.ROOT);
    }
}
