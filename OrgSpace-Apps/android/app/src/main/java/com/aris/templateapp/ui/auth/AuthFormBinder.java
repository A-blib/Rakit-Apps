package com.aris.templateapp.ui.auth;

import android.view.View;

import com.aris.templateapp.R;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.AuthResult;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Map;

/** Bagian yang sama di layar Masuk & Daftar: menampilkan error field dan error request. */
final class AuthFormBinder {

    private AuthFormBinder() {
    }

    static void showFieldError(TextInputLayout input, Integer messageRes) {
        input.setError(messageRes == null ? null : input.getContext().getString(messageRes));
    }

    /**
     * Error dari server: error per field (VALIDATION_ERROR) ditaruh di input yang sesuai;
     * lainnya ditampilkan sebagai snackbar. Error koneksi diberi tombol "Coba lagi".
     */
    static void showFailure(View root, Resource<AuthResult> failure, Map<String, TextInputLayout> inputsByServerField,
                            Runnable retry) {
        ApiError error = failure.getError();
        if (error != null && !error.getFieldErrors().isEmpty()) {
            boolean shown = false;
            for (Map.Entry<String, String> entry : error.getFieldErrors().entrySet()) {
                TextInputLayout input = inputsByServerField.get(entry.getKey());
                if (input != null) {
                    input.setError(entry.getValue());
                    shown = true;
                }
            }
            if (shown) {
                return;
            }
        }
        Snackbar snackbar = Snackbar.make(root, ErrorMessages.forError(root.getContext(), error), Snackbar.LENGTH_LONG);
        if (error != null && error.isNetworkError()) {
            snackbar.setDuration(Snackbar.LENGTH_INDEFINITE).setAction(R.string.action_retry, v -> retry.run());
        }
        snackbar.show();
    }
}
