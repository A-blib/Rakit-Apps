package com.aris.templateapp.ui.provider;

import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.aris.templateapp.R;
import com.aris.templateapp.data.remote.dto.TemplateFolderDto;
import com.aris.templateapp.databinding.DialogTextInputBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/** Dialog folder di "Template Anda": nama folder (buat / ganti nama), konfirmasi hapus, dan pilih folder tujuan. */
final class FolderDialogs {

    /** Sama dengan batas kolom {@code template_folders.name} di backend. */
    private static final int NAME_MAX = 60;

    private FolderDialogs() {
    }

    /**
     * Dialog nama folder. Dialog tidak tertutup selama nama kosong / terlalu panjang, supaya ketikan user
     * tidak hilang (tombol positif dipasang ulang lewat setOnShowListener).
     */
    static void name(Context context, @StringRes int title, @StringRes int confirm, @Nullable String initial,
                     Consumer<String> onSave) {
        DialogTextInputBinding input = DialogTextInputBinding.inflate(LayoutInflater.from(context));
        input.inputLayout.setHint(context.getString(R.string.folder_name_hint));
        input.inputLayout.setCounterEnabled(true);
        input.inputLayout.setCounterMaxLength(NAME_MAX);
        input.input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        if (initial != null) {
            input.input.setText(initial);
            input.input.selectAll();
        }
        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setView(input.getRoot())
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(confirm, null)
                .create();
        dialog.setOnShowListener(d -> {
            input.input.requestFocus();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = input.input.getText() == null ? "" : input.input.getText().toString().trim();
                if (name.isEmpty()) {
                    input.inputLayout.setError(context.getString(R.string.error_folder_name_required));
                } else if (name.length() > NAME_MAX) {
                    input.inputLayout.setError(context.getString(R.string.error_folder_name_too_long));
                } else {
                    if (!name.equals(initial)) {
                        onSave.accept(name);
                    }
                    dialog.dismiss();
                }
            });
        });
        dialog.show();
    }

    static void confirmDelete(Context context, TemplateFolderDto folder, Runnable onConfirm) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.folder_delete_title, folder.name))
                .setMessage(R.string.folder_delete_body)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.folder_delete_confirm, (d, which) -> onConfirm.run())
                .show();
    }

    /**
     * Pilih folder tujuan. Pilihan pertama = "Tanpa folder"; tombol "Folder baru" membuat folder lalu langsung
     * memindahkan template ke sana.
     *
     * @param onPick       menerima folder tujuan, atau null untuk "Tanpa folder"
     * @param onNewFolder  dipanggil saat user memilih "Folder baru"
     */
    static void pickFolder(Context context, List<TemplateFolderDto> folders, @Nullable String currentFolderId,
                           Consumer<TemplateFolderDto> onPick, Runnable onNewFolder) {
        String[] labels = new String[folders.size() + 1];
        labels[0] = context.getString(R.string.folder_move_root);
        int checked = 0;
        for (int i = 0; i < folders.size(); i++) {
            labels[i + 1] = folders.get(i).name;
            if (Objects.equals(folders.get(i).id, currentFolderId)) {
                checked = i + 1;
            }
        }
        int initial = checked;
        int[] selected = {checked};
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.folder_move_title)
                .setSingleChoiceItems(labels, checked, (d, which) -> selected[0] = which)
                .setNeutralButton(R.string.folder_new, (d, which) -> onNewFolder.run())
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.folder_move_action, (d, which) -> {
                    if (selected[0] != initial) {
                        onPick.accept(selected[0] == 0 ? null : folders.get(selected[0] - 1));
                    }
                })
                .show();
    }
}
