package com.aris.templateapp.ui.creator;

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
import com.aris.templateapp.databinding.FragmentCreatorProfileBinding;
import com.aris.templateapp.ui.common.AppBarAccount;
import com.aris.templateapp.ui.common.CurrentUserViewModel;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.common.HomeNavigator;
import com.aris.templateapp.ui.profile.ProfileViewModel;
import com.aris.templateapp.ui.provider.TemplateUi;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Tab Profil pembuat website (alur-pembuatan-website.md bagian 5). Menggantikan bottom sheet menu profil:
 * beralih mode / jadi penyedia template, Pengaturan, dan Keluar ada di sini.
 */
@AndroidEntryPoint
public class CreatorProfileFragment extends Fragment {

    private FragmentCreatorProfileBinding binding;
    private CurrentUserViewModel userViewModel;
    private ProfileViewModel accountViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCreatorProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        userViewModel = new ViewModelProvider(this).get(CurrentUserViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        CreatorProfileViewModel profileViewModel = new ViewModelProvider(this).get(CreatorProfileViewModel.class);

        binding.statProjects.label.setText(R.string.profile_stat_projects);
        binding.statExported.label.setText(R.string.profile_stat_exported);
        profileViewModel.getCounts().observe(getViewLifecycleOwner(), counts -> {
            binding.statProjects.value.setText(TemplateUi.count(counts == null ? 0 : counts.total));
            binding.statExported.value.setText(TemplateUi.count(counts == null ? 0 : counts.exported));
        });

        binding.guestSignInButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_creator_dashboard_to_login));
        binding.editButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.creatorProfileEditFragment));
        binding.settingsItem.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.settingsFragment));
        binding.becomeProviderItem.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.providerFormFragment));
        binding.switchModeItem.setOnClickListener(v -> accountViewModel.switchMode(UserRole.PROVIDER));
        binding.signOutItem.setOnClickListener(v -> accountViewModel.signOut());

        userViewModel.getUser().observe(getViewLifecycleOwner(), this::bindUser);
        accountViewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.switchModeItem.setEnabled(!loading);
            binding.signOutItem.setEnabled(!loading);
        });
        accountViewModel.getModeChanged().observe(getViewLifecycleOwner(), event -> {
            User user = event.getContentIfNotHandled();
            if (user != null) {
                HomeNavigator.navigateHome(this, user);
            }
        });
        accountViewModel.getSignedOut().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() != null) {
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

    /** Tab tampil lagi (mis. setelah masuk, edit profil, atau beralih mode gagal): baca ulang data user. */
    @Override
    public void onResume() {
        super.onResume();
        userViewModel.reload();
    }

    private void bindUser(@Nullable User user) {
        boolean loggedIn = user != null;
        binding.guestSection.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        binding.userSection.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        binding.signOutItem.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        binding.signOutDivider.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        if (!loggedIn) {
            return;
        }
        binding.avatar.setText(AppBarAccount.initialOf(user.getDisplayName()));
        binding.name.setText(user.getDisplayName());
        binding.email.setText(user.getEmail());
        binding.email.setVisibility(user.getEmail() == null ? View.GONE : View.VISIBLE);

        String purpose = user.getWebsitePurpose();
        binding.purpose.setText(purpose == null ? getString(R.string.profile_not_set)
                : getString(TemplateUi.categoryLabel(purpose)));
        String organization = user.getOrganizationName();
        binding.organization.setText(organization == null ? getString(R.string.profile_not_set) : organization);

        // Punya profil provider aktif → beralih mode; belum provider → jadi penyedia template;
        // provider ditangguhkan → keduanya disembunyikan (mode provider terkunci).
        boolean provider = user.hasRole(UserRole.PROVIDER);
        binding.switchModeItem.setVisibility(provider && !user.isProviderSuspended() ? View.VISIBLE : View.GONE);
        binding.becomeProviderItem.setVisibility(provider ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
