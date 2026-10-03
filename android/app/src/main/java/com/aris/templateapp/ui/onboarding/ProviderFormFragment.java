package com.aris.templateapp.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.databinding.FragmentProviderFormBinding;
import com.aris.templateapp.ui.common.HomeNavigator;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Form penyedia template (bagian 6.6): nama kreator (wajib), bio (maks 300), link portofolio (URL valid),
 * keahlian (chip), dan persetujuan aturan (wajib). Dipakai dari onboarding maupun dari menu
 * "Jadi penyedia template" milik user lama. Status awal pending.
 */
@AndroidEntryPoint
public class ProviderFormFragment extends Fragment {

    private FragmentProviderFormBinding binding;
    private OnboardingViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProviderFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(OnboardingViewModel.class);
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && OnboardingUi.text(binding.creatorNameInput).isEmpty()) {
                binding.creatorNameInput.setText(user.getDisplayName());
            }
        });
        // Pesan "harus menyetujui" hilang begitu checkbox dicentang, tanpa menunggu tombol kirim ditekan lagi.
        binding.termsCheckbox.setOnCheckedChangeListener((button, checked) -> {
            if (checked) {
                binding.termsError.setVisibility(View.GONE);
            }
        });
        binding.submitButton.setOnClickListener(v -> viewModel.submitProvider(
                OnboardingUi.text(binding.creatorNameInput),
                OnboardingUi.text(binding.bioInput),
                OnboardingUi.text(binding.portfolioInput),
                selectedSpecialties(),
                binding.termsCheckbox.isChecked()));

        viewModel.getFormErrors().observe(getViewLifecycleOwner(), errors -> {
            OnboardingUi.showError(binding.creatorNameLayout, errors.get(OnboardingFormValidator.Field.CREATOR_NAME));
            OnboardingUi.showError(binding.bioLayout, errors.get(OnboardingFormValidator.Field.BIO));
            OnboardingUi.showError(binding.portfolioLayout, errors.get(OnboardingFormValidator.Field.PORTFOLIO_URL));
            Integer terms = errors.get(OnboardingFormValidator.Field.TERMS);
            binding.termsError.setVisibility(terms == null ? View.GONE : View.VISIBLE);
        });
        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> binding.submitButton.setEnabled(!loading));
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
        viewModel.getSuccess().observe(getViewLifecycleOwner(), event -> {
            var user = event.getContentIfNotHandled();
            if (user != null) {
                HomeNavigator.navigateHome(this, user);
            }
        });
    }

    /** Teks chip yang dipilih dikirim apa adanya sebagai daftar keahlian. */
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
