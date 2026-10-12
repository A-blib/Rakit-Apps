package com.aris.templateapp.ui.creator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aris.templateapp.databinding.SheetCreateProjectBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Bottom sheet tombol + dan kartu "Mulai" (alur-pembuatan-website.md bagian 2.3): "Pakai template" membuka tab
 * Template, "Rakit sendiri" membuka editor Rakit sendiri ("Segera hadir", belum membuat website). Pilihan diteruskan
 * ke shell ({@link CreatorDashboardFragment#start}) yang meminta tamu masuk dulu.
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
        binding.optionTemplate.getRoot().setOnClickListener(v -> choose(PendingStart.Option.TEMPLATE));
        binding.optionCustom.getRoot().setOnClickListener(v -> choose(PendingStart.Option.ASSEMBLED));
    }

    private void choose(PendingStart.Option option) {
        CreatorDashboardFragment shell = (CreatorDashboardFragment) requireParentFragment();
        dismiss();
        shell.start(option);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
