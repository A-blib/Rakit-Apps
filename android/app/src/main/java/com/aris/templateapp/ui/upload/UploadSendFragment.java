package com.aris.templateapp.ui.upload;

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
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.data.remote.dto.DraftDto;
import com.aris.templateapp.data.remote.dto.TemplateDetailDto.IssueDto;
import com.aris.templateapp.data.remote.dto.UploadCheckDto;
import com.aris.templateapp.databinding.FragmentUploadSendBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.provider.ProviderTabRequest;
import com.aris.templateapp.ui.provider.ProviderTemplatesViewModel;
import com.aris.templateapp.ui.provider.TemplateUi;
import com.aris.templateapp.ui.upload.SendViewModel.Phase;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Langkah 6 Upload: Kirim (alur-fitur-upload.md bagian 9). Pengecekan akhir hampir pasti lolos karena file sudah lolos
 * di langkah 2 dan tandaan sudah dicek saat Simpan; jika tetap gagal, template kembali menjadi draft beserta masalahnya.
 */
@AndroidEntryPoint
public class UploadSendFragment extends Fragment {

    @Inject
    ThumbnailLoader thumbnailLoader;

    private FragmentUploadSendBinding binding;
    private SendViewModel viewModel;

    private final OnBackPressedCallback back = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            close();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUploadSendBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        WizardHeader.bind(binding.header, 6, R.string.upload_step_send, this::close);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), back);
        viewModel = new ViewModelProvider(this).get(SendViewModel.class);
        viewModel.getState().observe(getViewLifecycleOwner(), this::render);
        binding.rightsCheck.setOnCheckedChangeListener((v, checked) -> updateSendEnabled());
        binding.editInfoButton.setOnClickListener(v ->
                UploadNav.replaceStep(this, R.id.uploadInfoFragment, viewModel.templateId()));
        viewModel.start(requireArguments().getString(UploadNav.ARG_TEMPLATE_ID, ""));
    }

    private void render(@Nullable SendViewModel.State state) {
        if (state == null) {
            return;
        }
        if (state.phase == Phase.LOADING) {
            binding.content.setVisibility(View.GONE);
            binding.state.showLoading();
            return;
        }
        if (state.phase == Phase.ERROR) {
            binding.content.setVisibility(View.GONE);
            binding.state.showError(ErrorMessages.forError(requireContext(), state.error), viewModel::load);
            return;
        }
        binding.state.hide(binding.content, () -> bind(state));
    }

    private void bind(SendViewModel.State state) {
        DraftDto draft = state.draft;
        binding.cardName.setText(draft.name);
        binding.cardCategory.setText(TemplateUi.categoryLabel(draft.category));
        binding.cardDescription.setText(draft.description);
        thumbnailLoader.load(draft.thumbnailUrl, binding.thumbnail,
                () -> binding.thumbnailPlaceholder.setVisibility(View.GONE));

        UploadCheckDto check = state.check;
        List<IssueDto> errors = check == null || check.check == null || check.check.errors == null
                ? Collections.emptyList() : check.check.errors;
        List<IssueDto> warnings = check == null || check.check == null || check.check.warnings == null
                ? Collections.emptyList() : check.check.warnings;
        int pages = draft.techInfo == null || draft.techInfo.pages == null ? 1 : draft.techInfo.pages.size();
        int sections = state.marking.sections == null ? 0 : state.marking.sections.size();
        int fields = state.marking.fields == null ? 0 : state.marking.fields.size();
        binding.summary.setText(getString(R.string.send_summary, pages, sections, fields, warnings.size()));

        boolean infoIncomplete = infoProblem(draft);
        binding.infoProblem.setVisibility(infoIncomplete ? View.VISIBLE : View.GONE);
        binding.editInfoButton.setVisibility(infoIncomplete ? View.VISIBLE : View.GONE);

        IssueListBinder.Actions actions = new IssueListBinder.Actions() {
            @Override
            public void onLearn(IssueDto issue) {
                UploadNav.openHelp(UploadSendFragment.this, issue.code);
            }

            @Override
            public void onReport(IssueDto issue) {
                ReportIssueDialog.show(UploadSendFragment.this, reason -> viewModel.report(issue.id, reason));
            }
        };
        boolean failed = state.phase == Phase.FAILED;
        List<IssueDto> shownErrors = failed ? errors : Collections.emptyList();
        binding.errorsTitle.setVisibility(shownErrors.isEmpty() ? View.GONE : View.VISIBLE);
        binding.errorsTitle.setText(getString(R.string.upload_errors_title, shownErrors.size()));
        IssueListBinder.bind(binding.errorsList, shownErrors, true, actions);
        binding.warningsTitle.setVisibility(warnings.isEmpty() ? View.GONE : View.VISIBLE);
        binding.warningsTitle.setText(getString(R.string.upload_warnings_title, warnings.size()));
        IssueListBinder.bind(binding.warningsList, warnings, false, actions);

        boolean editable = state.phase == Phase.READY || state.phase == Phase.FAILED;
        binding.rightsCheck.setVisibility(editable ? View.VISIBLE : View.GONE);
        binding.statusGroup.setVisibility(editable && state.error == null && !failed ? View.GONE : View.VISIBLE);
        binding.progress.setVisibility(state.phase == Phase.SENDING || state.phase == Phase.CHECKING ? View.VISIBLE : View.GONE);
        switch (state.phase) {
            case SENDING:
            case CHECKING:
                setStatus(R.string.upload_step_send, getString(R.string.send_checking));
                setButtons(0, null, 0, null);
                break;
            case PUBLISHED:
                setStatus(R.string.send_published_title, getString(R.string.send_published_body, draft.name)
                        + (warnings.isEmpty() ? "" : "\n\n" + getString(R.string.send_published_warnings, warnings.size())));
                setButtons(R.string.send_done, () -> UploadNav.exit(this), R.string.send_open_templates, this::openTemplates);
                break;
            case FAILED:
                setStatus(R.string.send_failed_title, getString(R.string.send_failed_body));
                setButtons(R.string.send_back_mark, () ->
                                UploadNav.replaceStep(this, R.id.uploadMarkFragment, viewModel.templateId()),
                        R.string.send_button, viewModel::send);
                break;
            case READY:
            default:
                if (state.error != null) {
                    setStatus(R.string.send_failed_title, ErrorMessages.forError(requireContext(), state.error));
                }
                setButtons(R.string.upload_back, () ->
                                UploadNav.replaceStep(this, R.id.uploadTryFragment, viewModel.templateId()),
                        R.string.send_button, viewModel::send);
                break;
        }
        updateSendEnabled();
    }

    private static boolean infoProblem(DraftDto draft) {
        int name = draft.name == null ? 0 : draft.name.trim().length();
        int description = draft.description == null ? 0 : draft.description.trim().length();
        return name < UploadInfoFragment.NAME_MIN || draft.category == null
                || description < UploadInfoFragment.DESCRIPTION_MIN || draft.keywords == null || draft.keywords.isEmpty();
    }

    private void setStatus(@StringRes int title, String body) {
        binding.statusTitle.setText(title);
        binding.statusBody.setText(body);
    }

    private void setButtons(@StringRes int secondary, @Nullable Runnable onSecondary, @StringRes int primary,
                            @Nullable Runnable onPrimary) {
        binding.secondaryButton.setVisibility(secondary == 0 ? View.GONE : View.VISIBLE);
        binding.primaryButton.setVisibility(primary == 0 ? View.GONE : View.VISIBLE);
        if (secondary != 0) {
            binding.secondaryButton.setText(secondary);
            binding.secondaryButton.setOnClickListener(v -> onSecondary.run());
        }
        if (primary != 0) {
            binding.primaryButton.setText(primary);
            binding.primaryButton.setOnClickListener(v -> onPrimary.run());
        }
    }

    /** Tombol Kirim aktif setelah info lengkap dan hak pakai aset dicentang (bagian 9). */
    private void updateSendEnabled() {
        SendViewModel.State state = viewModel.getState().getValue();
        if (state == null || state.draft == null) {
            return;
        }
        boolean sendable = state.phase == Phase.READY || state.phase == Phase.FAILED;
        if (sendable) {
            binding.primaryButton.setEnabled(binding.rightsCheck.isChecked() && !infoProblem(state.draft));
        } else {
            binding.primaryButton.setEnabled(true);
        }
    }

    private void openTemplates() {
        new ViewModelProvider(requireActivity()).get(ProviderTabRequest.class)
                .open(R.id.tab_templates, ProviderTemplatesViewModel.STATUS_PUBLISHED);
        UploadNav.exit(this);
    }

    private void close() {
        SendViewModel.State state = viewModel.getState().getValue();
        if (state != null && state.phase == Phase.PUBLISHED) {
            UploadNav.exit(this);
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.upload_exit_title)
                .setMessage(R.string.upload_exit_draft_body)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.upload_exit, (d, w) -> UploadNav.exit(this))
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
