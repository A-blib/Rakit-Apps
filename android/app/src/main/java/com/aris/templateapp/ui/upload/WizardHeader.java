package com.aris.templateapp.ui.upload;

import androidx.annotation.StringRes;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.ViewWizardHeaderBinding;

/** Mengisi zona atas wizard: judul langkah, "Langkah X dari 6", garis kemajuan, dan tombol ✕. */
public final class WizardHeader {

    private WizardHeader() {
    }

    static void bind(ViewWizardHeaderBinding header, int step, @StringRes int title, Runnable onClose) {
        header.title.setText(title);
        // Teks pendek "5/6" (rancangan bagian 3.3) agar judul langkah tidak terpotong; pembaca layar membacakan
        // kalimat lengkapnya.
        header.step.setText(header.getRoot().getContext().getString(R.string.upload_step_short, step));
        header.step.setContentDescription(header.getRoot().getContext().getString(R.string.upload_step_of, step));
        header.stepBar.setProgress(step);
        header.closeButton.setOnClickListener(v -> onClose.run());
    }

    /** Nama langkah untuk kartu draft, mis. "Tandai". */
    @StringRes
    public static int stepName(int step) {
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
