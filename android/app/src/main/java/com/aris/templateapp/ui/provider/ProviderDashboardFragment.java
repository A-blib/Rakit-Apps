package com.aris.templateapp.ui.provider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

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
 * Setiap tab adalah Fragment anak di dalam ViewPager2, sehingga tab bisa dipilih lewat bottom navigation atau
 * digeser ke samping. Tab yang sedang tampil berstatus RESUMED, tab lain hanya STARTED, jadi tab memakai
 * {@code onResume} sebagai tanda "tab ini baru dibuka". Layar lanjutan (detail template, panduan, edit profil) dibuka
 * lewat Navigation utama di atas shell ini (lihat {@link ProviderNav}).
 */
@AndroidEntryPoint
public class ProviderDashboardFragment extends Fragment {

    /** Urutan tab = urutan halaman ViewPager2 = urutan menu bottom navigation. */
    static final int[] TABS = {R.id.tab_home, R.id.tab_upload, R.id.tab_templates, R.id.tab_profile};

    private FragmentProviderDashboardBinding binding;
    private CurrentUserViewModel userViewModel;

    /** Tombol kembali di tab selain Beranda membuka Beranda dulu; di Beranda baru menutup app. */
    private final OnBackPressedCallback backToHome = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            selectTab(R.id.tab_home);
        }
    };

    /**
     * Bottom navigation disembunyikan selama keyboard terbuka (mis. saat mengetik di pencarian Template Anda),
     * supaya ruang layar yang tersisa dipakai untuk isi, bukan untuk tab.
     */
    private final ViewTreeObserver.OnGlobalLayoutListener keyboardListener = () -> {
        if (binding == null) {
            return;
        }
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(binding.getRoot());
        int visibility = insets != null && insets.isVisible(WindowInsetsCompat.Type.ime()) ? View.GONE : View.VISIBLE;
        if (binding.bottomNav.getVisibility() != visibility) {
            binding.bottomNav.setVisibility(visibility);
            binding.bottomNavDivider.setVisibility(visibility);
        }
    };

    /** Halaman berganti (digeser atau lewat kode): samakan tab yang menyala di bottom navigation. */
    private final ViewPager2.OnPageChangeCallback pageChange = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            binding.bottomNav.getMenu().findItem(TABS[position]).setChecked(true);
            backToHome.setEnabled(position != 0);
        }
    };

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

        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(keyboardListener);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), backToHome);

        // Tab bisa dipilih lewat bottom navigation ATAU digeser ke samping (ViewPager2).
        // Semua tab dibiarkan hidup (offscreenPageLimit = 3) agar isi & posisi scroll tiap tab tetap utuh.
        binding.pager.setAdapter(new ProviderTabsAdapter(this));
        binding.pager.setOffscreenPageLimit(TABS.length - 1);
        binding.pager.registerOnPageChangeCallback(pageChange);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int position = indexOf(item.getItemId());
            if (binding.pager.getCurrentItem() != position) {
                binding.pager.setCurrentItem(position, true);
            }
            return true;
        });

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

    /** Dipanggil tab lain, mis. tombol "Upload template baru" di Beranda membuka tab Upload. */
    public void selectTab(@IdRes int tab) {
        if (binding != null) {
            binding.pager.setCurrentItem(indexOf(tab), true);
        }
    }

    /** "Lihat semua" di Perlu tindakan: buka Template Anda dengan filter status tertentu. */
    public void openTemplates(String status) {
        new ViewModelProvider(requireActivity()).get(ProviderTemplatesViewModel.class).setStatus(status);
        selectTab(R.id.tab_templates);
    }

    private static int indexOf(@IdRes int tab) {
        for (int i = 0; i < TABS.length; i++) {
            if (TABS[i] == tab) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(keyboardListener);
        binding.pager.unregisterOnPageChangeCallback(pageChange);
        binding = null;
    }
}
