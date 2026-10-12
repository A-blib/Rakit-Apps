package com.aris.templateapp.ui.onboarding;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.aris.templateapp.R;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Map;

/** Pembantu kecil untuk form: onboarding, ruang kerja, "Edit profil", dan Info template. */
public final class OnboardingUi {

    private OnboardingUi() {
    }

    public static String text(EditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }

    public static void showError(TextInputLayout input, Integer messageRes) {
        input.setError(messageRes == null ? null : input.getContext().getString(messageRes));
    }

    /** Pesan error di bawah input hilang begitu user mulai memperbaikinya, tidak menunggu tombol ditekan lagi. */
    public static void clearErrorOnType(TextInputLayout... layouts) {
        for (TextInputLayout layout : layouts) {
            EditText input = layout.getEditText();
            if (input == null) {
                continue;
            }
            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (layout.getError() != null) {
                        layout.setError(null);
                    }
                }
            });
        }
    }

    /**
     * Error per field dari backend ditaruh di input yang sesuai; sisanya snackbar.
     * Error koneksi diberi tombol "Coba lagi" yang menjalankan {@code retry}.
     */
    public static void showFailure(View root, ApiError error, Map<String, TextInputLayout> inputsByServerField,
                            Runnable retry) {
        if (error != null) {
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
