package com.aris.templateapp.ui.profile;

import android.view.View;

import androidx.fragment.app.Fragment;

import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.databinding.ViewAvatarEditBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

/**
 * Menghubungkan tampilan {@code view_avatar_edit.xml} dengan {@link AvatarEditViewModel}. Dipakai layar Edit profil
 * pembuat website dan provider supaya perilakunya sama persis.
 * <p>
 * Pemilih foto (Photo Picker) tetap didaftarkan di Fragment masing-masing, karena
 * {@code registerForActivityResult} harus dipanggil saat Fragment dibuat, bukan saat view-nya dibuat.
 */
public final class AvatarEditSection {

    private AvatarEditSection() {
    }

    public static void attach(Fragment fragment, ViewAvatarEditBinding b, AvatarEditViewModel viewModel,
                              ThumbnailLoader loader, Runnable pickPhoto) {
        b.changeButton.setOnClickListener(v -> pickPhoto.run());
        b.photo.setOnClickListener(v -> pickPhoto.run());
        b.removeButton.setOnClickListener(v -> new MaterialAlertDialogBuilder(fragment.requireContext())
                .setTitle(R.string.avatar_remove_title)
                .setMessage(R.string.avatar_remove_body)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.avatar_remove, (d, which) -> viewModel.remove())
                .show());

        viewModel.getUser().observe(fragment.getViewLifecycleOwner(), user -> bindUser(fragment, b, loader, user));
        viewModel.isBusy().observe(fragment.getViewLifecycleOwner(), busy -> {
            b.progress.setVisibility(busy ? View.VISIBLE : View.GONE);
            b.changeButton.setEnabled(!busy);
            b.removeButton.setEnabled(!busy);
        });
        viewModel.getMessage().observe(fragment.getViewLifecycleOwner(), event -> {
            Integer text = event.getContentIfNotHandled();
            if (text != null) {
                Snackbar.make(b.getRoot(), text, Snackbar.LENGTH_SHORT).show();
            }
        });
        viewModel.getFailure().observe(fragment.getViewLifecycleOwner(), event -> {
            ApiError error = event.getContentIfNotHandled();
            if (error != null) {
                Snackbar.make(b.getRoot(), ErrorMessages.forError(fragment.requireContext(), error),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private static void bindUser(Fragment fragment, ViewAvatarEditBinding b, ThumbnailLoader loader, User user) {
        String name = user == null ? null : user.getDisplayName();
        String url = user == null ? null : user.getAvatarUrl();
        AvatarViews.bind(b.initial, b.photo, loader, name, url);
        b.photo.setContentDescription(fragment.getString(R.string.cd_profile_avatar, name == null ? "" : name));
        // "Hapus foto" hanya berguna jika ada foto.
        b.removeButton.setVisibility(url == null ? View.GONE : View.VISIBLE);
    }
}
