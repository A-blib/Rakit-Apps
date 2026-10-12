package com.aris.templateapp.ui.workspace;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.data.model.Workspace;
import com.aris.templateapp.data.repository.WorkspaceRepository;
import com.aris.templateapp.databinding.SheetWorkspacePickerBinding;
import com.aris.templateapp.ui.organization.OrganizationArgs;
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
        binding.manageItem.setOnClickListener(v -> {
            Workspace active = workspaceRepository.getActive().getValue();
            if (active != null) {
                dismiss();
                NavHostFragment.findNavController(requireParentFragment())
                        .navigate(R.id.organizationFragment, OrganizationArgs.of(active.getId()));
            }
        });
        binding.createItem.setOnClickListener(v -> open(R.id.organizationFormFragment));
        binding.joinItem.setOnClickListener(v -> open(R.id.joinOrganizationFragment));
        binding.personalItem.setOnClickListener(v -> open(R.id.personalSpaceFragment));
    }

    /** Form buat/gabung/ruang pribadi langsung, tanpa layar pilihan; setelah selesai kembali ke dashboard. */
    private void open(@IdRes int destination) {
        dismiss();
        NavHostFragment.findNavController(requireParentFragment()).navigate(destination);
    }

    private void bind(List<Workspace> workspaces) {
        Workspace active = workspaceRepository.getActive().getValue();
        // Ruang pribadi juga punya layar kelola (profil, riwayat, hapus), hanya labelnya berbeda.
        binding.manageItem.setText(active != null && active.isPersonal() ? R.string.workspace_picker_manage_personal
                : R.string.workspace_picker_manage);
        WorkspaceRows.bind(binding.list, workspaces, active, thumbnailLoader, workspace -> {
            if (active == null || !active.getId().equals(workspace.getId())) {
                workspaceRepository.select(workspace);
                Snackbar.make(requireParentFragment().requireView(),
                        getString(R.string.workspace_switched, workspace.getName()), Snackbar.LENGTH_SHORT).show();
            }
            dismiss();
        });
        boolean hasPersonal = false;
        for (Workspace workspace : workspaces) {
            hasPersonal |= workspace.isPersonal();
        }
        binding.personalItem.setVisibility(hasPersonal ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
