package com.aris.templateapp.ui.organization;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.data.model.OrgCategories;
import com.aris.templateapp.data.model.Organization;
import com.aris.templateapp.databinding.FragmentOrganizationProfileBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.common.StatusBannerView;
import com.aris.templateapp.ui.onboarding.OnboardingUi;
import com.aris.templateapp.ui.workspace.CategoryChips;
import com.aris.templateapp.ui.workspace.OrganizationForm;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Profil organisasi: diketik sekali, lalu (nanti) mengisi otomatis kolom template (K10). Semua anggota bisa melihat;
 * hanya Admin & Pemilik yang bisa mengubah (bagian 3.1). Backend tetap menolak perubahan dari Editor.
 */
@AndroidEntryPoint
public class OrganizationProfileFragment extends Fragment {

    @Inject
    ThumbnailLoader thumbnailLoader;

    private FragmentOrganizationProfileBinding binding;
    private OrganizationProfileViewModel viewModel;
    private CategoryChips chips;
    private boolean filled;
    private boolean personal;

    private final ActivityResultLauncher<PickVisualMediaRequest> picker = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    viewModel.uploadLogo(uri);
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOrganizationProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        filled = savedInstanceState != null;
        viewModel = new ViewModelProvider(this).get(OrganizationProfileViewModel.class);
        viewModel.start(OrganizationArgs.id(this));
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        chips = new CategoryChips(binding.levelGroup, binding.categoryGroup, null, () -> {
            binding.levelError.setVisibility(View.GONE);
            binding.categoryError.setVisibility(View.GONE);
        });

