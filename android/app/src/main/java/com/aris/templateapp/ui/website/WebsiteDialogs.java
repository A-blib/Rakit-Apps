package com.aris.templateapp.ui.website;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;

import com.aris.templateapp.R;
import com.aris.templateapp.data.model.Website;
import com.aris.templateapp.databinding.DialogRenameProjectBinding;
import com.aris.templateapp.ui.onboarding.OnboardingUi;
import com.aris.templateapp.ui.organization.OrgUi;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.function.Consumer;

/** Dialog ganti nama & hapus website, dipakai kartu tab Website dan halaman kelola website. */
public final class WebsiteDialogs {

    private static final int NAME_MAX = 100;

    private WebsiteDialogs() {
    }

    /** Tombol Simpan dipasang sendiri agar dialog tidak tertutup saat nama tidak valid. */
    public static void rename(Context context, Website website, Consumer<String> onSave) {
        DialogRenameProjectBinding binding = DialogRenameProjectBinding.inflate(LayoutInflater.from(context));
        binding.nameInput.setText(website.name);
        binding.nameInput.selectAll();
        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.website_rename_title)
                .setView(binding.getRoot())
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_save, null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = OnboardingUi.text(binding.nameInput).trim();
            if (name.isEmpty()) {
                binding.nameLayout.setError(context.getString(R.string.error_project_name_required));
            } else if (name.length() > NAME_MAX) {
                binding.nameLayout.setError(context.getString(R.string.error_name_too_long));
            } else {
                if (!name.equals(website.name)) {
                    onSave.accept(name);
                }
                dialog.dismiss();
            }
        }));
        dialog.show();
    }

    /**
     * Website yang pernah online wajib mengetik ulang namanya (alur-pembuatan-website.md 4.3); draft cukup
     * konfirmasi biasa. {@code onConfirm} menerima nama ketikan, atau null untuk draft.
     */
    public static void delete(Context context, Website website, Consumer<String> onConfirm) {
        String title = context.getString(R.string.website_delete_title, website.name);
        if (website.wasPublished()) {
            OrgUi.textDialog(context, title, context.getString(R.string.website_delete_published_body),
                    R.string.website_delete_name_hint, null, R.string.project_delete, onConfirm);
            return;
        }
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(R.string.website_delete_body)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.project_delete, (d, which) -> onConfirm.accept(null))
                .show();
    }
}
