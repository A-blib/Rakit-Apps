package com.aris.templateapp.ui.creator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.data.model.Workspace;
import com.aris.templateapp.databinding.FragmentCreatorDashboardBinding;
import com.aris.templateapp.ui.common.AppBarAccount;
import com.aris.templateapp.ui.common.CurrentUserViewModel;
import com.aris.templateapp.ui.common.KeyboardAwareBottomBar;
import com.aris.templateapp.ui.workspace.WorkspacePickerSheet;
import com.aris.templateapp.ui.workspace.WorkspaceViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Shell Dashboard Pembuat Website (alur-pembuatan-website.md bagian 2), untuk tamu maupun user login:
 * app bar (Masuk / avatar → tab Profil) + ViewPager2 berisi 4 tab + bottom navigation dengan tombol + di tengah.
 * Tab bisa dipilih lewat bottom navigation atau digeser ke samping, sama seperti Dashboard Provider.
 */
@AndroidEntryPoint
public class CreatorDashboardFragment extends Fragment {

    /** Argumen navigasi (lihat nav_graph.xml): true jika user dialihkan ke sini karena mode provider ditangguhkan. */
    public static final String ARG_PROVIDER_SUSPENDED = "providerSuspended";

    /** Urutan tab = urutan halaman ViewPager2. Slot tombol + di menu bukan tab, jadi tidak ada di sini. */
    static final int[] TABS = {R.id.tab_creator_home, R.id.tab_creator_projects, R.id.tab_creator_gallery,
            R.id.tab_creator_profile};

    @Inject
    ThumbnailLoader thumbnailLoader;

    private FragmentCreatorDashboardBinding binding;
    private CurrentUserViewModel userViewModel;
    private WorkspaceViewModel workspaceViewModel;
    private KeyboardAwareBottomBar keyboardAware;

    /** Tombol kembali di tab selain Beranda membuka Beranda dulu; di Beranda baru menutup app. */
    private final OnBackPressedCallback backToHome = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            selectTab(R.id.tab_creator_home);
        }
    };

    private final ViewPager2.OnPageChangeCallback pageChange = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            binding.bottomNav.getMenu().findItem(TABS[position]).setChecked(true);
            backToHome.setEnabled(position != 0);
        }
    };

    public static Bundle args(boolean providerSuspended) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_PROVIDER_SUSPENDED, providerSuspended);
        return args;
    }

    boolean isProviderSuspended() {
        return getArguments() != null && getArguments().getBoolean(ARG_PROVIDER_SUSPENDED, false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCreatorDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        userViewModel = new ViewModelProvider(this).get(CurrentUserViewModel.class);
        workspaceViewModel = new ViewModelProvider(this).get(WorkspaceViewModel.class);
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            AppBarAccount.bind(binding.account, user);
            bindWorkspace(user == null ? null : workspaceViewModel.getActive().getValue());
        });
        workspaceViewModel.getActive().observe(getViewLifecycleOwner(), workspace ->
                bindWorkspace(userViewModel.getUser().getValue() == null ? null : workspace));
        workspaceViewModel.getNeedsWorkspace().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() != null) {
                // User lama tanpa ruang kerja: tampilkan layar pilihan yang sama dengan onboarding (bagian 5).
                NavHostFragment.findNavController(this).navigate(R.id.workspaceChoiceFragment, null,
                        new NavOptions.Builder().setPopUpTo(R.id.creatorDashboardFragment, true).build());
            }
        });
        binding.workspaceSwitcher.setOnClickListener(v -> {
            if (getChildFragmentManager().findFragmentByTag(WorkspacePickerSheet.TAG) == null) {
                new WorkspacePickerSheet().show(getChildFragmentManager(), WorkspacePickerSheet.TAG);
            }
        });
        binding.account.signInButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_creator_dashboard_to_login));
        // Avatar membuka tab Profil (menggantikan bottom sheet menu profil, keputusan Aris).
        binding.account.avatarContainer.setOnClickListener(v -> selectTab(R.id.tab_creator_profile));

        keyboardAware = KeyboardAwareBottomBar.attach(binding.getRoot(), binding.bottomBar, binding.bottomNavDivider);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), backToHome);

        binding.pager.setAdapter(new CreatorTabsAdapter(this));
        binding.pager.setOffscreenPageLimit(TABS.length - 1);
        binding.pager.registerOnPageChangeCallback(pageChange);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int position = indexOf(item.getItemId());
            if (position >= 0 && binding.pager.getCurrentItem() != position) {
                binding.pager.setCurrentItem(position, true);
            }
            return position >= 0;
        });
        binding.createButton.setOnClickListener(v -> showCreateSheet());
    }

    @Override
    public void onStart() {
        super.onStart();
        // Dibaca ulang setiap layar tampil lagi, mis. setelah kembali dari layar Masuk.
        userViewModel.reload();
        if (userViewModel.isLoggedInNow()) {
            workspaceViewModel.refresh();
        }
    }

    /**
     * Tombol +, kartu "Mulai", dan tombol "Buat website" di tab Website membuka bottom sheet yang sama. Website
     * disimpan di cloud milik ruang kerja, jadi tamu diminta masuk dulu (alur-pembuatan-website.md G1).
     */
    public void showCreateSheet() {
        if (!userViewModel.isLoggedInNow()) {
            NavHostFragment.findNavController(this).navigate(R.id.action_creator_dashboard_to_login);
            return;
        }
        if (getChildFragmentManager().findFragmentByTag(CreateProjectSheet.TAG) == null) {
            new CreateProjectSheet().show(getChildFragmentManager(), CreateProjectSheet.TAG);
        }
    }

    public void selectTab(@IdRes int tab) {
        int position = indexOf(tab);
        if (binding != null && position >= 0) {
            binding.pager.setCurrentItem(position, true);
        }
    }

    /** "Lihat semua" di "Template untuk anda": buka tab Template dengan kategori yang sesuai sudah terpilih. */
    public void openGallery(@Nullable String category) {
        new ViewModelProvider(requireActivity()).get(GalleryViewModel.class)
                .setCategory(category == null ? GalleryViewModel.ALL : category);
        selectTab(R.id.tab_creator_gallery);
    }

    /** Tamu atau belum punya ruang kerja: judul "OrgSpace". Selain itu: nama ruang kerja + panah pemilih. */
    private void bindWorkspace(@Nullable Workspace workspace) {
        if (binding == null) {
            return;
        }
        boolean show = workspace != null;
        binding.workspaceSwitcher.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.toolbar.setTitle(show ? null : getString(R.string.app_name));
        if (show) {
            // Nama singkat lebih muat di app bar ("OSIS Smansa"); nama lengkap tetap ada di pemilih.
            binding.workspaceName.setText(workspace.getGreetingName());
            binding.workspaceSwitcher.setContentDescription(getString(R.string.cd_workspace_picker, workspace.getName()));
            binding.workspaceInitial.setText(AppBarAccount.initialOf(workspace.getName()));
            binding.workspaceInitial.setVisibility(View.VISIBLE);
            binding.workspaceLogo.setImageDrawable(null);
            thumbnailLoader.load(workspace.getLogoUrl(), binding.workspaceLogo,
                    () -> binding.workspaceInitial.setVisibility(View.INVISIBLE));
        }
    }

    private static int indexOf(@IdRes int tab) {
        for (int i = 0; i < TABS.length; i++) {
            if (TABS[i] == tab) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        keyboardAware.detach();
        binding.pager.unregisterOnPageChangeCallback(pageChange);
        binding = null;
    }
}
