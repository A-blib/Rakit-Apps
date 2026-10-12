package com.aris.templateapp.ui.provider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.remote.dto.ProviderProfileDto;
import com.aris.templateapp.databinding.FragmentProviderFormBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.onboarding.OnboardingFormValidator;
import com.aris.templateapp.ui.onboarding.OnboardingUi;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * "Edit profil" provider (alur-provider.md bagian 7): memakai layout form provider onboarding yang sama,
 * dengan judul "Edit profil", tanpa persetujuan aturan, dan tombol "Simpan".
 */
@AndroidEntryPoint
public class ProviderProfileEditFragment extends Fragment {

    private FragmentProviderFormBinding binding;
    private ProviderProfileEditViewModel viewModel;
    /** Isi awal hanya dipasang sekali; setelah itu yang diketik user tidak boleh tertimpa (mis. layar diputar). */
    private boolean prefilled;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProviderFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        prefilled = savedInstanceState != null;
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.formTitle.setText(R.string.profile_edit_title);
        binding.formSubtitle.setText(R.string.profile_edit_subtitle);
        binding.termsCheckbox.setVisibility(View.GONE);
        binding.submitButton.setText(R.string.action_save);

        viewModel = new ViewModelProvider(this).get(ProviderProfileEditViewModel.class);
        viewModel.getInitial().observe(getViewLifecycleOwner(), this::prefill);
        binding.submitButton.setOnClickListener(v -> viewModel.save(
                OnboardingUi.text(binding.creatorNameInput),
                OnboardingUi.text(binding.bioInput),
                OnboardingUi.text(binding.portfolioInput),
                selectedSpecialties()));

        viewModel.getFormErrors().observe(getViewLifecycleOwner(), errors -> {
            OnboardingUi.showError(binding.creatorNameLayout, errors.get(OnboardingFormValidator.Field.CREATOR_NAME));
            OnboardingUi.showError(binding.bioLayout, errors.get(OnboardingFormValidator.Field.BIO));
            OnboardingUi.showError(binding.portfolioLayout, errors.get(OnboardingFormValidator.Field.PORTFOLIO_URL));
        });
        viewModel.isSaving().observe(getViewLifecycleOwner(), saving -> binding.submitButton.setEnabled(!saving));
        viewModel.getFailure().observe(getViewLifecycleOwner(), event -> {
            var error = event.getContentIfNotHandled();
            if (error != null) {
                OnboardingUi.showFailure(binding.getRoot(), error, Map.of(
                        "creatorName", binding.creatorNameLayout,
                        "bio", binding.bioLayout,
                        "portfolioUrl", binding.portfolioLayout),
                        binding.submitButton::performClick);
            }
        });
        viewModel.getSaved().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() != null) {
                View root = requireActivity().findViewById(android.R.id.content);
                NavHostFragment.findNavController(this).navigateUp();
                Snackbar.make(root, R.string.profile_saved, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void prefill(@Nullable Resource<ProviderProfileDto> resource) {
        if (resource == null || prefilled) {
            return;
        }
        binding.submitButton.setEnabled(resource.getStatus() == Resource.Status.SUCCESS);
        if (resource.getStatus() == Resource.Status.ERROR) {
            Snackbar.make(binding.getRoot(), ErrorMessages.forError(requireContext(), resource.getError()),
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.action_retry, v -> viewModel.loadInitial()).show();
            return;
        }
        if (resource.getStatus() != Resource.Status.SUCCESS) {
            return;
        }
        prefilled = true;
        ProviderProfileDto profile = resource.getData();
        binding.creatorNameInput.setText(profile.creatorName);
        binding.bioInput.setText(profile.bio);
        binding.portfolioInput.setText(profile.portfolioUrl);

        // Centang chip yang cocok; keahlian lain (mis. diketik di versi berikutnya) ditambahkan sebagai chip baru
        // agar tidak hilang saat disimpan.
        Set<String> remaining = new HashSet<>(profile.specialties == null ? List.of() : profile.specialties);
        for (int i = 0; i < binding.specialtyGroup.getChildCount(); i++) {
            Chip chip = (Chip) binding.specialtyGroup.getChildAt(i);
            boolean selected = remaining.remove(chip.getText().toString());
            chip.setChecked(selected);
        }
        for (String extra : remaining) {
            Chip chip = new Chip(requireContext());
            chip.setText(extra);
            chip.setCheckable(true);
            chip.setChecked(true);
            binding.specialtyGroup.addView(chip);
        }
    }

    private List<String> selectedSpecialties() {
        List<String> selected = new ArrayList<>();
        for (int id : binding.specialtyGroup.getCheckedChipIds()) {
            Chip chip = binding.specialtyGroup.findViewById(id);
            selected.add(chip.getText().toString());
        }
        return selected;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
