package com.aris.templateapp.ui.editor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.DialogExportDoneBinding;
import com.aris.templateapp.ui.guide.Guide;
import com.aris.templateapp.ui.guide.GuideFragment;

import java.io.File;

/**
 * Layar Selesai (alur-buat-website-via-template.md bagian 9): "Website siap" + tautan Panduan "Cara mengonlinekan
 * file ZIP" (Netlify Drop & GitHub Pages, konten statis di app sejak Fase 14).
 */
public class ExportDoneDialog extends DialogFragment {

    private static final String TAG = "export-selesai";
    private static final String ARG_ZIP = "zip";
    private static final String ARG_SAVED = "saved";

    private DialogExportDoneBinding binding;

    static void show(Fragment editor, File zip, boolean saved) {
        ExportDoneDialog dialog = new ExportDoneDialog();
        Bundle args = new Bundle();
        args.putString(ARG_ZIP, zip.getAbsolutePath());
        args.putBoolean(ARG_SAVED, saved);
        dialog.setArguments(args);
        dialog.show(editor.getChildFragmentManager(), TAG);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.Theme_App_FullScreenDialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogExportDoneBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        File zip = new File(requireArguments().getString(ARG_ZIP, ""));
        boolean saved = requireArguments().getBoolean(ARG_SAVED);
        binding.body.setText(getString(saved ? R.string.export_done_saved : R.string.export_done_shared, zip.getName()));
        binding.guideButton.setOnClickListener(v -> {
            Fragment editor = requireParentFragment();
            dismiss();
            GuideFragment.open(editor, Guide.HOST_ZIP);
        });
        // ZIP masih ada di cache sampai export berikutnya, jadi bisa dibagikan lagi dari sini.
        binding.shareButton.setVisibility(zip.isFile() ? View.VISIBLE : View.GONE);
        binding.shareButton.setOnClickListener(v -> ExportDialog.share(this, zip));
        binding.backButton.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
