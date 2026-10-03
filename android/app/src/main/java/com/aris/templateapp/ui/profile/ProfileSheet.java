package com.aris.templateapp.ui.profile;

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
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.data.model.UserRole;
import com.aris.templateapp.databinding.SheetProfileBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.common.HomeNavigator;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Menu profil (bagian 6.6 & 9.4), dibuka dari avatar di dashboard: nama + email, badge mode aktif,
 * beralih mode / jadi penyedia template, Pengaturan, Keluar.
 */
@AndroidEntryPoint
public class ProfileSheet extends BottomSheetDialogFragment {

    public static final String TAG = "ProfileSheet";

    private SheetProfileBinding binding;
    private ProfileViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SheetProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        viewModel.getUser().observe(getViewLifecycleOwner(), this::bindUser);
        viewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.switchModeItem.setEnabled(!loading);
            binding.signOutItem.setEnabled(!loading);
        });

        binding.settingsItem.setOnClickListener(v -> navigateFromHost(R.id.settingsFragment));
        binding.becomeProviderItem.setOnClickListener(v -> navigateFromHost(R.id.providerFormFragment));
        binding.signOutItem.setOnClickListener(v -> viewModel.signOut());

        viewModel.getModeChanged().observe(getViewLifecycleOwner(), event -> {
            User user = event.getContentIfNotHandled();
            if (user != null) {
                Fragment host = requireParentFragment();
                dismiss();
                HomeNavigator.navigateHome(host, user);
            }
        });
        viewModel.getSignedOut().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() != null) {
                Fragment host = requireParentFragment();
                View hostView = host.requireView();
                dismiss();
                HomeNavigator.navigateHome(host, null);
                Snackbar.make(hostView, R.string.signed_out, Snackbar.LENGTH_SHORT).show();
            }
        });
        viewModel.getFailure().observe(getViewLifecycleOwner(), event -> {
            var error = event.getContentIfNotHandled();
            if (error != null) {
                Snackbar snackbar = Snackbar.make(binding.getRoot(), ErrorMessages.forError(requireContext(), error),
                        Snackbar.LENGTH_LONG);
                // Gagal karena koneksi: beri tombol "Coba lagi" (skenario 12 bagian 13.2).
                if (error.isNetworkError()) {
                    snackbar.setDuration(Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.action_retry, v -> viewModel.retrySwitchMode());
                }
                snackbar.show();
            }
        });
    }

    private void bindUser(@Nullable User user) {
        if (user == null) {
            return;
        }
        binding.name.setText(user.getDisplayName());
        binding.email.setText(user.getEmail());
        binding.email.setVisibility(user.getEmail() == null ? View.GONE : View.VISIBLE);
        boolean providerMode = user.getActiveMode() == UserRole.PROVIDER;
        binding.modeBadge.setText(providerMode ? R.string.profile_mode_provider : R.string.profile_mode_creator);

        // "Beralih mode" hanya jika punya dua peran; jika belum provider tampilkan "Jadi penyedia template".
        boolean hasBoth = user.hasRole(UserRole.CREATOR) && user.hasRole(UserRole.PROVIDER);
        binding.switchModeItem.setVisibility(hasBoth ? View.VISIBLE : View.GONE);
        binding.switchModeItem.setText(providerMode ? R.string.profile_switch_to_creator : R.string.profile_switch_to_provider);
        binding.switchModeItem.setOnClickListener(v ->
                viewModel.switchMode(providerMode ? UserRole.CREATOR : UserRole.PROVIDER));
        binding.becomeProviderItem.setVisibility(user.hasRole(UserRole.PROVIDER) ? View.GONE : View.VISIBLE);
    }

    /** Sheet ini adalah dialog; navigasi dilakukan lewat NavController milik layar di belakangnya. */
    private void navigateFromHost(int destination) {
        Fragment host = requireParentFragment();
        dismiss();
        NavHostFragment.findNavController(host).navigate(destination);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
