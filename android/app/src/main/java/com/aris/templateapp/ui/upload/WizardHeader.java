package com.aris.templateapp.ui.upload;

import androidx.annotation.StringRes;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.ViewWizardHeaderBinding;

/** Mengisi zona atas wizard: judul langkah, "Langkah X dari 6", garis kemajuan, dan tombol ✕. */
final class WizardHeader {

    private WizardHeader() {
    }

    static void bind(ViewWizardHeaderBinding header, int step, @StringRes int title, Runnable onClose) {
        header.title.setText(title);
        header.step.setText(header.getRoot().getContext().getString(R.string.upload_step_of, step));
        header.stepBar.setProgress(step);
        header.closeButton.setOnClickListener(v -> onClose.run());
    }

    /** Nama langkah untuk kartu draft, mis. "Tandai". */
    @StringRes
    static int stepName(int step) {
        switch (step) {
            case 1:
                return R.string.upload_step_pick;
            case 2:
                return R.string.upload_step_check;
            case 4:
                return R.string.upload_step_mark;
            case 5:
                return R.string.upload_step_try;
            case 6:
                return R.string.upload_step_send;
            case 3:
            default:
                return R.string.upload_step_info;
        }
    }
}
