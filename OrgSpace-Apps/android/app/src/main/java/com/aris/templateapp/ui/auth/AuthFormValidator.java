package com.aris.templateapp.ui.auth;

import androidx.annotation.StringRes;

import com.aris.templateapp.R;

import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validasi form masuk & daftar di HP sebelum dikirim, agar user langsung tahu kesalahannya tanpa menunggu server.
 * Aturannya sama dengan backend (password 8–72 karakter, format email). Backend tetap memvalidasi ulang.
 * Tidak memakai kelas Android (mis. Patterns.EMAIL_ADDRESS) agar bisa diuji dengan unit test biasa.
 */
public final class AuthFormValidator {

    public enum Field { NAME, EMAIL, PASSWORD }

    static final int PASSWORD_MIN = 8;
    static final int PASSWORD_MAX = 72;
    // Cukup longgar: ada teks@teks.teks tanpa spasi. Pemeriksaan sesungguhnya dilakukan backend.
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private AuthFormValidator() {
    }

    /** Kosong = valid. Isinya: field → resource teks pesan error. */
    public static Map<Field, Integer> validateLogin(String email, String password) {
        Map<Field, Integer> errors = new EnumMap<>(Field.class);
        putIfInvalid(errors, Field.EMAIL, emailError(email));
        if (password == null || password.isEmpty()) {
            errors.put(Field.PASSWORD, R.string.error_password_required);
        }
        return errors;
    }

    public static Map<Field, Integer> validateRegister(String name, String email, String password) {
        Map<Field, Integer> errors = new EnumMap<>(Field.class);
        if (name == null || name.trim().isEmpty()) {
            errors.put(Field.NAME, R.string.error_name_required);
        }
        putIfInvalid(errors, Field.EMAIL, emailError(email));
        putIfInvalid(errors, Field.PASSWORD, passwordError(password));
        return errors;
    }

    @StringRes
    static Integer emailError(String email) {
        if (email == null || email.trim().isEmpty()) {
            return R.string.error_email_required;
        }
        return EMAIL.matcher(email.trim()).matches() ? null : R.string.error_email_invalid;
    }

    @StringRes
    static Integer passwordError(String password) {
        if (password == null || password.isEmpty()) {
            return R.string.error_password_required;
        }
        if (password.length() < PASSWORD_MIN) {
            return R.string.error_password_length;
        }
        return password.length() > PASSWORD_MAX ? R.string.error_password_too_long : null;
    }

    private static void putIfInvalid(Map<Field, Integer> errors, Field field, Integer error) {
        if (error != null) {
            errors.put(field, error);
        }
    }
}
