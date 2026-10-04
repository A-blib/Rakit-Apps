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

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.FragmentCreatorFormBinding;
import com.aris.templateapp.ui.common.HomeNavigator;

import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Form pembuat website (bagian 6.6): nama tampilan (terisi otomatis), tujuan website (chip), nama organisasi.
 * "Mulai" dan "Lewati" sama-sama menyelesaikan onboarding.
 */
@AndroidEntryPoint
public class CreatorFormFragment extends Fragment {

    private FragmentCreatorFormBinding binding;
    private OnboardingViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCreatorFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(OnboardingViewModel.class);
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            // Isi otomatis hanya jika user belum mengetik apa pun (mis. saat layar dibuat ulang).
            if (user != null && OnboardingUi.text(binding.displayNameInput).isEmpty()) {
                binding.displayNameInput.setText(user.getDisplayName());
            }
        });
        binding.startButton.setOnClickListener(v -> viewModel.submitCreator(
                OnboardingUi.text(binding.displayNameInput), OnboardingUi.purposeValue(binding.purposeGroup.getCheckedChipId()),
                OnboardingUi.text(binding.organizationInput)));
        binding.skipButton.setOnClickListener(v -> viewModel.skipCreator(OnboardingUi.text(binding.displayNameInput)));

        viewModel.getFormErrors().observe(getViewLifecycleOwner(), errors ->
                OnboardingUi.showError(binding.displayNameLayout, errors.get(OnboardingFormValidator.Field.DISPLAY_NAME)));
        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.startButton.setEnabled(!loading);
            binding.skipButton.setEnabled(!loading);
        });
        viewModel.getFailure().observe(getViewLifecycleOwner(), event -> {
            var error = event.getContentIfNotHandled();
            if (error != null) {
                OnboardingUi.showFailure(binding.getRoot(), error,
                        Map.of("displayName", binding.displayNameLayout, "organizationName", binding.organizationLayout),
                        binding.startButton::performClick);
            }
        });
        viewModel.getSuccess().observe(getViewLifecycleOwner(), event -> {
            var user = event.getContentIfNotHandled();
            if (user != null) {
                HomeNavigator.navigateHome(this, user);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
