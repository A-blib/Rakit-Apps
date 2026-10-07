package com.aris.templateapp.ui.provider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.remote.dto.TemplateDetailDto;
import com.aris.templateapp.data.remote.dto.TemplateDetailDto.CheckDto;
import com.aris.templateapp.data.remote.dto.TemplateDetailDto.IssueDto;
import com.aris.templateapp.databinding.FragmentTemplateDetailBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.upload.IssueListBinder;
import com.aris.templateapp.ui.upload.ReportIssueDialog;
import com.aris.templateapp.ui.upload.UploadNav;
import com.aris.templateapp.ui.upload.ZipPicker;

import java.util.Collections;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Detail template + hasil pengecekan terakhir (alur-provider.md bagian 4.5 & 6.2). Template yang tidak lolos punya
 * tombol "Upload file perbaikan"; draft punya tombol "Lanjutkan draft".
 */
@AndroidEntryPoint
public class TemplateDetailFragment extends Fragment {

    private FragmentTemplateDetailBinding binding;
    private final ZipPicker picker = new ZipPicker(this, (uri, name, size, templateId) ->
            UploadNav.startUpload(this, uri, name, size, templateId));
    private TemplateDetailViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTemplateDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.statViews.label.setText(R.string.summary_views);
        binding.statDownloads.label.setText(R.string.summary_downloads);

        viewModel = new ViewModelProvider(this).get(TemplateDetailViewModel.class);
        viewModel.getDetail().observe(getViewLifecycleOwner(), this::render);
        viewModel.start(requireArguments().getString(ProviderNav.ARG_TEMPLATE_ID, ""));
    }

    private void render(@Nullable Resource<TemplateDetailDto> resource) {
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
                binding.state.showError(ErrorMessages.forError(requireContext(), resource.getError()), viewModel::load);
                break;
            case SUCCESS:
            default:
                binding.state.hide(binding.content, () -> bind(resource.getData()));
                break;
        }
    }

    private void bind(TemplateDetailDto detail) {
        CheckDto check = detail.latestCheck;
        List<IssueDto> errors = check == null || check.errors == null ? Collections.emptyList() : check.errors;
        List<IssueDto> warnings = check == null || check.warnings == null ? Collections.emptyList() : check.warnings;

        binding.name.setText(detail.name);
        binding.version.setText(check == null ? null : getString(R.string.detail_version, check.version));
        binding.version.setVisibility(check == null ? View.GONE : View.VISIBLE);

        // Status + waktu pengecekan selesai, mis. "TIDAK LOLOS · 2 ERROR · 4 OKT 11.40".
        String status = TemplateUi.statusLine(requireContext(), detail.status, errors.size(), warnings.size());
        String finished = check == null ? "" : TemplateUi.dateTime(check.finishedAt);
        binding.status.setText(finished.isEmpty() ? status
                : getString(R.string.template_status_with_count, status, finished));
        binding.status.setTextColor(ContextCompat.getColor(requireContext(), statusColor(detail.status, warnings.size())));
        binding.category.setText(TemplateUi.categoryLabel(detail.category));
        binding.statViews.value.setText(TemplateUi.count(detail.views));
        binding.statDownloads.value.setText(TemplateUi.count(detail.downloads));

        String message = null;
        if (check == null) {
            message = getString(R.string.detail_no_check);
        } else if ("running".equals(check.status)) {
            message = getString(R.string.detail_check_running);
        } else if (errors.isEmpty() && warnings.isEmpty()) {
            message = getString(R.string.detail_check_passed);
        }
        binding.checkMessage.setText(message);
        binding.checkMessage.setVisibility(message == null ? View.GONE : View.VISIBLE);

        binding.errorsTitle.setText(getString(R.string.detail_errors_title, errors.size()));
        binding.errorsTitle.setVisibility(errors.isEmpty() ? View.GONE : View.VISIBLE);
        binding.warningsTitle.setText(getString(R.string.detail_warnings_title, warnings.size()));
        binding.warningsTitle.setVisibility(warnings.isEmpty() ? View.GONE : View.VISIBLE);
        IssueListBinder.Actions actions = new IssueListBinder.Actions() {
            @Override
            public void onLearn(IssueDto issue) {
                UploadNav.openHelp(TemplateDetailFragment.this, issue.code);
            }

            @Override
            public void onReport(IssueDto issue) {
                ReportIssueDialog.show(TemplateDetailFragment.this, reason -> viewModel.report(issue.id, reason));
            }
        };
        IssueListBinder.bind(binding.errorsList, errors, true, actions);
        IssueListBinder.bind(binding.warningsList, warnings, false, actions);
        bindUploadAction(detail);
    }

    /** Tidak lolos → pilih ZIP perbaikan; draft → lanjutkan wizard di langkah terakhir (alur-fitur-upload.md 3.1 & 5.4). */
    private void bindUploadAction(TemplateDetailDto detail) {
        boolean failed = TemplateUi.CHECK_FAILED.equals(detail.status);
        boolean draft = "draft".equals(detail.status);
        binding.uploadActionButton.setVisibility(failed || draft ? View.VISIBLE : View.GONE);
        if (failed) {
            binding.uploadActionButton.setText(R.string.upload_fix_button);
            binding.uploadActionButton.setOnClickListener(v -> picker.launch(detail.id));
        } else if (draft) {
            binding.uploadActionButton.setText(R.string.detail_continue_draft);
            binding.uploadActionButton.setOnClickListener(v -> viewModel.resumeDraft(step ->
                    UploadNav.resumeDraft(this, detail.id, step)));
        }
    }

    private static int statusColor(String status, int warningCount) {
        if (TemplateUi.CHECK_FAILED.equals(status)) {
            return R.color.color_error;
        }
        if (TemplateUi.PUBLISHED.equals(status) && warningCount > 0) {
            return R.color.color_warning;
        }
        return R.color.color_muted;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
