package com.aris.templateapp.ui.auth;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.aris.templateapp.R;
import com.aris.templateapp.data.model.LoginMethod;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Dialog penyambungan akun (bagian 6.3): "Email ini sudah terdaftar dengan {metode}. Masuk dengan {metode}
 * untuk menyambungkan akun {metode baru}." Jika akun lama punya beberapa metode, user memilih salah satu.
 * <p>
 * Berupa DialogFragment (bukan dialog biasa) agar tetap tampil saat HP diputar. Datanya diambil dari
 * AuthViewModel milik layar induk (Masuk/Daftar), sehingga tidak perlu dikirim lewat argumen.
 */
public class LinkAccountDialog extends DialogFragment {

    public static final String TAG = "LinkAccountDialog";

    private LinkRequest request;

    public static LinkAccountDialog newInstance() {
        return new LinkAccountDialog();
    }

    /** Dipanggil layar induk sebelum show(); setelah layar dibuat ulang, nilainya dibaca dari ViewModel. */
    void setRequest(LinkRequest request) {
        this.request = request;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AuthViewModel viewModel = new ViewModelProvider(requireParentFragment()).get(AuthViewModel.class);
        if (request == null) {
            request = viewModel.getPendingLinkCandidate();
        }
        if (request == null || request.getExistingMethods().isEmpty()) {
            dismissAllowingStateLoss();
            return new MaterialAlertDialogBuilder(requireContext()).create();
        }
        viewModel.rememberLinkCandidate(request);

        List<LoginMethod> methods = request.getExistingMethods();
        String existing = ErrorMessages.methodLabels(requireContext(),
                methods.stream().map(LoginMethod::value).collect(Collectors.toList()));
        String newMethod = ErrorMessages.methodLabel(requireContext(), request.getNewMethod());

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.link_dialog_title)
                .setNegativeButton(R.string.action_cancel, (d, w) -> viewModel.cancelLink());

        if (methods.size() == 1) {
            LoginMethod method = methods.get(0);
            builder.setMessage(getString(R.string.link_dialog_body, existing, existing, newMethod))
                    .setPositiveButton(getString(R.string.link_dialog_sign_in_with,
                                    ErrorMessages.methodLabel(requireContext(), method)),
                            (d, w) -> viewModel.continueLinkWith(request, method));
        } else {
            // Beberapa metode lama: tampilkan sebagai daftar pilihan.
            String[] labels = new String[methods.size()];
            for (int i = 0; i < methods.size(); i++) {
                labels[i] = getString(R.string.link_dialog_sign_in_with,
                        ErrorMessages.methodLabel(requireContext(), methods.get(i)));
            }
            builder.setItems(labels, (d, which) -> viewModel.continueLinkWith(request, methods.get(which)));
        }
        return builder.create();
    }
}
