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
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.data.model.PublishRequest;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.data.model.Website;
import com.aris.templateapp.data.model.WebsiteQuery;
import com.aris.templateapp.data.model.Workspace;
import com.aris.templateapp.databinding.FragmentCreatorHomeBinding;
import com.aris.templateapp.databinding.ItemRowBinding;
import com.aris.templateapp.ui.common.CurrentUserViewModel;
import com.aris.templateapp.ui.common.StatusBannerView;
import com.aris.templateapp.ui.guide.Guide;
import com.aris.templateapp.ui.guide.GuideFragment;
import com.aris.templateapp.ui.guide.GuideStore;
import com.aris.templateapp.ui.website.WebsiteArgs;
import com.aris.templateapp.ui.website.WebsiteSmallAdapter;
import com.aris.templateapp.ui.website.WebsiteUi;
import com.aris.templateapp.ui.workspace.WorkspaceViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Tab Beranda pembuat website (alur-pembuatan-website.md bagian 3). Isi menyesuaikan kondisi (3.2):
 * tamu → sapaan umum; ruang kerja belum punya website → sapaan besar + Mulai besar; sudah punya → "Hai, {nama}",
 * Lanjutkan, Mulai satu baris. Admin/Pemilik juga melihat banner perubahan & permintaan terbit.
 */
@AndroidEntryPoint
public class CreatorHomeFragment extends Fragment {

    @Inject
    ThumbnailLoader thumbnailLoader;

    @Inject
    GuideStore guideStore;

    private FragmentCreatorHomeBinding binding;
    private CreatorHomeViewModel viewModel;
    private CurrentUserViewModel userViewModel;
    private WebsiteSmallAdapter smallAdapter;
    private GalleryAdapter recommendAdapter;
    @Nullable private Workspace workspace;
    private boolean resumedBefore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCreatorHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(CreatorHomeViewModel.class);
        userViewModel = new ViewModelProvider(this).get(CurrentUserViewModel.class);

        if (shell().isProviderSuspended()) {
            binding.suspendedBanner.setVisibility(View.VISIBLE);
            binding.suspendedBanner.bind(StatusBannerView.Kind.ERROR, getString(R.string.provider_suspended_redirect),
                    R.drawable.ic_block);
        }

        StartOptions.bind(binding.startTemplate, binding.startCustom);
        binding.heroButton.setOnClickListener(v -> shell().showCreateSheet());
        binding.startTemplate.getRoot().setOnClickListener(v -> shell().start(PendingStart.Option.TEMPLATE));
        binding.startCustom.getRoot().setOnClickListener(v -> shell().start(PendingStart.Option.ASSEMBLED));
        binding.startTemplateButton.setOnClickListener(v -> shell().start(PendingStart.Option.TEMPLATE));
        binding.startCustomButton.setOnClickListener(v -> shell().start(PendingStart.Option.ASSEMBLED));
        binding.continueSeeAll.setOnClickListener(v -> shell().openWebsites(WebsiteQuery.Filter.ALL));
        binding.recommendSeeAll.setOnClickListener(v -> shell().openGallery(viewModel.getRecommendedCategory()));

        smallAdapter = new WebsiteSmallAdapter(thumbnailLoader, this::open);
        binding.continueList.setAdapter(smallAdapter);
        recommendAdapter = GalleryAdapter.cards(thumbnailLoader, template -> TemplateOpener.open(this, template));
        binding.recommendList.setAdapter(recommendAdapter);

