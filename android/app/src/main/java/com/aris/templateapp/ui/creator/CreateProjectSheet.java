package com.aris.templateapp.ui.creator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.SheetCreateProjectBinding;
import com.aris.templateapp.ui.editor.EditorNav;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Bottom sheet tombol + dan kartu "Mulai" (alur-pembuatan-website.md bagian 2 & 6): "Pakai template" membuka tab
 * Template, "Custom" membuka Editor Custom Mode untuk project baru. Selalu ditampilkan oleh shell
 * ({@link CreatorDashboardFragment#showCreateSheet()}), jadi induknya selalu shell.
 */
public class CreateProjectSheet extends BottomSheetDialogFragment {

    public static final String TAG = "CreateProjectSheet";

    private SheetCreateProjectBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SheetCreateProjectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        StartOptions.bind(binding.optionTemplate, binding.optionCustom);
        binding.optionTemplate.getRoot().setOnClickListener(v -> {
            CreatorDashboardFragment shell = (CreatorDashboardFragment) requireParentFragment();
            dismiss();
            shell.selectTab(R.id.tab_creator_gallery);
        });
        binding.optionCustom.getRoot().setOnClickListener(v -> {
            CreatorDashboardFragment shell = (CreatorDashboardFragment) requireParentFragment();
            dismiss();
            EditorNav.openCustomEditor(shell, null);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
