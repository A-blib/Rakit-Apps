package com.aris.templateapp.ui.common;

import android.view.View;

import com.aris.templateapp.data.model.User;
import com.aris.templateapp.databinding.ViewAppBarAccountBinding;

import java.util.Locale;

/**
 * Bagian kanan app bar dashboard: tombol "Masuk" untuk tamu, atau avatar (huruf depan nama) untuk user login.
 */
public final class AppBarAccount {

    private AppBarAccount() {
    }

    public static void bind(ViewAppBarAccountBinding binding, User user) {
        boolean loggedIn = user != null;
        binding.signInButton.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        binding.avatarContainer.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        if (loggedIn) {
            binding.avatarInitial.setText(initialOf(user.getDisplayName()));
        }
    }

    static String initialOf(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        return name.trim().substring(0, 1).toUpperCase(Locale.ROOT);
    }
}
