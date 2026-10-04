package com.aris.templateapp.ui.provider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aris.templateapp.databinding.FragmentProviderUploadBinding;
import com.aris.templateapp.ui.common.LottieTint;

/** Tab Upload versi awal: "Segera hadir" + tombol ke panduan menyiapkan template (alur-provider.md bagian 5.0). */
public class ProviderUploadFragment extends Fragment {

    private FragmentProviderUploadBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProviderUploadBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LottieTint.applyForeground(binding.illustration);
        binding.openGuideButton.setOnClickListener(v -> ProviderNav.openGuide(this, Guide.PREPARE_TEMPLATE));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