        viewModel.getOrganization().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    binding.stateView.showLoading();
                    break;
                case ERROR:
                    binding.content.setVisibility(View.GONE);
                    binding.stateView.showError(ErrorMessages.forError(requireContext(), resource.getError()),
                            viewModel::load);
                    break;
                case SUCCESS:
                default:
                    binding.stateView.hide(binding.content, () -> bind(resource.getData()));
                    break;
            }
        });
        binding.logoButton.setOnClickListener(v -> picker.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));
        binding.saveButton.setOnClickListener(v -> viewModel.save(readForm(), readContact()));

        viewModel.getFormErrors().observe(getViewLifecycleOwner(), this::showErrors);
        viewModel.isBusy().observe(getViewLifecycleOwner(), busy -> {
            binding.saveButton.setEnabled(!busy);
            binding.logoButton.setEnabled(!busy);
        });
        viewModel.getImageError().observe(getViewLifecycleOwner(), event -> {
            Integer message = event.getContentIfNotHandled();
            if (message != null) {
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
            }
        });
        viewModel.getFailure().observe(getViewLifecycleOwner(), event -> {
            var error = event.getContentIfNotHandled();
            if (error != null) {
                OnboardingUi.showFailure(binding.getRoot(), error, Map.of(
                        "name", binding.nameLayout, "shortName", binding.shortNameLayout,
                        "institutionName", binding.institutionLayout, "city", binding.cityLayout,
                        "periodEndYear", binding.periodEndLayout, "email", binding.emailLayout,
                        "address", binding.addressLayout), binding.saveButton::performClick);
            }
        });
        viewModel.getSaved().observe(getViewLifecycleOwner(), event -> {
            var what = event.getContentIfNotHandled();
            if (what == OrganizationProfileViewModel.Saved.PROFILE) {
                View root = requireActivity().findViewById(android.R.id.content);
                NavHostFragment.findNavController(this).navigateUp();
                Snackbar.make(root, R.string.org_profile_saved, Snackbar.LENGTH_SHORT).show();
            } else if (what == OrganizationProfileViewModel.Saved.LOGO) {
                Snackbar.make(binding.getRoot(), R.string.org_logo_saved, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void bind(Organization org) {
        OrgUi.bindLogo(binding.logo, thumbnailLoader, org.name, org.logoUrl);
        personal = org.personal;
        binding.orgOnlySection.setVisibility(org.personal ? View.GONE : View.VISIBLE);
        OrgCategories categories = viewModel.getCategories().getValue();
        if (categories != null) {
            chips.bind(categories, org.level, org.category);
        }
        if (!filled) {
            // Diisi sekali saja; setelah itu isian user tidak ditimpa (mis. saat logo baru diunggah).
            filled = true;
            set(binding.nameInput, org.name);
            set(binding.shortNameInput, org.shortName);
            set(binding.institutionInput, org.institutionName);
            set(binding.cityInput, org.city);
            set(binding.periodStartInput, org.periodStartYear == null ? null : String.valueOf(org.periodStartYear));
            set(binding.periodEndInput, org.periodEndYear == null ? null : String.valueOf(org.periodEndYear));
            set(binding.addressInput, org.address);
            set(binding.emailInput, org.email);
            set(binding.whatsappInput, org.whatsapp);
            set(binding.instagramInput, org.instagram == null ? null : "@" + org.instagram);
            set(binding.tiktokInput, org.tiktok == null ? null : "@" + org.tiktok);
            set(binding.youtubeInput, org.youtube);
        }

        boolean editable = org.canManage();
        binding.readonlyBanner.setVisibility(editable ? View.GONE : View.VISIBLE);
        binding.readonlyBanner.bind(StatusBannerView.Kind.INFO, getString(R.string.org_profile_readonly));
        binding.saveButton.setVisibility(editable ? View.VISIBLE : View.GONE);
        binding.logoButton.setVisibility(editable ? View.VISIBLE : View.GONE);
        binding.logoHint.setVisibility(editable ? View.VISIBLE : View.GONE);
        chips.setEnabled(editable);
        for (TextInputLayout layout : new TextInputLayout[] {binding.nameLayout, binding.shortNameLayout,
                binding.institutionLayout, binding.cityLayout, binding.periodStartLayout, binding.periodEndLayout,
                binding.addressLayout, binding.emailLayout, binding.whatsappLayout, binding.instagramLayout,
                binding.tiktokLayout, binding.youtubeLayout}) {
            layout.setEnabled(editable);
        }
    }

    private OrganizationForm readForm() {
        OrganizationForm form = new OrganizationForm();
        form.personal = personal;
        form.name = OnboardingUi.text(binding.nameInput);
        form.shortName = OnboardingUi.text(binding.shortNameInput);
        form.level = chips.getLevel();
        form.category = chips.getCategory();
        form.institution = OnboardingUi.text(binding.institutionInput);
        form.city = OnboardingUi.text(binding.cityInput);
        form.periodStart = OnboardingUi.text(binding.periodStartInput);
        form.periodEnd = OnboardingUi.text(binding.periodEndInput);
        return form;
    }

    private OrganizationProfileViewModel.Contact readContact() {
        OrganizationProfileViewModel.Contact contact = new OrganizationProfileViewModel.Contact();
        contact.address = OnboardingUi.text(binding.addressInput);
        contact.email = OnboardingUi.text(binding.emailInput);
        contact.whatsapp = OnboardingUi.text(binding.whatsappInput);
        contact.instagram = OnboardingUi.text(binding.instagramInput);
        contact.tiktok = OnboardingUi.text(binding.tiktokInput);
        contact.youtube = OnboardingUi.text(binding.youtubeInput);
        return contact;
    }

    private void showErrors(Map<OrganizationForm.Field, Integer> errors) {
        OnboardingUi.showError(binding.nameLayout, errors.get(OrganizationForm.Field.NAME));
        OnboardingUi.showError(binding.shortNameLayout, errors.get(OrganizationForm.Field.SHORT_NAME));
        OnboardingUi.showError(binding.institutionLayout, errors.get(OrganizationForm.Field.INSTITUTION));
        OnboardingUi.showError(binding.cityLayout, errors.get(OrganizationForm.Field.CITY));
        OnboardingUi.showError(binding.periodStartLayout, errors.get(OrganizationForm.Field.PERIOD_START));
        OnboardingUi.showError(binding.periodEndLayout, errors.get(OrganizationForm.Field.PERIOD_END));
        binding.levelError.setVisibility(errors.containsKey(OrganizationForm.Field.LEVEL) ? View.VISIBLE : View.GONE);
        binding.categoryError.setVisibility(errors.containsKey(OrganizationForm.Field.CATEGORY) ? View.VISIBLE : View.GONE);
    }

    private static void set(EditText input, @Nullable String value) {
        input.setText(value == null ? "" : value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
