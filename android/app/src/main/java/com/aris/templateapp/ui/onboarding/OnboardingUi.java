package com.aris.templateapp.ui.onboarding;

import android.view.View;
import android.widget.EditText;

import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Map;

/** Pembantu kecil yang dipakai kedua form onboarding. */
final class OnboardingUi {

    private OnboardingUi() {
    }

    static String text(EditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }

    static void showError(TextInputLayout input, Integer messageRes) {
        input.setError(messageRes == null ? null : input.getContext().getString(messageRes));
    }

    /** Error per field dari backend ditaruh di input yang sesuai; sisanya snackbar. */
    static void showFailure(View root, ApiError error, Map<String, TextInputLayout> inputsByServerField) {
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
        Snackbar.make(root, ErrorMessages.forError(root.getContext(), error), Snackbar.LENGTH_LONG).show();
    }
}
