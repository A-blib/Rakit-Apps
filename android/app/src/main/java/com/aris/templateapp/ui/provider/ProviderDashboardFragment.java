package com.aris.templateapp.ui.provider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.FragmentProviderDashboardBinding;
import com.aris.templateapp.ui.common.AppBarAccount;
import com.aris.templateapp.ui.common.CurrentUserViewModel;
import com.aris.templateapp.ui.common.HomeNavigator;
import com.aris.templateapp.ui.creator.CreatorDashboardFragment;
import com.aris.templateapp.ui.profile.ProfileSheet;
import com.google.android.material.badge.BadgeDrawable;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Shell Dashboard Provider (alur-provider.md bagian 2): app bar + 4 tab di bottom navigation.
 * <p>
 * Setiap tab adalah Fragment anak yang dibuat saat pertama kali dibuka lalu hanya disembunyikan/ditampilkan,
 * sehingga isi dan posisi scroll tab tetap utuh. Layar lanjutan (detail template, panduan, edit profil) dibuka
 * lewat Navigation utama di atas shell ini (lihat {@link ProviderNav}).
 */
@AndroidEntryPoint
public class ProviderDashboardFragment extends Fragment {

    private static final String STATE_TAB = "selectedTab";
    private static final int[] TABS = {R.id.tab_home, R.id.tab_upload, R.id.tab_templates, R.id.tab_profile};

    private FragmentProviderDashboardBinding binding;
    private CurrentUserViewModel userViewModel;
    @IdRes
    private int selectedTab = R.id.tab_home;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            selectedTab = savedInstanceState.getInt(STATE_TAB, R.id.tab_home);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProviderDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        userViewModel = new ViewModelProvider(this).get(CurrentUserViewModel.class);
        ProviderDashboardViewModel dashboardViewModel = new ViewModelProvider(this).get(ProviderDashboardViewModel.class);

        binding.account.avatarContainer.setOnClickListener(v ->
                new ProfileSheet().show(getChildFragmentManager(), ProfileSheet.TAG));
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            AppBarAccount.bind(binding.account, user);
            // Ditangguhkan saat app terbuka (data /users/me terbaru): mode provider dikunci (bagian 3.2).
            if (user != null && user.isProviderSuspended()) {
                HomeNavigator.navigateHome(this, user);
            }
        });

        binding.bottomNav.setOnItemSelectedListener(item -> {
            showTab(item.getItemId());
            return true;
        });
        binding.bottomNav.setSelectedItemId(selectedTab);
        showTab(selectedTab);

        // Badge tab Beranda = jumlah "Perlu tindakan" (bagian 3.3), monokrom seperti komponen lain.
        dashboardViewModel.getDashboard().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null || resource.getData() == null) {
                return;
            }
            int count = resource.getData().actionItems == null ? 0 : resource.getData().actionItems.size();
            BadgeDrawable badge = binding.bottomNav.getOrCreateBadge(R.id.tab_home);
            badge.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_foreground));
            badge.setBadgeTextColor(ContextCompat.getColor(requireContext(), R.color.color_background));
            badge.setNumber(count);
            badge.setVisible(count > 0);
        });
        dashboardViewModel.getAccessLost().observe(getViewLifecycleOwner(), event -> {
            String code = event.getContentIfNotHandled();
            if (code != null) {
                NavHostFragment.findNavController(this).navigate(R.id.creatorDashboardFragment,
                        CreatorDashboardFragment.args("PROVIDER_SUSPENDED".equals(code)),
                        new NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        userViewModel.reload();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_TAB, selectedTab);
    }

    /** Dipanggil tab lain, mis. tombol "Upload template baru" di Beranda membuka tab Upload. */
    public void selectTab(@IdRes int tab) {
        if (binding != null) {
            binding.bottomNav.setSelectedItemId(tab);
        }
    }

    /** "Lihat semua" di Perlu tindakan: buka Template Anda dengan filter status tertentu. */
    public void openTemplates(String status) {
        new ViewModelProvider(requireActivity()).get(ProviderTemplatesViewModel.class).setStatus(status);
        selectTab(R.id.tab_templates);
    }

    private void showTab(@IdRes int tab) {
        selectedTab = tab;
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction().setReorderingAllowed(true);
        for (int id : TABS) {
            Fragment fragment = fm.findFragmentByTag(tagOf(id));
            if (id == tab) {
                if (fragment == null) {
                    transaction.add(R.id.tab_container, newTab(id), tagOf(id));
                } else {
                    transaction.show(fragment);
                }
            } else if (fragment != null) {
                transaction.hide(fragment);
            }
        }
        transaction.commitNow();
    }

    private static String tagOf(@IdRes int tab) {
        return "provider_tab_" + tab;
    }

    private static Fragment newTab(@IdRes int tab) {
        if (tab == R.id.tab_upload) {
            return new ProviderUploadFragment();
        } else if (tab == R.id.tab_templates) {
            return new ProviderTemplatesFragment();
        } else if (tab == R.id.tab_profile) {
            return new ProviderProfileFragment();
        }
        return new ProviderHomeFragment();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
