package com.aris.templateapp.ui.creator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aris.templateapp.R;
import com.aris.templateapp.data.local.ProjectEntity;
import com.aris.templateapp.data.repository.GalleryRepository;
import com.aris.templateapp.databinding.FragmentCreatorHomeBinding;
import com.aris.templateapp.databinding.ItemRowBinding;
import com.aris.templateapp.ui.common.StatusBannerView;
import com.aris.templateapp.ui.editor.EditorNav;
import com.aris.templateapp.ui.guide.Guide;
import com.aris.templateapp.ui.guide.GuideFragment;
import com.aris.templateapp.ui.guide.GuideStore;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Tab Beranda pembuat website (alur-pembuatan-website.md bagian 3): titik masuk + ringkasan. Belum punya project →
 * "Mulai" besar di atas; sudah punya → "Lanjutkan project" di atas dan "Mulai" menjadi satu baris (bagian 3.2).
 */
@AndroidEntryPoint
public class CreatorHomeFragment extends Fragment {

    @Inject
    GuideStore guideStore;

    @Inject
    GalleryRepository galleryRepository;

    private FragmentCreatorHomeBinding binding;
    private CreatorHomeViewModel viewModel;
    private ProjectSmallAdapter smallAdapter;
    private GalleryAdapter recommendAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCreatorHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(CreatorHomeViewModel.class);

        if (shell().isProviderSuspended()) {
            binding.suspendedBanner.setVisibility(View.VISIBLE);
            binding.suspendedBanner.bind(StatusBannerView.Kind.ERROR, getString(R.string.provider_suspended_redirect),
                    R.drawable.ic_block);
        }

        StartOptions.bind(binding.startTemplate, binding.startCustom);
        binding.startTemplate.getRoot().setOnClickListener(v -> shell().selectTab(R.id.tab_creator_gallery));
        binding.startCustom.getRoot().setOnClickListener(v -> EditorNav.openCustomEditor(this, null));
        binding.startTemplateButton.setOnClickListener(v -> shell().selectTab(R.id.tab_creator_gallery));
        binding.startCustomButton.setOnClickListener(v -> EditorNav.openCustomEditor(this, null));
        binding.continueSeeAll.setOnClickListener(v -> shell().selectTab(R.id.tab_creator_projects));
        binding.recommendSeeAll.setOnClickListener(v -> shell().openGallery(viewModel.getRecommendedCategory()));

        smallAdapter = new ProjectSmallAdapter(project -> EditorNav.openProject(this, project));
        binding.continueList.setAdapter(smallAdapter);
        recommendAdapter = GalleryAdapter.cards(template -> TemplateOpener.open(this, galleryRepository, template));
        binding.recommendList.setAdapter(recommendAdapter);

        viewModel.getRecent().observe(getViewLifecycleOwner(), this::bindRecent);
        viewModel.getRecommendations().observe(getViewLifecycleOwner(), templates -> {
            binding.recommendSection.setVisibility(templates.isEmpty() ? View.GONE : View.VISIBLE);
            recommendAdapter.submitList(templates);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tab tampil / kembali dari editor atau layar lain: perbarui rekomendasi (mis. tujuan website diubah).
        viewModel.loadRecommendations();
        bindGuides();
    }

    private CreatorDashboardFragment shell() {
        return (CreatorDashboardFragment) requireParentFragment();
    }

    private void bindRecent(List<ProjectEntity> projects) {
        boolean hasProjects = !projects.isEmpty();
        binding.startLarge.setVisibility(hasProjects ? View.GONE : View.VISIBLE);
        binding.continueSection.setVisibility(hasProjects ? View.VISIBLE : View.GONE);
        binding.startCompact.setVisibility(hasProjects ? View.VISIBLE : View.GONE);
        if (!hasProjects) {
            return;
        }
        ProjectEntity latest = projects.get(0);
        binding.continueLarge.name.setText(latest.name);
        binding.continueLarge.status.setText(ProjectUi.compactStatus(requireContext(), latest));
        ProjectUi.bindInfo(binding.continueLarge.info, latest);
        binding.continueLarge.getRoot().setOnClickListener(v -> EditorNav.openProject(this, latest));

        List<ProjectEntity> others = projects.subList(1, projects.size());
        binding.continueList.setVisibility(others.isEmpty() ? View.GONE : View.VISIBLE);
        smallAdapter.submitList(others);
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
        binding = null;
    }
}
