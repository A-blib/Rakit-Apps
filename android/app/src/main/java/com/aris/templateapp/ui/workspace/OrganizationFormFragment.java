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

import com.aris.templateapp.databinding.FragmentOrganizationFormBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.common.HomeNavigator;
import com.aris.templateapp.ui.onboarding.OnboardingUi;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Form "Buat ruang organisasi" (revisi-fondasi-orgspace.md 5.1). Pilihan kategori mengikuti jenjang yang dipilih;
 * daftar jenjang & kategori diambil dari backend agar sama dengan galeri dan upload kreator.
 */
@AndroidEntryPoint
public class OrganizationFormFragment extends Fragment {

    private FragmentOrganizationFormBinding binding;
    private WorkspaceSetupViewModel viewModel;
    private CategoryChips chips;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOrganizationFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(WorkspaceSetupViewModel.class);
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        chips = new CategoryChips(binding.levelGroup, binding.categoryGroup, binding.categoryEmpty, () -> {
            if (chips.getLevel() != null) {
                binding.levelError.setVisibility(View.GONE);
            }
            if (chips.getCategory() != null) {
                binding.categoryError.setVisibility(View.GONE);
            }
        });

        viewModel.getCategories().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    binding.content.setVisibility(View.GONE);
                    binding.stateView.showLoading();
                    break;
                case ERROR:
                    binding.content.setVisibility(View.GONE);
                    binding.stateView.showError(ErrorMessages.forError(requireContext(), resource.getError()),
                            viewModel::loadCategories);
                    break;
                case SUCCESS:
                default:
                    binding.stateView.hide(binding.content, () -> chips.bind(resource.getData(), null, null));
                    break;
            }
        });
        viewModel.loadCategories();

        binding.submitButton.setOnClickListener(v -> viewModel.createOrganization(readForm()));
        OnboardingUi.clearErrorOnType(binding.nameLayout, binding.shortNameLayout, binding.institutionLayout,
                binding.cityLayout, binding.periodStartLayout, binding.periodEndLayout, binding.positionLayout);

        viewModel.getFormErrors().observe(getViewLifecycleOwner(), this::showErrors);
        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> binding.submitButton.setEnabled(!loading));
        viewModel.getFailure().observe(getViewLifecycleOwner(), event -> {
            var error = event.getContentIfNotHandled();
            if (error != null) {
                OnboardingUi.showFailure(binding.getRoot(), error, Map.of(
                                "name", binding.nameLayout, "shortName", binding.shortNameLayout,
                                "institutionName", binding.institutionLayout, "city", binding.cityLayout,
                                "periodStartYear", binding.periodStartLayout, "periodEndYear", binding.periodEndLayout,
                                "positionTitle", binding.positionLayout),
                        () -> {
                            if (!viewModel.retryPendingFinish()) {
                                binding.submitButton.performClick();
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

    private OrganizationForm readForm() {
        OrganizationForm form = new OrganizationForm();
        form.name = OnboardingUi.text(binding.nameInput);
        form.shortName = OnboardingUi.text(binding.shortNameInput);
        form.level = chips.getLevel();
        form.category = chips.getCategory();
        form.institution = OnboardingUi.text(binding.institutionInput);
        form.city = OnboardingUi.text(binding.cityInput);
        form.periodStart = OnboardingUi.text(binding.periodStartInput);
        form.periodEnd = OnboardingUi.text(binding.periodEndInput);
        form.position = OnboardingUi.text(binding.positionInput);
        return form;
    }

    private void showErrors(Map<OrganizationForm.Field, Integer> errors) {
        show(binding.nameLayout, errors.get(OrganizationForm.Field.NAME));
        show(binding.shortNameLayout, errors.get(OrganizationForm.Field.SHORT_NAME));
        show(binding.institutionLayout, errors.get(OrganizationForm.Field.INSTITUTION));
        show(binding.cityLayout, errors.get(OrganizationForm.Field.CITY));
        show(binding.periodStartLayout, errors.get(OrganizationForm.Field.PERIOD_START));
        show(binding.periodEndLayout, errors.get(OrganizationForm.Field.PERIOD_END));
        show(binding.positionLayout, errors.get(OrganizationForm.Field.POSITION));
        binding.levelError.setVisibility(errors.containsKey(OrganizationForm.Field.LEVEL) ? View.VISIBLE : View.GONE);
        binding.categoryError.setVisibility(errors.containsKey(OrganizationForm.Field.CATEGORY) ? View.VISIBLE : View.GONE);
    }

    private static void show(TextInputLayout layout, @Nullable Integer message) {
        OnboardingUi.showError(layout, message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