        new ViewModelProvider(this).get(WorkspaceViewModel.class).getActive().observe(getViewLifecycleOwner(), active -> {
            workspace = userViewModel.isLoggedInNow() ? active : null;
            viewModel.setWorkspace(workspace);
            bindRecommendTitle();
        });
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> render());
        viewModel.getHome().observe(getViewLifecycleOwner(), home -> render());
        viewModel.getRecommendations().observe(getViewLifecycleOwner(), templates -> {
            binding.recommendSection.setVisibility(templates.isEmpty() ? View.GONE : View.VISIBLE);
            recommendAdapter.submitList(templates);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tab tampil lagi / kembali dari layar lain: anggota lain mungkin sudah mengubah website. Resume pertama
        // dilewati karena data baru saja dimuat saat ruang kerja aktif diketahui.
        if (resumedBefore) {
            viewModel.load();
        }
        resumedBefore = true;
        // Nama untuk "Hai, {nama}" dan status masuk/tamu dibaca ulang (mis. setelah masuk atau ubah nama).
        userViewModel.reload();
        bindGuides();
    }

    private CreatorDashboardFragment shell() {
        return (CreatorDashboardFragment) requireParentFragment();
    }

    private void render() {
        CreatorHomeViewModel.Home home = viewModel.getHome().getValue();
        User user = userViewModel.getUser().getValue();
        boolean guest = user == null;
        // Sapaan besar untuk tamu dan ruang kerja yang belum punya website; saat data belum datang tidak dipilih.
        boolean hasWebsites = !guest && home != null && Boolean.TRUE.equals(home.hasWebsites);
        boolean noWebsites = guest || (home != null && Boolean.FALSE.equals(home.hasWebsites));

        binding.hero.setVisibility(noWebsites ? View.VISIBLE : View.GONE);
        binding.heroTitle.setText(guest || workspace == null ? getString(R.string.home_hero_guest)
                : getString(R.string.home_hero_title, workspace.getGreetingName()));
        binding.startLarge.setVisibility(noWebsites && !guest ? View.VISIBLE : View.GONE);
        binding.hi.setVisibility(hasWebsites ? View.VISIBLE : View.GONE);
        if (hasWebsites) {
            binding.hi.setText(getString(R.string.home_hi, firstName(user.getDisplayName())));
        }
        binding.startCompact.setVisibility(hasWebsites ? View.VISIBLE : View.GONE);
        bindContinue(hasWebsites ? home.recent : null);
        bindBanners(guest ? null : home);
    }

    private void bindContinue(@Nullable List<Website> websites) {
        boolean show = websites != null && !websites.isEmpty();
        binding.continueSection.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) {
            return;
        }
        Website latest = websites.get(0);
        binding.continueLarge.name.setText(latest.name);
        binding.continueLarge.status.setText(WebsiteUi.compactStatus(requireContext(), latest));
        String info = WebsiteUi.info(requireContext(), latest);
        binding.continueLarge.info.setText(info);
        binding.continueLarge.info.setVisibility(info == null ? View.GONE : View.VISIBLE);
        binding.continueLarge.thumbnail.setImageDrawable(null);
        thumbnailLoader.load(latest.thumbnailUrl, binding.continueLarge.thumbnail, null);
        binding.continueLarge.getRoot().setOnClickListener(v -> open(latest));

        List<Website> others = websites.subList(1, websites.size());
        binding.continueList.setVisibility(others.isEmpty() ? View.GONE : View.VISIBLE);
        smallAdapter.submitList(others);
    }

    /** Permintaan terbit (paling mendesak) dan jumlah website dengan perubahan belum diterbitkan (bagian 3.2). */
    private void bindBanners(@Nullable CreatorHomeViewModel.Home home) {
        PublishRequest request = home == null || home.requests.isEmpty() ? null : home.requests.get(0);
        binding.requestBanner.setVisibility(request == null ? View.GONE : View.VISIBLE);
        if (request != null) {
            binding.requestBanner.bind(StatusBannerView.Kind.INFO, getString(R.string.home_banner_request,
                    request.requestedByName == null ? getString(R.string.activity_unknown_actor)
                            : request.requestedByName, request.websiteName), R.drawable.ic_send);
            binding.requestBanner.setAction(getString(R.string.action_review), v -> openRequest(request));
        }
        long changed = home == null ? 0 : home.changedCount;
        binding.changedBanner.setVisibility(changed > 0 ? View.VISIBLE : View.GONE);
        if (changed > 0) {
            binding.changedBanner.bind(StatusBannerView.Kind.WARNING,
                    getString(R.string.home_banner_changed, (int) changed));
            binding.changedBanner.setAction(getString(R.string.action_view),
                    v -> shell().openWebsites(WebsiteQuery.Filter.CHANGED));
        }
    }

    private void bindRecommendTitle() {
        // Ruang pribadi dan tamu tidak punya kategori organisasi, jadi judulnya "Template populer" (bagian 6).
        boolean organization = workspace != null && !workspace.isPersonal();
        binding.recommendTitle.setText(organization
                ? getString(R.string.home_recommend_for, workspace.getGreetingName())
                : getString(R.string.home_recommend_popular));
    }

    /**
     * Kartu Lanjutkan seharusnya langsung membuka editor (bagian 2.3). Editor yang tersinkron ke cloud baru ada di
     * fase T4, jadi sementara ini membuka halaman kelola website.
     */
    private void open(Website website) {
        NavHostFragment.findNavController(this).navigate(R.id.websiteManageFragment, WebsiteArgs.of(website));
    }

    private void openRequest(PublishRequest request) {
        if (workspace == null) {
            return;
        }
        Bundle args = WebsiteArgs.of(request.websiteId, workspace.getId(), request.websiteName);
        NavHostFragment.findNavController(this).navigate(R.id.websiteManageFragment, args);
    }

    /** "Rina Pratama" → "Rina". */
    static String firstName(String displayName) {
        String trimmed = displayName == null ? "" : displayName.trim();
        int space = trimmed.indexOf(' ');
        return space < 0 ? trimmed : trimmed.substring(0, space);
    }

    /** Dibaca ulang setiap tab tampil, agar titik "baru" hilang setelah panduan dibuka. */
    private void bindGuides() {
        binding.guideList.removeAllViews();
        for (Guide guide : Guide.forAudience(Guide.Audience.CREATOR)) {
            ItemRowBinding row = ItemRowBinding.inflate(getLayoutInflater(), binding.guideList, true);
            row.title.setText(guide.title);
            row.detail.setText(R.string.badge_coming_soon);
            row.detail.setVisibility(guide.available ? View.GONE : View.VISIBLE);
            boolean isNew = !guideStore.isOpened(guide);
            row.newDot.setVisibility(isNew ? View.VISIBLE : View.GONE);
            row.newDot.setContentDescription(isNew ? getString(R.string.cd_guide_new) : null);
            row.getRoot().setOnClickListener(v -> GuideFragment.open(this, guide));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        resumedBefore = false;
        binding = null;
    }
}
