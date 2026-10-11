package com.aris.templateapp.ui.workspace;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.data.model.Workspace;
import com.aris.templateapp.data.repository.WorkspaceRepository;
import com.aris.templateapp.databinding.ItemWorkspaceBinding;
import com.aris.templateapp.databinding.SheetWorkspacePickerBinding;
import com.aris.templateapp.ui.common.AppBarAccount;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Pemilih ruang kerja dari app bar Dashboard Pembuat Website. Memilih ruang kerja lain langsung berlaku di seluruh
 * app lewat {@link WorkspaceRepository#select}.
 */
@AndroidEntryPoint
public class WorkspacePickerSheet extends BottomSheetDialogFragment {

    public static final String TAG = "WorkspacePickerSheet";

    @Inject
    WorkspaceRepository workspaceRepository;
    @Inject
    ThumbnailLoader thumbnailLoader;

    private SheetWorkspacePickerBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SheetWorkspacePickerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        workspaceRepository.getWorkspaces().observe(getViewLifecycleOwner(), this::bind);
        binding.addItem.setOnClickListener(v -> {
            dismiss();
            NavHostFragment.findNavController(requireParentFragment()).navigate(R.id.workspaceChoiceFragment);
        });
    }

    private void bind(List<Workspace> workspaces) {
        binding.list.removeAllViews();
        Workspace active = workspaceRepository.getActive().getValue();
        for (Workspace workspace : workspaces) {
            ItemWorkspaceBinding row = ItemWorkspaceBinding.inflate(getLayoutInflater(), binding.list, true);
            row.name.setText(workspace.getName());
            row.role.setText(workspace.isPersonal() ? getString(R.string.workspace_personal_badge)
                    : getString(WorkspaceUi.roleLabel(workspace.getRole())));
            row.initial.setText(AppBarAccount.initialOf(workspace.getName()));
            row.logo.setImageDrawable(null);
            thumbnailLoader.load(workspace.getLogoUrl(), row.logo, null);
            boolean selected = active != null && active.getId().equals(workspace.getId());
            row.check.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
            row.getRoot().setSelected(selected);
            row.getRoot().setOnClickListener(v -> {
                if (!selected) {
                    workspaceRepository.select(workspace);
                    View root = requireParentFragment().requireView();
                    Snackbar.make(root, getString(R.string.workspace_switched, workspace.getName()),
                            Snackbar.LENGTH_SHORT).show();
                }
                dismiss();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
