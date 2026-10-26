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

import com.aris.templateapp.BuildConfig;
import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.data.model.UserRole;
import com.aris.templateapp.data.model.Workspace;
import com.aris.templateapp.databinding.FragmentCreatorProfileBinding;
import com.aris.templateapp.ui.common.CurrentUserViewModel;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.common.HomeNavigator;
import com.aris.templateapp.ui.profile.AvatarViews;
import com.aris.templateapp.ui.profile.ProfileRow;
import com.aris.templateapp.ui.profile.ProfileViewModel;
import com.aris.templateapp.ui.workspace.WorkspaceRows;
import com.aris.templateapp.ui.workspace.WorkspaceViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Tab Profil pembuat website = akun pribadi (alur-pembuatan-website.md bagian 8): kartu identitas + statistik ruang
 * kerja, "Ruang kerja saya" (ketuk untuk pindah), Mode (beralih / jadi kreator template), Akun, dan Keluar.
 * Baris menu memakai {@link ProfileRow} agar sama dengan Profil kreator.
 */
@AndroidEntryPoint
public class CreatorProfileFragment extends Fragment {

    private FragmentCreatorProfileBinding binding;
    @Inject
    ThumbnailLoader thumbnailLoader;

    private CurrentUserViewModel userViewModel;
    private WorkspaceViewModel workspaceViewModel;
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
        workspaceViewModel = new ViewModelProvider(this).get(WorkspaceViewModel.class);
        // Daftar dan ruang kerja aktif diamati bersama, agar tanda ✓ pindah begitu ruang kerja diganti.
        workspaceViewModel.getWorkspaces().observe(getViewLifecycleOwner(), list -> bindWorkspaces());
        workspaceViewModel.getActive().observe(getViewLifecycleOwner(), active -> bindWorkspaces());
        accountViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        ProfileRow.bind(binding.switchModeItem, R.drawable.ic_swap_horiz, R.string.profile_switch_to_provider,
                R.string.profile_switch_to_provider_desc);
        ProfileRow.bind(binding.becomeProviderItem, R.drawable.ic_storefront, R.string.profile_become_provider,
                R.string.profile_become_provider_desc);
        ProfileRow.bind(binding.linkedMethodsItem, R.drawable.ic_link, R.string.settings_linked_methods,
                R.string.profile_linked_methods_desc);
        ProfileRow.bind(binding.settingsItem, R.drawable.ic_settings, R.string.profile_settings,
                R.string.profile_settings_desc);
        ProfileRow.bind(binding.signOutItem, R.drawable.ic_logout, R.string.profile_sign_out,
                R.string.profile_sign_out_desc);
        ProfileRow.danger(binding.signOutItem);
        binding.version.setText(getString(R.string.profile_version, BuildConfig.VERSION_NAME));

        binding.guestSignInButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_creator_dashboard_to_login));
        binding.editButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.creatorProfileEditFragment));
        binding.linkedMethodsItem.getRoot().setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.linkedMethodsFragment));
        binding.settingsItem.getRoot().setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.settingsFragment));
        binding.becomeProviderItem.getRoot().setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.providerFormFragment));
        binding.switchModeItem.getRoot().setOnClickListener(v -> accountViewModel.switchMode(UserRole.PROVIDER));
        binding.signOutItem.getRoot().setOnClickListener(v -> accountViewModel.signOut());

        userViewModel.getUser().observe(getViewLifecycleOwner(), this::bindUser);
        accountViewModel.isLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.switchModeItem.getRoot().setEnabled(!loading);
            binding.signOutItem.getRoot().setEnabled(!loading);
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

    private void bindWorkspaces() {
        List<Workspace> list = workspaceViewModel.getWorkspaces().getValue();
        Workspace active = workspaceViewModel.getActive().getValue();
        List<Workspace> workspaces = list == null ? Collections.emptyList() : list;
        long organizations = workspaces.stream().filter(w -> !w.isPersonal()).count();
        binding.statWorkspaces.setText(String.valueOf(workspaces.size()));
        binding.statOrganizations.setText(String.valueOf(organizations));
        binding.workspacesSection.setVisibility(workspaces.isEmpty() || binding.userSection.getVisibility() != View.VISIBLE
                ? View.GONE : View.VISIBLE);
        WorkspaceRows.bind(binding.workspaceList, workspaces, active,
                thumbnailLoader, workspace -> {
                    if (active != null && active.getId().equals(workspace.getId())) {
                        return;
                    }
                    workspaceViewModel.select(workspace);
                    Snackbar.make(binding.getRoot(), getString(R.string.workspace_switched, workspace.getName()),
                            Snackbar.LENGTH_SHORT).show();
                });
    }

    private void bindUser(@Nullable User user) {
        boolean loggedIn = user != null;
        // User dibaca di thread latar; sebelum itu semua bagian masih "gone" dan isi layar bergeser saat muncul.
        binding.content.setVisibility(View.VISIBLE);
        binding.guestSection.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        binding.userSection.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        binding.modeSection.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        binding.linkedMethodsItem.getRoot().setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        binding.linkedMethodsDivider.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        binding.signOutCard.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        bindWorkspaces();
        if (!loggedIn) {
            return;
        }
        AvatarViews.bind(binding.avatar, binding.avatarPhoto, thumbnailLoader, user.getDisplayName(),
                user.getAvatarUrl());
        binding.avatar.setContentDescription(getString(R.string.cd_profile_avatar, user.getDisplayName()));
        binding.name.setText(user.getDisplayName());
        binding.email.setText(user.getEmail());
        binding.email.setVisibility(user.getEmail() == null ? View.GONE : View.VISIBLE);

        // Punya profil provider aktif → beralih mode; belum provider → jadi penyedia template;
        // provider ditangguhkan → keduanya disembunyikan (mode provider terkunci).
        boolean provider = user.hasRole(UserRole.PROVIDER);
        boolean canSwitch = provider && !user.isProviderSuspended();
        binding.switchModeItem.getRoot().setVisibility(canSwitch ? View.VISIBLE : View.GONE);
        binding.becomeProviderItem.getRoot().setVisibility(provider ? View.GONE : View.VISIBLE);
        binding.modeSection.setVisibility(canSwitch || !provider ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
