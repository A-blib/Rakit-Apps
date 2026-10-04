package com.aris.templateapp.ui.onboarding;

import android.view.View;
import android.widget.EditText;

import com.aris.templateapp.R;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Map;

/** Pembantu kecil yang dipakai kedua form onboarding dan layar "Edit profil" (provider & pembuat website). */
public final class OnboardingUi {

    /** Chip tujuan website di fragment_creator_form.xml dan nilai backend-nya, urutan sama. */
    private static final int[] PURPOSE_CHIPS = {R.id.purpose_sekolah, R.id.purpose_organisasi, R.id.purpose_umkm,
            R.id.purpose_instansi, R.id.purpose_pribadi, R.id.purpose_lainnya};
    private static final String[] PURPOSE_VALUES = {"sekolah", "organisasi", "umkm", "instansi", "pribadi", "lainnya"};

    private OnboardingUi() {
    }

    /** Nilai backend untuk chip tujuan website, atau null jika tidak ada yang dipilih (View.NO_ID). */
    public static String purposeValue(int chipId) {
        for (int i = 0; i < PURPOSE_CHIPS.length; i++) {
            if (PURPOSE_CHIPS[i] == chipId) {
                return PURPOSE_VALUES[i];
            }
        }
        return null;
    }

    /** Kebalikan {@link #purposeValue}: id chip untuk nilai backend, atau View.NO_ID. */
    public static int purposeChipId(String value) {
        for (int i = 0; i < PURPOSE_VALUES.length; i++) {
            if (PURPOSE_VALUES[i].equals(value)) {
                return PURPOSE_CHIPS[i];
            }
        }
        return View.NO_ID;
    }

    public static String text(EditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }

    public static void showError(TextInputLayout input, Integer messageRes) {
        input.setError(messageRes == null ? null : input.getContext().getString(messageRes));
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
