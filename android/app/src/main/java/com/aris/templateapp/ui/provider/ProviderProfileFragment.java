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
import com.aris.templateapp.data.model.UserRole;
import com.aris.templateapp.data.remote.dto.ProviderProfileDto;
import com.aris.templateapp.databinding.FragmentProviderProfileBinding;
import com.aris.templateapp.ui.common.AppBarAccount;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.common.HomeNavigator;
import com.aris.templateapp.ui.profile.ProfileViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;

/** Tab Profil versi awal (alur-provider.md bagian 7). */
@AndroidEntryPoint
public class ProviderProfileFragment extends Fragment {

    private FragmentProviderProfileBinding binding;
    private ProviderProfileViewModel viewModel;
    private ProfileViewModel accountViewModel;
    private boolean resumedBefore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProviderProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ProviderProfileViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        binding.statPublished.label.setText(R.string.profile_stat_published);
        binding.statDownloads.label.setText(R.string.profile_stat_downloads);

        binding.editButton.setOnClickListener(v -> ProviderNav.openEditProfile(this));
        binding.linkedMethodsItem.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.linkedMethodsFragment));
        binding.settingsItem.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.settingsFragment));
        binding.switchModeItem.setOnClickListener(v -> accountViewModel.switchMode(UserRole.CREATOR));
        binding.signOutItem.setOnClickListener(v -> accountViewModel.signOut());

        viewModel.getProfile().observe(getViewLifecycleOwner(), this::render);
        accountViewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.switchModeItem.setEnabled(!loading);
            binding.signOutItem.setEnabled(!loading);
        });
        accountViewModel.getModeChanged().observe(getViewLifecycleOwner(), event -> {
            var user = event.getContentIfNotHandled();
            if (user != null) {
                HomeNavigator.navigateHome(this, user);
            }
        });
        accountViewModel.getSignedOut().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() != null) {
                // Snackbar ditempel ke Activity karena layar ini langsung diganti Dashboard Pembuat Website.
                View root = requireActivity().findViewById(android.R.id.content);
                HomeNavigator.navigateHome(this, null);
                Snackbar.make(root, R.string.signed_out, Snackbar.LENGTH_SHORT).show();
            }
        });
        accountViewModel.getFailure().observe(getViewLifecycleOwner(), event -> {
            var error = event.getContentIfNotHandled();
            if (error != null) {
                Snackbar snackbar = Snackbar.make(binding.getRoot(), ErrorMessages.forError(requireContext(), error),
                        Snackbar.LENGTH_LONG);
                if (error.isNetworkError()) {
                    snackbar.setAction(R.string.action_retry, v -> accountViewModel.retrySwitchMode());
                }
                snackbar.show();
            }
        });
    }

    /** Tab ini tampil lagi (geser/bottom navigation, kembali dari Edit profil/Pengaturan): tampilkan data terbaru. */
    @Override
    public void onResume() {
        super.onResume();
        if (resumedBefore) {
            viewModel.load();
        }
        resumedBefore = true;
    }

    private void render(@Nullable Resource<ProviderProfileDto> resource) {
        if (resource == null) {
            return;
        }
        switch (resource.getStatus()) {
            case LOADING:
                binding.content.setVisibility(View.GONE);
                binding.state.showLoading();
                break;
            case ERROR:
                binding.content.setVisibility(View.GONE);
                binding.state.showError(ErrorMessages.forError(requireContext(), resource.getError()), viewModel::load);
                break;
            case SUCCESS:
            default:
                binding.state.hide(() -> {
                    if (binding != null) {
                        binding.content.setVisibility(View.VISIBLE);
                        bind(resource.getData());
                    }
                });
                break;
        }
    }

    private void bind(ProviderProfileDto profile) {
        binding.avatar.setText(AppBarAccount.initialOf(profile.creatorName));
        binding.creatorName.setText(profile.creatorName);
        boolean hasBio = profile.bio != null && !profile.bio.trim().isEmpty();
        binding.bio.setText(hasBio ? profile.bio : getString(R.string.profile_no_bio));
        binding.portfolio.setText(profile.portfolioUrl);
        binding.portfolio.setVisibility(profile.portfolioUrl == null ? View.GONE : View.VISIBLE);

        binding.specialties.removeAllViews();
        if (profile.specialties != null) {
            for (String specialty : profile.specialties) {
                Chip chip = new Chip(requireContext());
                chip.setText(specialty);
                chip.setCheckable(false);
                chip.setClickable(false);
                binding.specialties.addView(chip);
            }
        }
        binding.specialties.setVisibility(binding.specialties.getChildCount() == 0 ? View.GONE : View.VISIBLE);

        binding.statPublished.value.setText(TemplateUi.count(profile.publishedCount));
        binding.statDownloads.value.setText(TemplateUi.count(profile.totalDownloads));
        binding.email.setText(profile.email);
        binding.email.setVisibility(profile.email == null ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
