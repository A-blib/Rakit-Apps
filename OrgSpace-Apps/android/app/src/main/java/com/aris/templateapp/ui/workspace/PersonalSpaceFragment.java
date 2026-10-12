package com.aris.templateapp.ui.workspace;

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
import com.aris.templateapp.databinding.FragmentPersonalSpaceBinding;
import com.aris.templateapp.ui.common.HomeNavigator;
import com.aris.templateapp.ui.onboarding.OnboardingUi;

import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

/** "Untuk pribadi": nama tampilan (terisi otomatis) → ruang pribadi dibuat (revisi-fondasi-orgspace.md bagian 5). */
@AndroidEntryPoint
public class PersonalSpaceFragment extends Fragment {

    private static final int NAME_MAX = 100;

    private FragmentPersonalSpaceBinding binding;
    private WorkspaceSetupViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPersonalSpaceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(WorkspaceSetupViewModel.class);
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && OnboardingUi.text(binding.displayNameInput).isEmpty()) {
                binding.displayNameInput.setText(user.getDisplayName());
            }
        });
        binding.submitButton.setOnClickListener(v -> submit());
        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> binding.submitButton.setEnabled(!loading));
        viewModel.getFailure().observe(getViewLifecycleOwner(), event -> {
            var error = event.getContentIfNotHandled();
            if (error != null) {
                OnboardingUi.showFailure(binding.getRoot(), error,
                        Map.of("name", binding.displayNameLayout, "displayName", binding.displayNameLayout), () -> {
                            if (!viewModel.retryPendingFinish()) {
                                submit();
                            }
                        });
            }
        });
        viewModel.getDone().observe(getViewLifecycleOwner(), event -> {
            var user = event.getContentIfNotHandled();
            if (user != null) {
                HomeNavigator.navigateHome(this, user);
            }
        });
    }

    private void submit() {
        String name = OnboardingUi.text(binding.displayNameInput).trim();
        if (name.isEmpty()) {
            OnboardingUi.showError(binding.displayNameLayout, R.string.error_display_name_required);
        } else if (name.length() > NAME_MAX) {
            OnboardingUi.showError(binding.displayNameLayout, R.string.error_name_too_long);
        } else {
            OnboardingUi.showError(binding.displayNameLayout, null);
            viewModel.createPersonal(name);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
