package com.aris.templateapp.ui.creator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.databinding.FragmentCreatorProfileEditBinding;
import com.aris.templateapp.ui.onboarding.OnboardingFormValidator;
import com.aris.templateapp.ui.onboarding.OnboardingUi;
import com.aris.templateapp.ui.profile.AvatarEditSection;
import com.aris.templateapp.ui.profile.AvatarEditViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * "Edit profil" pembuat website (alur-pembuatan-website.md bagian 5.1): memakai layout form onboarding pembuat
 * website yang sama, dengan judul "Edit profil", tanpa tombol "Lewati", dan tombol "Simpan". Bagian foto profil
 * ({@link AvatarEditSection}) langsung menyimpan foto saat dipilih.
 */
@AndroidEntryPoint
public class CreatorProfileEditFragment extends Fragment {

    @Inject
    ThumbnailLoader thumbnailLoader;

    private AvatarEditViewModel avatarViewModel;
    /** Photo Picker bawaan Android (tanpa izin penyimpanan); didaftarkan saat Fragment dibuat. */
    private final ActivityResultLauncher<PickVisualMediaRequest> avatarPicker = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    avatarViewModel.upload(uri);
                }
            });

    private FragmentCreatorProfileEditBinding binding;
    private CreatorProfileEditViewModel viewModel;
    /** Isi awal hanya dipasang sekali, agar ketikan user tidak tertimpa (mis. layar diputar). */
    private boolean prefilled;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCreatorProfileEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        prefilled = savedInstanceState != null;
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        avatarViewModel = new ViewModelProvider(this).get(AvatarEditViewModel.class);
        AvatarEditSection.attach(this, binding.avatarSection, avatarViewModel, thumbnailLoader,
                () -> avatarPicker.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));

        viewModel = new ViewModelProvider(this).get(CreatorProfileEditViewModel.class);
        viewModel.getInitial().observe(getViewLifecycleOwner(), this::prefill);
        binding.saveButton.setOnClickListener(v -> viewModel.save(OnboardingUi.text(binding.displayNameInput)));

        viewModel.getFormErrors().observe(getViewLifecycleOwner(), errors ->
                OnboardingUi.showError(binding.displayNameLayout, errors.get(OnboardingFormValidator.Field.DISPLAY_NAME)));
        viewModel.isSaving().observe(getViewLifecycleOwner(), saving -> binding.saveButton.setEnabled(!saving));
        viewModel.getFailure().observe(getViewLifecycleOwner(), event -> {
            var error = event.getContentIfNotHandled();
            if (error != null) {
                OnboardingUi.showFailure(binding.getRoot(), error,
                        Map.of("displayName", binding.displayNameLayout),
                        binding.saveButton::performClick);
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
