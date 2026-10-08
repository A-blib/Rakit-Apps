package com.aris.templateapp.ui.upload;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aris.templateapp.R;
import com.aris.templateapp.core.upload.FileSizes;
import com.aris.templateapp.core.upload.ZipQuickCheck;
import com.aris.templateapp.data.remote.dto.TemplateDetailDto.CheckDto;
import com.aris.templateapp.data.remote.dto.TemplateDetailDto.IssueDto;
import com.aris.templateapp.data.remote.dto.UploadCheckDto;
import com.aris.templateapp.databinding.FragmentUploadCheckBinding;
import com.aris.templateapp.databinding.ItemUploadStageBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.guide.Guide;
import com.aris.templateapp.ui.guide.GuideFragment;
import com.aris.templateapp.ui.upload.UploadCheckState.Phase;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Langkah 2 Upload: cek kilat di HP, upload dengan progress bar, daftar tahap pengecekan yang dicentang satu per satu
 * (bagian 5.8), tahap C di HP, lalu hasil "Belum memenuhi standar" (bagian 5.4) atau lolos.
 */
@AndroidEntryPoint
public class UploadCheckFragment extends Fragment {

    /** Urutan tahap di layar = urutan nilai {@code stage} dari server, ditambah tahap C di HP. */
    private static final List<String> STAGES = Arrays.asList("uploaded", "opening_zip", "structure", "html_library",
            "size", "device");
    private static final int[] STAGE_LABELS = {R.string.upload_stage_upload, R.string.upload_stage_open,
            R.string.upload_stage_structure, R.string.upload_stage_html, R.string.upload_stage_size,
            R.string.upload_stage_device};

    private FragmentUploadCheckBinding binding;
    private UploadCheckViewModel viewModel;
    private OffscreenPage offscreenPage;
    private final ZipPicker picker = new ZipPicker(this, (uri, name, size, templateId) ->
            UploadNav.startUpload(this, uri, name, size, templateId));

