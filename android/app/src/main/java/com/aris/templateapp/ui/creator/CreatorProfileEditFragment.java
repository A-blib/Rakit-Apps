package com.aris.templateapp.ui.creator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.databinding.FragmentCreatorFormBinding;
import com.aris.templateapp.ui.onboarding.OnboardingFormValidator;
import com.aris.templateapp.ui.onboarding.OnboardingUi;
import com.google.android.material.snackbar.Snackbar;

import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * "Edit profil" pembuat website (alur-pembuatan-website.md bagian 5.1): memakai layout form onboarding pembuat
 * website yang sama, dengan judul "Edit profil", tanpa tombol "Lewati", dan tombol "Simpan".
 */
@AndroidEntryPoint
public class CreatorProfileEditFragment extends Fragment {

    private FragmentCreatorFormBinding binding;
    private CreatorProfileEditViewModel viewModel;
    /** Isi awal hanya dipasang sekali, agar ketikan user tidak tertimpa (mis. layar diputar). */
    private boolean prefilled;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCreatorFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        prefilled = savedInstanceState != null;
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.formTitle.setText(R.string.profile_edit_title);
        binding.formSubtitle.setText(R.string.profile_creator_edit_subtitle);
        // Tanpa "Lewati", tombol Simpan dibuat selebar layar (sama dengan Edit profil provider).
        binding.skipButton.setVisibility(View.GONE);
        binding.buttonSpacer.setVisibility(View.GONE);
        binding.startButton.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        binding.startButton.setText(R.string.action_save);

        viewModel = new ViewModelProvider(this).get(CreatorProfileEditViewModel.class);
        viewModel.getInitial().observe(getViewLifecycleOwner(), this::prefill);
        binding.startButton.setOnClickListener(v -> viewModel.save(
                OnboardingUi.text(binding.displayNameInput),
                OnboardingUi.purposeValue(binding.purposeGroup.getCheckedChipId()),
                OnboardingUi.text(binding.organizationInput)));

        viewModel.getFormErrors().observe(getViewLifecycleOwner(), errors ->
                OnboardingUi.showError(binding.displayNameLayout, errors.get(OnboardingFormValidator.Field.DISPLAY_NAME)));
        viewModel.isSaving().observe(getViewLifecycleOwner(), saving -> binding.startButton.setEnabled(!saving));
        viewModel.getFailure().observe(getViewLifecycleOwner(), event -> {
            var error = event.getContentIfNotHandled();
            if (error != null) {
                OnboardingUi.showFailure(binding.getRoot(), error,
                        Map.of("displayName", binding.displayNameLayout, "organizationName", binding.organizationLayout),
                        binding.startButton::performClick);
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

    private void prefill(@Nullable User user) {
        if (user == null || prefilled) {
            return;
        }
        prefilled = true;
        binding.displayNameInput.setText(user.getDisplayName());
        binding.organizationInput.setText(user.getOrganizationName());
        int chip = OnboardingUi.purposeChipId(user.getWebsitePurpose());
        if (chip != View.NO_ID) {
            binding.purposeGroup.check(chip);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
