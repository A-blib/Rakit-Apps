package com.aris.templateapp.ui.provider;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.core.upload.FileSizes;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.remote.dto.UploadOverviewDto;
import com.aris.templateapp.data.remote.dto.UploadOverviewDto.CheckingDto;
import com.aris.templateapp.data.remote.dto.UploadOverviewDto.DraftItemDto;
import com.aris.templateapp.data.remote.dto.UploadOverviewDto.NeedsFixDto;
import com.aris.templateapp.databinding.FragmentProviderUploadBinding;
import com.aris.templateapp.databinding.ItemUploadCardBinding;
import com.aris.templateapp.databinding.SheetBeforeUploadBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.guide.Guide;
import com.aris.templateapp.ui.upload.UploadNav;
import com.aris.templateapp.ui.upload.WizardHeader;
import com.aris.templateapp.ui.upload.ZipPicker;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Halaman awal tab Upload (alur-fitur-upload.md bagian 3.1). Tidak langsung membuka pemilih file; isinya
 * menyesuaikan kondisi provider dengan urutan Perlu diperbaiki → Lanjutkan draft → Upload template baru.
 */
@AndroidEntryPoint
public class ProviderUploadFragment extends Fragment {

    @Inject
    ThumbnailLoader thumbnailLoader;

    private FragmentProviderUploadBinding binding;
    private ProviderUploadViewModel viewModel;
    private final ZipPicker picker = new ZipPicker(this, (uri, name, size, templateId) ->
            UploadNav.startUpload(this, uri, name, size, templateId));

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProviderUploadBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ProviderUploadViewModel.class);
        viewModel.getOverview().observe(getViewLifecycleOwner(), this::render);
        viewModel.getMaxZipBytes().observe(getViewLifecycleOwner(), max ->
                binding.dropLimit.setText(getString(R.string.upload_pick_limit, FileSizes.format(max))));
        viewModel.getMessage().observe(getViewLifecycleOwner(), event -> {
            Integer text = event.getContentIfNotHandled();
            if (text != null) {
                Snackbar.make(binding.getRoot(), text, Snackbar.LENGTH_SHORT).show();
            }
        });
        binding.dropZone.setOnClickListener(v -> picker.launch(null));
        binding.beforeUpload.guideButton.setOnClickListener(v -> ProviderNav.openGuide(this, Guide.CHECK_RULES));
        binding.newUploadButton.setOnClickListener(v -> showBeforeUpload());
    }

    /** Tab ini baru dibuka (lihat ProviderDashboardFragment): muat ulang agar status pengecekan selalu terbaru. */
    @Override
    public void onResume() {
        super.onResume();
        viewModel.refresh();
    }

    private void render(@Nullable Resource<UploadOverviewDto> resource) {
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
                binding.state.showError(ErrorMessages.forError(requireContext(), resource.getError()), viewModel::refresh);
                break;
            case SUCCESS:
            default:
                binding.state.hide(binding.content, () -> bind(resource.getData()));
                break;
        }
    }

    private void bind(UploadOverviewDto data) {
        boolean empty = data.isEmpty();
        binding.emptyGroup.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.listGroup.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            return;
        }
        bindNeedsFix(data.needsFix);
        bindChecking(data.checking);
        bindDrafts(data.drafts);
        binding.draftQuota.setText(getString(R.string.upload_draft_quota, data.draftCount, data.draftLimit));
        binding.newUploadButton.setEnabled(data.canStartNew);
        binding.quotaFull.setVisibility(data.canStartNew ? View.GONE : View.VISIBLE);
        binding.quotaFull.setText(getString(R.string.upload_quota_full, data.draftLimit));
    }

    private void bindNeedsFix(@Nullable List<NeedsFixDto> items) {
        LinearLayout list = binding.needsFixList;
        list.removeAllViews();
        int count = items == null ? 0 : items.size();
        binding.needsFixTitle.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
        binding.needsFixTitle.setText(getString(R.string.upload_needs_fix_title, count));
        if (items == null) {
            return;
        }
        for (NeedsFixDto item : items) {
            ItemUploadCardBinding card = ItemUploadCardBinding.inflate(getLayoutInflater(), list, true);
            card.leadingIcon.setVisibility(View.VISIBLE);
            card.leadingIcon.setImageResource(R.drawable.ic_error);
            card.leadingIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                    R.color.color_error)));
            card.title.setText(item.fileName);
            card.line1.setText(getString(R.string.upload_issue_counts, item.errorCount, item.warningCount));
            card.line2.setText(TemplateUi.updatedAgo(requireContext(), item.updatedAt));
            card.actionButton.setVisibility(View.VISIBLE);
            card.actionButton.setText(R.string.upload_fix_button);
            card.actionButton.setOnClickListener(v -> picker.launch(item.templateId));
            card.getRoot().setOnClickListener(v -> UploadNav.openCheck(this, item.templateId));
            bindMenu(card, item.fileName, item.templateId, R.string.upload_delete_failed);
        }
    }

    private void bindChecking(@Nullable List<CheckingDto> items) {
        LinearLayout list = binding.checkingList;
        list.removeAllViews();
        boolean empty = items == null || items.isEmpty();
        binding.checkingTitle.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            return;
        }
        for (CheckingDto item : items) {
            ItemUploadCardBinding card = ItemUploadCardBinding.inflate(getLayoutInflater(), list, true);
            card.spinner.setVisibility(View.VISIBLE);
            card.title.setText(item.fileName);
            card.line1.setText(R.string.upload_checking_title);
            card.line2.setVisibility(View.GONE);
            card.getRoot().setOnClickListener(v -> UploadNav.openCheck(this, item.templateId));
        }
    }

    private void bindDrafts(@Nullable List<DraftItemDto> items) {
        LinearLayout list = binding.draftsList;
        list.removeAllViews();
        boolean empty = items == null || items.isEmpty();
        binding.draftsHeader.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            return;
        }
        for (DraftItemDto item : items) {
            ItemUploadCardBinding card = ItemUploadCardBinding.inflate(getLayoutInflater(), list, true);
            card.thumbnailFrame.setVisibility(View.VISIBLE);
            card.thumbnailPlaceholder.setVisibility(View.VISIBLE);
            thumbnailLoader.load(item.thumbnailUrl, card.thumbnail,
                    () -> card.thumbnailPlaceholder.setVisibility(View.GONE));
            card.title.setText(item.name);
            card.line1.setText(getString(R.string.upload_draft_step, item.wizardStep,
                    getString(WizardHeader.stepName(item.wizardStep))));
            card.line2.setText(TemplateUi.updatedAgo(requireContext(), item.updatedAt));
            if (item.expiringSoon) {
                long days = daysUntil(item.deleteAt);
                card.warning.setVisibility(View.VISIBLE);
                card.warning.setText(days <= 0 ? getString(R.string.upload_draft_expiring_today)
                        : getString(R.string.upload_draft_expiring, days));
            }
            card.getRoot().setOnClickListener(v -> UploadNav.resumeDraft(this, item.templateId, item.wizardStep));
            bindMenu(card, item.name, item.templateId, R.string.upload_delete_draft);
        }
    }

    private static long daysUntil(String iso) {
        try {
            return Duration.between(Instant.now(), Instant.parse(iso)).toDays();
        } catch (DateTimeParseException | NullPointerException e) {
            return 0;
        }
    }

    private void bindMenu(ItemUploadCardBinding card, String name, String templateId, int deleteLabel) {
        card.moreButton.setVisibility(View.VISIBLE);
        card.moreButton.setContentDescription(getString(R.string.cd_upload_more, name));
        card.moreButton.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(requireContext(), v);
            menu.getMenu().add(deleteLabel);
            menu.setOnMenuItemClickListener(item -> {
                confirmDelete(name, templateId);
                return true;
            });
            menu.show();
        });
    }

    private void confirmDelete(String name, String templateId) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.upload_delete_title, name))
                .setMessage(R.string.upload_delete_body)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (d, w) -> viewModel.delete(templateId))
                .show();
    }

    /** Kondisi B: checklist "Sebelum upload" tampil dulu, baru pemilih file (bagian 3.1). */
    private void showBeforeUpload() {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        SheetBeforeUploadBinding sheetBinding = SheetBeforeUploadBinding.inflate(getLayoutInflater());
        sheetBinding.beforeUpload.guideButton.setOnClickListener(v -> {
            sheet.dismiss();
            ProviderNav.openGuide(this, Guide.CHECK_RULES);
        });
        sheetBinding.pickButton.setOnClickListener(v -> {
            sheet.dismiss();
            picker.launch(null);
        });
        sheet.setContentView(sheetBinding.getRoot());
        sheet.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
