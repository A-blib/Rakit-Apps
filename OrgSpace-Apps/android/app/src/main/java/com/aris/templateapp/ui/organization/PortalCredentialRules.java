package com.aris.templateapp.ui.organization;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.aris.templateapp.R;

import java.util.regex.Pattern;

/** Aturan isian kredensial Portal, sama dengan PortalCredentialRequest di backend, agar salah ketik tampil langsung. */
final class PortalCredentialRules {

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._-]{3,40}$");

    private PortalCredentialRules() {
    }

    @Nullable
    @StringRes
    static Integer usernameError(String username) {
        return USERNAME.matcher(username.strip()).matches() ? null : R.string.portal_error_username;
    }

    @Nullable
    @StringRes
    static Integer passwordError(String password) {
        // BCrypt di backend hanya membaca 72 byte; huruf non-ASCII bisa lebih dari 1 byte, server tetap memeriksa ulang.
        return password.length() >= 8 && password.length() <= 72 ? null : R.string.portal_error_password;
    }
}
