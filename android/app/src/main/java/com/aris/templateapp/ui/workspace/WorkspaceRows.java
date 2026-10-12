package com.aris.templateapp.ui.workspace;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.data.model.Workspace;
import com.aris.templateapp.databinding.ItemWorkspaceBinding;
import com.aris.templateapp.ui.common.AppBarAccount;

import java.util.List;
import java.util.function.Consumer;

/**
 * Daftar ruang kerja (logo/huruf depan, nama, peran, ✓ untuk yang aktif). Dipakai pemilih di app bar dan bagian
 * "Ruang kerja saya" di tab Profil, agar keduanya selalu tampil sama.
 */
public final class WorkspaceRows {

    private WorkspaceRows() {
    }

    public static void bind(ViewGroup container, List<Workspace> workspaces, @Nullable Workspace active,
                            ThumbnailLoader loader, Consumer<Workspace> onClick) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        for (Workspace workspace : workspaces) {
            ItemWorkspaceBinding row = ItemWorkspaceBinding.inflate(inflater, container, true);
            row.name.setText(workspace.getName());
            row.role.setText(workspace.isPersonal() ? R.string.workspace_personal_badge
                    : WorkspaceUi.roleLabel(workspace.getRole()));
            row.initial.setText(AppBarAccount.initialOf(workspace.getName()));
            row.initial.setVisibility(View.VISIBLE);
            row.logo.setImageDrawable(null);
            loader.load(workspace.getLogoUrl(), row.logo, () -> row.initial.setVisibility(View.INVISIBLE));
            boolean selected = active != null && active.getId().equals(workspace.getId());
            row.check.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
            row.getRoot().setSelected(selected);
            row.getRoot().setOnClickListener(v -> onClick.accept(workspace));
        }
    }
}
