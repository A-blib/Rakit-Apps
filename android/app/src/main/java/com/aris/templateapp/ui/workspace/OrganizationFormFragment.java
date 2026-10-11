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

import com.aris.templateapp.data.model.OrgCategories;
import com.aris.templateapp.databinding.FragmentOrganizationFormBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.common.HomeNavigator;
import com.aris.templateapp.ui.onboarding.OnboardingUi;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
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
    @Nullable private OrgCategories categories;
    @Nullable private String selectedLevel;
    @Nullable private String selectedCategory;

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
                    binding.stateView.hide(binding.content, () -> bindCategories(resource.getData()));
                    break;
            }
        });
        viewModel.loadCategories();

        binding.levelGroup.setOnCheckedStateChangeListener((group, ids) -> {
            selectedLevel = checkedValue(group, ids);
            binding.levelError.setVisibility(View.GONE);
            showCategories();
        });
        binding.categoryGroup.setOnCheckedStateChangeListener((group, ids) -> {
            selectedCategory = checkedValue(group, ids);
            binding.categoryError.setVisibility(View.GONE);
        });
        binding.submitButton.setOnClickListener(v -> viewModel.createOrganization(readForm()));

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

    private void bindCategories(OrgCategories data) {
        if (categories != null) {
            return;
        }
        categories = data;
        binding.levelGroup.removeAllViews();
        for (OrgCategories.Option level : data.getLevels()) {
            binding.levelGroup.addView(chip(level));
        }
        showCategories();
    }

    /** Kategori dibangun ulang setiap jenjang berganti; pilihan lama dipertahankan jika masih tersedia. */
    private void showCategories() {
        if (categories == null) {
            return;
        }
        List<OrgCategories.Category> options = categories.categoriesFor(selectedLevel);
        String keep = selectedCategory;
        selectedCategory = null;
        binding.categoryGroup.removeAllViews();
        for (OrgCategories.Category category : options) {
            Chip chip = chip(category);
            binding.categoryGroup.addView(chip);
            if (category.value.equals(keep)) {
                chip.setChecked(true);
            }
        }
        binding.categoryEmpty.setVisibility(options.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private Chip chip(OrgCategories.Option option) {
        Chip chip = new Chip(requireContext());
        chip.setText(option.label);
        chip.setTag(option.value);
        chip.setCheckable(true);
        chip.setId(View.generateViewId());
        return chip;
    }

    @Nullable
    private String checkedValue(ChipGroup group, List<Integer> ids) {
        if (ids.isEmpty()) {
            return null;
        }
        View chip = group.findViewById(ids.get(0));
        return chip == null ? null : (String) chip.getTag();
    }

    private OrganizationForm readForm() {
        OrganizationForm form = new OrganizationForm();
        form.name = OnboardingUi.text(binding.nameInput);
        form.shortName = OnboardingUi.text(binding.shortNameInput);
        form.level = selectedLevel;
        form.category = selectedCategory;
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
        categories = null;
    }
}
