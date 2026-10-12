package com.aris.templateapp.ui.upload;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.DialogReportIssueBinding;
import com.aris.templateapp.ui.onboarding.OnboardingUi;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** Dialog "Ini keliru? Laporkan" (bagian 5.9). Alasan opsional; null jika dikosongkan. */
public final class ReportIssueDialog {

    public interface Send {
        void send(@Nullable String reason);
    }

    private ReportIssueDialog() {
    }

    public static void show(Fragment fragment, Send send) {
        DialogReportIssueBinding binding = DialogReportIssueBinding.inflate(fragment.getLayoutInflater());
        new MaterialAlertDialogBuilder(fragment.requireContext())
                .setTitle(R.string.report_title)
                .setView(binding.getRoot())
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.report_send, (d, w) -> {
                    String reason = OnboardingUi.text(binding.reasonInput).trim();
                    send.send(reason.isEmpty() ? null : reason);
                })
                .show();
    }
}
