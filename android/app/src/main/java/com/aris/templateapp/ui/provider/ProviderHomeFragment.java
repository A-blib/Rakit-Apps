package com.aris.templateapp.ui.provider;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aris.templateapp.R;
import com.aris.templateapp.ui.guide.Guide;
import com.aris.templateapp.ui.guide.GuideStore;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.remote.dto.ProviderDashboardDto;
import com.aris.templateapp.data.remote.dto.ProviderDashboardDto.ActionItemDto;
import com.aris.templateapp.data.remote.dto.ProviderDashboardDto.PopularTemplateDto;
import com.aris.templateapp.databinding.FragmentProviderHomeBinding;
import com.aris.templateapp.databinding.ItemPopularRowBinding;
import com.aris.templateapp.databinding.ItemRowBinding;
import com.aris.templateapp.databinding.ItemStatBoxBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Tab Beranda (alur-provider.md bagian 3). Data diambil dari {@link ProviderDashboardViewModel} milik shell,
 * sehingga shell dan tab ini memakai satu request yang sama.
 */
@AndroidEntryPoint
public class ProviderHomeFragment extends Fragment {

    private static final int CHECKLIST_STEPS = 3;

    @Inject
    GuideStore guideStore;

    private FragmentProviderHomeBinding binding;
    private ProviderDashboardViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProviderHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireParentFragment()).get(ProviderDashboardViewModel.class);
        viewModel.getDashboard().observe(getViewLifecycleOwner(), this::render);
        viewModel.getRefreshFailed().observe(getViewLifecycleOwner(), event -> {
            var error = event.getContentIfNotHandled();
            if (error != null) {
                Snackbar.make(binding.getRoot(), ErrorMessages.forError(requireContext(), error),
                        Snackbar.LENGTH_LONG).show();
            }
        });

        binding.periodGroup.setOnCheckedStateChangeListener((group, checkedIds) -> viewModel.setPeriod(
                checkedIds.contains(R.id.period_30)
                        ? ProviderDashboardViewModel.PERIOD_30_DAYS : ProviderDashboardViewModel.PERIOD_7_DAYS));
        binding.uploadButton.setOnClickListener(v -> shell().selectTab(R.id.tab_upload));
        binding.seeAllButton.setOnClickListener(v -> shell().openTemplates(ProviderTemplatesViewModel.STATUS_NEEDS_FIX));
        bindStatLabels();
    }

    /**
     * Tab Beranda tampil lagi: dibuka lewat bottom navigation/geser, kembali dari detail atau panduan,
     * atau app kembali dari latar belakang. Data diperbarui diam-diam (badge ikut diperbarui).
     */
    @Override
    public void onResume() {
        super.onResume();
        // Termasuk saat kembali dari wizard Upload: angka Aktif & Perlu tindakan bisa sudah berubah.
        viewModel.refreshIfStale();
    }

    private ProviderDashboardFragment shell() {
        return (ProviderDashboardFragment) requireParentFragment();
    }

    private void render(@Nullable Resource<ProviderDashboardDto> resource) {
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
                binding.state.showError(ErrorMessages.forError(requireContext(), resource.getError()),
                        () -> viewModel.load(false));
                break;
            case SUCCESS:
            default:
                binding.state.hide(binding.content, () -> bind(resource.getData()));
                break;
        }
    }

    private void bind(ProviderDashboardDto data) {
        binding.greeting.setText(getString(R.string.home_greeting, data.creatorName));
        bindActionItems(data.actionItems);

        binding.checklistCard.setVisibility(data.hasTemplates ? View.GONE : View.VISIBLE);
        binding.statsSection.setVisibility(data.hasTemplates ? View.VISIBLE : View.GONE);
        binding.uploadButton.setText(data.hasTemplates ? R.string.action_upload_new : R.string.action_upload_first);
        if (data.hasTemplates) {
            bindStats(data);
        } else {
            bindChecklist(data.profileComplete);
        }
        bindGuides();
    }

    // ---- Perlu tindakan -------------------------------------------------------------------------

    private void bindActionItems(@Nullable List<ActionItemDto> items) {
        boolean empty = items == null || items.isEmpty();
        binding.actionSection.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.actionList.removeAllViews();
        if (empty) {
            return;
        }
        binding.actionCount.setText(String.valueOf(items.size()));
        boolean anyTemplate = false;
        for (ActionItemDto item : items) {
            ItemRowBinding row = addRow(binding.actionList);
            switch (item.kind) {
                case ActionItemDto.CHECK_FAILED:
                    setIcon(row, R.drawable.ic_error, R.color.color_error);
                    setTexts(row, item.templateName, TemplateUi.statusLine(requireContext(),
                            TemplateUi.CHECK_FAILED, item.errorCount, 0));
                    break;
                case ActionItemDto.WARNING:
                    setIcon(row, R.drawable.ic_warning, R.color.color_warning);
                    setTexts(row, item.templateName, TemplateUi.statusLine(requireContext(),
                            TemplateUi.PUBLISHED, 0, item.warningCount));
                    break;
                case ActionItemDto.DRAFT:
                    setIcon(row, R.drawable.ic_info, R.color.color_muted);
                    setTexts(row, item.templateName, getString(R.string.action_draft_detail));
                    break;
                case ActionItemDto.PROFILE_INCOMPLETE:
                default:
                    setIcon(row, R.drawable.ic_person, R.color.color_muted);
                    setTexts(row, getString(R.string.action_profile_incomplete), null);
                    row.getRoot().setOnClickListener(v -> ProviderNav.openEditProfile(this));
                    continue;
            }
            anyTemplate = true;
            String templateId = item.templateId;
            // Draft dilanjutkan di wizard Upload; template lain membuka detail + hasil pengecekan.
            row.getRoot().setOnClickListener(v -> {
                if (ActionItemDto.DRAFT.equals(item.kind)) {
                    shell().selectTab(R.id.tab_upload);
                } else {
                    ProviderNav.openTemplate(this, templateId);
                }
            });
        }
        binding.seeAllButton.setVisibility(anyTemplate ? View.VISIBLE : View.GONE);
    }

    // ---- Checklist provider baru ----------------------------------------------------------------

    private void bindChecklist(boolean profileComplete) {
        boolean guideRead = guideStore.anyOpened(Guide.Audience.PROVIDER);
        int done = (profileComplete ? 1 : 0) + (guideRead ? 1 : 0);
        binding.checklistProgress.setText(getString(R.string.checklist_progress, done, CHECKLIST_STEPS));
        binding.checklistBar.setProgressCompat(done * 100 / CHECKLIST_STEPS, false);

        binding.checklistSteps.removeAllViews();
        ItemRowBinding profile = addChecklistStep(R.string.checklist_profile, profileComplete);
        profile.getRoot().setOnClickListener(v -> ProviderNav.openEditProfile(this));
        ItemRowBinding guide = addChecklistStep(R.string.checklist_guide, guideRead);
        guide.getRoot().setOnClickListener(v -> ProviderNav.openGuide(this, Guide.PREPARE_TEMPLATE));
        ItemRowBinding upload = addChecklistStep(R.string.checklist_upload, false);
        upload.getRoot().setOnClickListener(v -> shell().selectTab(R.id.tab_upload));
    }

    private ItemRowBinding addChecklistStep(int text, boolean done) {
        ItemRowBinding row = addRow(binding.checklistSteps);
        setIcon(row, done ? R.drawable.ic_check_circle : R.drawable.ic_radio_unchecked,
                done ? R.color.color_success : R.color.color_border_strong);
        row.icon.setContentDescription(getString(done ? R.string.cd_step_done : R.string.cd_step_todo));
        row.icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        setTexts(row, getString(text), null);
        return row;
    }

    // ---- Ringkasan, grafik, populer -------------------------------------------------------------

    private void bindStatLabels() {
        setStat(binding.statActive, R.string.summary_active, 0);
        setStat(binding.statViews, R.string.summary_views, 0);
        setStat(binding.statDownloads, R.string.summary_downloads, 0);
    }

    private void bindStats(ProviderDashboardDto data) {
        boolean month = ProviderDashboardViewModel.PERIOD_30_DAYS.equals(viewModel.getPeriod());
        binding.periodGroup.check(month ? R.id.period_30 : R.id.period_7);

        if (data.summary != null) {
            setStat(binding.statActive, R.string.summary_active, data.summary.active);
            setStat(binding.statViews, R.string.summary_views, data.summary.views);
            setStat(binding.statDownloads, R.string.summary_downloads, data.summary.downloads);
        }

        binding.chart.setPoints(DownloadTrendChart.toPoints(data.downloadTrend),
                DownloadTrendChart.labelEvery(data.downloadTrend == null ? 0 : data.downloadTrend.size()));
        binding.trendEmpty.setVisibility(DownloadTrendChart.isEmpty(data.downloadTrend) ? View.VISIBLE : View.GONE);

        binding.popularList.removeAllViews();
        boolean noPopular = data.popular == null || data.popular.isEmpty();
        binding.popularCard.setVisibility(noPopular ? View.GONE : View.VISIBLE);
        binding.popularEmpty.setVisibility(noPopular ? View.VISIBLE : View.GONE);
        if (!noPopular) {
            for (PopularTemplateDto item : data.popular) {
                ItemPopularRowBinding row = ItemPopularRowBinding.inflate(getLayoutInflater(), binding.popularList, true);
                row.rank.setText(String.valueOf(item.rank));
                row.name.setText(item.name);
                row.downloads.setText(getString(R.string.popular_downloads, item.downloads));
                String id = item.id;
                row.getRoot().setOnClickListener(v -> ProviderNav.openTemplate(this, id));
            }
        }
    }

    private void setStat(ItemStatBoxBinding box, int label, long value) {
        box.label.setText(label);
        box.value.setText(TemplateUi.count(value));
    }

    // ---- Panduan --------------------------------------------------------------------------------

    private void bindGuides() {
        binding.guideList.removeAllViews();
        for (Guide guide : Guide.forAudience(Guide.Audience.PROVIDER)) {
            ItemRowBinding row = addRow(binding.guideList);
            // Label "Segera hadir" di baris keterangan agar judul panjang tidak terlipat di samping pil.
            setTexts(row, getString(guide.title), guide.available ? null : getString(R.string.badge_coming_soon));
            boolean isNew = !guideStore.isOpened(guide);
            row.newDot.setVisibility(isNew ? View.VISIBLE : View.GONE);
            row.newDot.setContentDescription(isNew ? getString(R.string.cd_guide_new) : null);
            row.getRoot().setOnClickListener(v -> ProviderNav.openGuide(this, guide));
        }
    }

    // ---- Pembantu baris -------------------------------------------------------------------------

    private ItemRowBinding addRow(LinearLayout parent) {
        return ItemRowBinding.inflate(getLayoutInflater(), parent, true);
    }

    private void setIcon(ItemRowBinding row, @DrawableRes int icon, @ColorRes int tint) {
        row.icon.setVisibility(View.VISIBLE);
        row.icon.setImageResource(icon);
        row.icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), tint)));
    }

    private static void setTexts(ItemRowBinding row, CharSequence title, @Nullable CharSequence detail) {
        row.title.setText(title);
        row.detail.setText(detail);
        row.detail.setVisibility(detail == null ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
