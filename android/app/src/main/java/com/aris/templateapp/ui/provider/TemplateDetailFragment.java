package com.aris.templateapp.ui.provider;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
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
import com.aris.templateapp.databinding.ItemIssueBinding;
import com.aris.templateapp.ui.common.ErrorMessages;

import java.util.Collections;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Detail template + hasil pengecekan terakhir (alur-provider.md bagian 4.5 & 6.2). Versi awal tanpa tombol aksi;
 * "Upload versi perbaikan" ditambahkan bersama fitur Upload.
 */
@AndroidEntryPoint
public class TemplateDetailFragment extends Fragment {

    private FragmentTemplateDetailBinding binding;
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

        bindIssues(binding.errorsTitle, binding.errorsList, getString(R.string.detail_errors_title, errors.size()),
                errors, R.drawable.ic_error, R.color.color_error);
        bindIssues(binding.warningsTitle, binding.warningsList,
                getString(R.string.detail_warnings_title, warnings.size()), warnings, R.drawable.ic_warning,
                R.color.color_warning);
    }

    private void bindIssues(TextView title, LinearLayout list, String titleText, List<IssueDto> issues,
                            @DrawableRes int icon, @ColorRes int color) {
        list.removeAllViews();
        boolean empty = issues.isEmpty();
        title.setVisibility(empty ? View.GONE : View.VISIBLE);
        title.setText(titleText);
        for (IssueDto issue : issues) {
            ItemIssueBinding row = ItemIssueBinding.inflate(getLayoutInflater(), list, true);
            row.icon.setImageResource(icon);
            row.icon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color)));
            row.message.setText(issue.message);
            String location = issue.file == null ? null : issue.line == null ? issue.file
                    : getString(R.string.issue_location_line, issue.file, issue.line);
            row.location.setText(location);
            row.location.setVisibility(location == null ? View.GONE : View.VISIBLE);
            row.suggestion.setText(issue.suggestion);
            row.suggestion.setVisibility(issue.suggestion == null ? View.GONE : View.VISIBLE);
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
