package com.aris.templateapp.ui.upload;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.FragmentUploadTryBinding;

/** Langkah 5 Upload: Coba sebagai pengguna (alur-fitur-upload.md bagian 8). Isi lengkapnya dibangun di Fase 18. */
public class UploadTryFragment extends Fragment {

    private FragmentUploadTryBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUploadTryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        WizardHeader.bind(binding.header, 5, R.string.upload_step_try, () -> UploadNav.exit(this));
        binding.backButton.setOnClickListener(v -> UploadNav.replaceStep(this, R.id.uploadMarkFragment,
                requireArguments().getString(UploadNav.ARG_TEMPLATE_ID, "")));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
