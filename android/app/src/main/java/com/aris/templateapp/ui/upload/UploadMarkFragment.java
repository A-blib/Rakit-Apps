package com.aris.templateapp.ui.upload;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.FragmentUploadMarkBinding;

/** Langkah 4 Upload: Tandai bagian (alur-fitur-upload.md bagian 7). Isi lengkapnya dibangun di Fase 17. */
public class UploadMarkFragment extends Fragment {

    private FragmentUploadMarkBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUploadMarkBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        WizardHeader.bind(binding.header, 4, R.string.upload_step_mark, () -> UploadNav.exit(this));
        binding.backButton.setOnClickListener(v -> UploadNav.replaceStep(this, R.id.uploadInfoFragment,
                requireArguments().getString(UploadNav.ARG_TEMPLATE_ID, "")));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
