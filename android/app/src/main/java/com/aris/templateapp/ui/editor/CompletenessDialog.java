package com.aris.templateapp.ui.editor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aris.templateapp.R;
import com.aris.templateapp.core.template.CompletenessChecker;
import com.aris.templateapp.databinding.DialogCompletenessBinding;
import com.aris.templateapp.databinding.ItemRowBinding;
import com.aris.templateapp.ui.creator.ProjectUi;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Layar Kelengkapan (alur-buat-website-via-template.md bagian 7.2). Dibuka sebagai dialog layar penuh di atas editor
 * agar memakai ViewModel editor yang sama (nilai terbaru, termasuk yang belum tersimpan).
 */
public class CompletenessDialog extends DialogFragment {

    private static final String TAG = "kelengkapan";

    private DialogCompletenessBinding binding;
    private TemplateEditorViewModel viewModel;

    static void show(Fragment editor) {
        if (editor.getChildFragmentManager().findFragmentByTag(TAG) == null) {
            new CompletenessDialog().show(editor.getChildFragmentManager(), TAG);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.Theme_App_FullScreenDialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogCompletenessBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireParentFragment()).get(TemplateEditorViewModel.class);
        binding.toolbar.setNavigationOnClickListener(v -> dismiss());
        viewModel.getHeader().observe(getViewLifecycleOwner(), this::render);
    }

    private void render(@Nullable TemplateEditorViewModel.Header header) {
        if (header == null) {
            return;
        }
        CompletenessChecker.Result result = header.completeness;
        binding.summary.setText(getString(R.string.completeness_summary, result.completeRequired, result.totalRequired));
        binding.progress.setMax(Math.max(1, result.totalRequired));
        binding.progress.setProgressCompat(result.completeRequired, false);
        ProjectUi.bindStatusBadge(binding.statusBadge, header.status);

        boolean done = result.missingCount() == 0;
        binding.allDone.setVisibility(done ? View.VISIBLE : View.GONE);
        binding.missingTitle.setVisibility(done ? View.GONE : View.VISIBLE);
        fill(binding.missingList, result, true);
        binding.suggestionTitle.setVisibility(result.suggestions.isEmpty() ? View.GONE : View.VISIBLE);
        fill(binding.suggestionList, result, false);

        binding.exportDraftButton.setVisibility(done ? View.GONE : View.VISIBLE);
        binding.primaryButton.setText(done ? R.string.completeness_export : R.string.completeness_fill_now);
        binding.primaryButton.setOnClickListener(v -> {
            if (done) {
                export();
            } else {
                openField(result.missing.get(0).field.key);
            }
        });
        binding.exportDraftButton.setOnClickListener(v -> confirmDraftExport(result.missingCount()));
    }

    private void fill(LinearLayout list, CompletenessChecker.Result result, boolean missing) {
        list.removeAllViews();
        for (CompletenessChecker.Item item : missing ? result.missing : result.suggestions) {
            ItemRowBinding row = ItemRowBinding.inflate(getLayoutInflater(), list, true);
            row.icon.setVisibility(View.VISIBLE);
            row.icon.setImageResource(missing ? R.drawable.ic_radio_unchecked : R.drawable.ic_info);
            row.icon.setColorFilter(requireContext().getColor(missing ? R.color.color_warning : R.color.color_muted));
            row.title.setText(getString(R.string.completeness_item, item.field.label, reasonText(item)));
            row.getRoot().setPadding(0, row.getRoot().getPaddingTop(), 0, row.getRoot().getPaddingBottom());
            row.getRoot().setOnClickListener(v -> openField(item.field.key));
        }
    }

    private String reasonText(CompletenessChecker.Item item) {
        switch (item.reason) {
            case EMPTY:
                return getString(R.string.completeness_reason_empty);
            case INVALID_LINK:
                return getString(R.string.completeness_reason_link);
            case SAMPLE:
            default:
                return getString(R.string.completeness_reason_sample);
        }
    }

    /** Ketuk baris (bagian 7.2): kembali ke editor, buka halaman + section + isian itu. */
    private void openField(String key) {
        TemplateEditorFragment editor = (TemplateEditorFragment) requireParentFragment();
        dismiss();
        editor.openField(key);
    }

    /** Keputusan D1: export saat Draft boleh, dengan konfirmasi. */
    private void confirmDraftExport(int missing) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.completeness_draft_confirm_title)
                .setMessage(getString(R.string.completeness_draft_confirm_body, missing))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.completeness_draft_confirm, (d, w) -> export())
                .show();
    }

    private void export() {
        TemplateEditorFragment editor = (TemplateEditorFragment) requireParentFragment();
        dismiss();
        editor.startExportConfirmed();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
