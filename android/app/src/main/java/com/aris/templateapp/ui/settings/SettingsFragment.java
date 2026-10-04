package com.aris.templateapp.ui.settings;

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
import com.aris.templateapp.databinding.FragmentSettingsBinding;
import com.aris.templateapp.ui.common.CurrentUserViewModel;
import com.aris.templateapp.ui.common.HomeNavigator;
import com.aris.templateapp.ui.profile.ProfileViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.Optional;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/** Pengaturan (bagian 9.4): daftar bergrup dengan judul grup mono. Grup Akun hanya untuk user login. */
@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    @Inject
    Optional<DebugMenu> debugMenu;

    private FragmentSettingsBinding binding;
    private CurrentUserViewModel userViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Logika keluar sama dengan tab Profil, jadi ProfileViewModel dipakai ulang.
        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.linkedMethodsItem.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_settings_to_linked_methods));
        binding.version.setText(getString(R.string.settings_version, BuildConfig.VERSION_NAME));
        debugMenu.ifPresent(menu -> menu.addTo(binding.debugContainer, this));
        userViewModel = new ViewModelProvider(this).get(CurrentUserViewModel.class);
        userViewModel.getUser().observe(getViewLifecycleOwner(), user ->
                binding.accountGroup.setVisibility(user == null ? View.GONE : View.VISIBLE));
        binding.signOutItem.setOnClickListener(v -> profileViewModel.signOut());
        profileViewModel.isLoading().observe(getViewLifecycleOwner(), loading -> binding.signOutItem.setEnabled(!loading));
        profileViewModel.getSignedOut().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() != null) {
                View root = requireActivity().findViewById(android.R.id.content);
                HomeNavigator.navigateHome(this, null);
                Snackbar.make(root, R.string.signed_out, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        userViewModel.reload();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