    private final OnBackPressedCallback back = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            close();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUploadCheckBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        WizardHeader.bind(binding.header, 2, R.string.upload_step_check, this::close);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), back);
        offscreenPage = new OffscreenPage(binding.hostFrame);

        viewModel = new ViewModelProvider(this).get(UploadCheckViewModel.class);
        viewModel.getState().observe(getViewLifecycleOwner(), this::render);
        viewModel.getMessage().observe(getViewLifecycleOwner(), event -> {
            Integer text = event.getContentIfNotHandled();
            if (text != null) {
                Snackbar.make(binding.getRoot(), text, Snackbar.LENGTH_LONG).show();
            }
        });
        viewModel.getDeviceCheckRequest().observe(getViewLifecycleOwner(), event -> {
            UploadCheckViewModel.DeviceCheckRequest request = event.getContentIfNotHandled();
            if (request != null) {
                new DeviceChecker(requireContext(), offscreenPage)
                        .run(request.siteRoot, request.allowedHosts, request.pages, viewModel::onDeviceChecked);
            }
        });

        Bundle args = requireArguments();
        String uri = args.getString(UploadNav.ARG_URI);
        if (uri != null) {
            viewModel.startUpload(Uri.parse(uri), args.getString(UploadNav.ARG_FILE_NAME, ""),
                    args.getLong(UploadNav.ARG_FILE_SIZE), args.getString(UploadNav.ARG_TEMPLATE_ID), isMetered());
        } else {
            viewModel.monitor(args.getString(UploadNav.ARG_TEMPLATE_ID, ""));
        }
    }

    private boolean isMetered() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.isActiveNetworkMetered();
    }

    private void render(UploadCheckState state) {
        if (state == null) {
            return;
        }
        UploadCheckDto result = state.result;
        String fileName = result != null && result.fileName != null ? result.fileName : viewModel.getFileName();
        long fileSize = result != null && result.fileSize != null ? result.fileSize : viewModel.getFileSize();
        binding.fileMeta.setText(fileName == null ? null
                : getString(R.string.upload_file_meta, fileName, FileSizes.format(fileSize)));
        binding.state.hide();
        binding.content.setVisibility(View.VISIBLE);

        boolean running = state.phase == Phase.LOCAL_CHECKING || state.phase == Phase.UPLOADING
                || state.phase == Phase.CHECKING || state.phase == Phase.DEVICE_CHECK;
        binding.stages.setVisibility(running ? View.VISIBLE : View.GONE);
        binding.hint.setVisibility(state.phase == Phase.CHECKING ? View.VISIBLE : View.GONE);
        binding.progressGroup.setVisibility(state.phase == Phase.UPLOADING ? View.VISIBLE : View.GONE);
        binding.resultGroup.setVisibility(View.GONE);
        showActions(0, null, 0, null);

        switch (state.phase) {
            case LOCAL_CHECKING:
            case UPLOADING:
                bindStages("uploaded");
                bindProgress(state);
                break;
            case CHECKING:
                bindStages(state.stage == null ? "uploaded" : state.stage);
                break;
            case DEVICE_CHECK:
                bindStages("device");
                break;
            case CONFIRM_MOBILE_DATA:
                bindStages("uploaded");
                confirmMobileData();
                break;
            case LOCAL_FAILED:
                bindLocalFailure(state.local);
                break;
            case UPLOAD_ERROR:
                bindUploadError(state);
                break;
            case LOAD_ERROR:
                binding.content.setVisibility(View.GONE);
                binding.state.setVisibility(View.VISIBLE);
                binding.state.showError(ErrorMessages.forError(requireContext(), state.error), viewModel::retry);
                break;
            case FAILED:
                bindFailed(result);
                break;
            case PASSED:
            default:
                bindPassed(result);
                break;
        }
    }

    private void bindStages(String current) {
        binding.stages.removeAllViews();
        int currentIndex = STAGES.indexOf(current);
        for (int i = 0; i < STAGES.size(); i++) {
            ItemUploadStageBinding row = ItemUploadStageBinding.inflate(getLayoutInflater(), binding.stages, true);
            row.label.setText(STAGE_LABELS[i]);
            boolean done = i < currentIndex;
            boolean active = i == currentIndex;
            row.spinner.setVisibility(active ? View.VISIBLE : View.GONE);
            row.icon.setVisibility(active ? View.INVISIBLE : View.VISIBLE);
            row.icon.setImageResource(done ? R.drawable.ic_check_circle : R.drawable.ic_radio_unchecked);
            row.label.setTextColor(requireContext().getColor(done || active ? R.color.color_foreground : R.color.color_muted));
        }
    }

    private void bindProgress(UploadCheckState state) {
        if (state.phase != Phase.UPLOADING) {
            return;
        }
        long total = Math.max(1, state.total);
        binding.progressBar.setProgress((int) (state.sent * 1000 / total));
        binding.progressText.setText(state.waitingAttempt > 0
                ? getString(R.string.upload_waiting_network, state.waitingAttempt)
                : getString(R.string.upload_progress, FileSizes.megabytes(state.sent), FileSizes.megabytes(state.total)));
    }

    private void confirmMobileData() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.upload_mobile_data_title)
                .setMessage(getString(R.string.upload_mobile_data_body, FileSizes.format(viewModel.getFileSize())))
                .setNegativeButton(R.string.action_cancel, (d, w) -> UploadNav.exit(this))
                .setPositiveButton(R.string.upload_continue, (d, w) -> viewModel.confirmMobileData())
                .setCancelable(false)
                .show();
    }

    private void bindLocalFailure(@Nullable ZipQuickCheck.Result local) {
        ZipQuickCheck.Problem problem = local == null ? ZipQuickCheck.Problem.CORRUPT : local.problem;
        String message;
        String suggestion = null;
        switch (problem == null ? ZipQuickCheck.Problem.CORRUPT : problem) {
            case RAR:
                message = getString(R.string.upload_local_rar);
                break;
            case NOT_ZIP:
                message = getString(R.string.upload_local_not_zip);
                break;
            case NO_INDEX:
                message = local != null && local.deeperIndexFolder != null
                        ? getString(R.string.upload_local_no_index_deeper, local.deeperIndexFolder)
                        : getString(R.string.upload_local_no_index);
                break;
            case TOO_LARGE:
                message = getString(R.string.upload_local_too_large, FileSizes.format(viewModel.getFileSize()),
                        FileSizes.format(viewModel.getMaxZipBytes()),
                        String.join(", ", local.largestFiles));
                suggestion = getString(R.string.upload_local_too_large_hint);
                break;
            case CORRUPT:
            default:
                message = getString(R.string.upload_local_corrupt);
                break;
        }
        showResult(getString(R.string.upload_local_failed_title), suggestion == null ? message : message + "\n\n" + suggestion,
                Collections.emptyList(), Collections.emptyList());
        String fixId = requireArguments().getString(UploadNav.ARG_TEMPLATE_ID);
        boolean rar = problem == ZipQuickCheck.Problem.RAR;
        showActions(R.string.upload_pick_other, () -> picker.launch(fixId),
                rar ? R.string.upload_see_how : R.string.upload_later,
                rar ? () -> GuideFragment.open(this, Guide.PREPARE_TEMPLATE) : () -> UploadNav.exit(this));
    }

    private void bindUploadError(UploadCheckState state) {
        boolean network = state.error != null && state.error.isNetworkError();
        showResult(getString(R.string.upload_local_failed_title), ErrorMessages.forError(requireContext(), state.error),
                Collections.emptyList(), Collections.emptyList());
        String fixId = requireArguments().getString(UploadNav.ARG_TEMPLATE_ID);
        if (network) {
            showActions(R.string.action_retry, viewModel::retry, R.string.upload_later, () -> UploadNav.exit(this));
        } else {
            showActions(R.string.upload_pick_other, () -> picker.launch(fixId), R.string.upload_later,
                    () -> UploadNav.exit(this));
        }
    }

    private void bindFailed(UploadCheckDto result) {
        CheckDto check = result.check;
        List<IssueDto> errors = check == null || check.errors == null ? Collections.emptyList() : check.errors;
        List<IssueDto> warnings = check == null || check.warnings == null ? Collections.emptyList() : check.warnings;
        showResult(getString(R.string.upload_failed_title), getString(R.string.upload_failed_body), errors, warnings);
        showActions(R.string.upload_fix_button, () -> picker.launch(result.templateId), R.string.upload_later,
                () -> UploadNav.exit(this));
    }

    private void bindPassed(UploadCheckDto result) {
        List<IssueDto> warnings = result.check == null || result.check.warnings == null
                ? Collections.emptyList() : result.check.warnings;
        String body = warnings.isEmpty() ? getString(R.string.upload_passed_body)
                : getString(R.string.upload_passed_warnings, warnings.size());
        if (warnings.isEmpty() && viewModel.consumeAutoContinue()) {
            UploadNav.replaceStep(this, R.id.uploadInfoFragment, result.templateId);
            return;
        }
        showResult(getString(R.string.upload_passed_title), body, Collections.emptyList(), warnings);
        showActions(R.string.upload_next, () -> UploadNav.replaceStep(this, R.id.uploadInfoFragment, result.templateId),
                0, null);
    }

    private void showResult(String title, @Nullable String body, List<IssueDto> errors, List<IssueDto> warnings) {
        binding.resultGroup.setVisibility(View.VISIBLE);
        binding.resultTitle.setText(title);
        binding.resultBody.setText(body);
        binding.resultBody.setVisibility(body == null ? View.GONE : View.VISIBLE);
        binding.errorsTitle.setVisibility(errors.isEmpty() ? View.GONE : View.VISIBLE);
        binding.errorsTitle.setText(getString(R.string.upload_errors_title, errors.size()));
        binding.warningsTitle.setVisibility(warnings.isEmpty() ? View.GONE : View.VISIBLE);
        binding.warningsTitle.setText(getString(R.string.upload_warnings_title, warnings.size()));
        IssueListBinder.Actions actions = new IssueListBinder.Actions() {
            @Override
            public void onLearn(IssueDto issue) {
                UploadNav.openHelp(UploadCheckFragment.this, issue.code);
            }

            @Override
            public void onReport(IssueDto issue) {
                ReportIssueDialog.show(UploadCheckFragment.this, reason -> viewModel.report(issue.id, reason));
            }
        };
        IssueListBinder.bind(binding.errorsList, errors, true, actions);
        IssueListBinder.bind(binding.warningsList, warnings, false, actions);
    }

    private void showActions(@StringRes int primary, @Nullable Runnable onPrimary, @StringRes int secondary,
                             @Nullable Runnable onSecondary) {
        boolean visible = primary != 0;
        binding.actions.setVisibility(visible ? View.VISIBLE : View.GONE);
        binding.actionsDivider.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (!visible) {
            return;
        }
        binding.primaryButton.setText(primary);
        binding.primaryButton.setOnClickListener(v -> onPrimary.run());
        binding.secondaryButton.setVisibility(secondary == 0 ? View.GONE : View.VISIBLE);
        if (secondary != 0) {
            binding.secondaryButton.setText(secondary);
            binding.secondaryButton.setOnClickListener(v -> onSecondary.run());
        }
    }

    /**
     * ✕ / kembali (bagian 3.2): saat upload berjalan, tanya dulu karena upload dibatalkan; setelah file lolos,
     * beri tahu bahwa pekerjaan tersimpan sebagai draft; selain itu langsung keluar (pengecekan tetap jalan di server).
     */
    private void close() {
        UploadCheckState state = viewModel.getState().getValue();
        Phase phase = state == null ? Phase.LOCAL_CHECKING : state.phase;
        if (phase == Phase.UPLOADING) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.upload_exit_title)
                    .setMessage(R.string.upload_exit_uploading_body)
                    .setNegativeButton(R.string.action_cancel, null)
                    .setPositiveButton(R.string.upload_exit, (d, w) -> {
                        viewModel.cancel();
                        UploadNav.exit(this);
                    })
                    .show();
        } else if (phase == Phase.PASSED || phase == Phase.DEVICE_CHECK) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.upload_exit_title)
                    .setMessage(R.string.upload_exit_draft_body)
                    .setNegativeButton(R.string.action_cancel, null)
                    .setPositiveButton(R.string.upload_exit, (d, w) -> UploadNav.exit(this))
                    .show();
        } else {
            UploadNav.exit(this);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        offscreenPage.destroy();
        binding = null;
    }
}
