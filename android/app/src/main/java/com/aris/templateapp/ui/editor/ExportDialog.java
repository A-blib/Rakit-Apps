package com.aris.templateapp.ui.editor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aris.templateapp.R;
import com.aris.templateapp.core.template.ExportNames;
import com.aris.templateapp.core.template.ProjectStore;
import com.aris.templateapp.core.upload.FileSizes;
import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.data.model.TemplateManifest;
import com.aris.templateapp.databinding.DialogExportBinding;
import com.aris.templateapp.ui.creator.ProjectUi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Layar Export (alur-buat-website-via-template.md bagian 8). ZIP dibuat dulu di cache app, lalu:
 * <ul>
 *   <li><b>Simpan ke HP</b>: pemilih lokasi Android ({@code ACTION_CREATE_DOCUMENT}), isi ZIP disalin ke sana;</li>
 *   <li><b>Bagikan</b>: lewat {@link FileProvider} + {@code ACTION_SEND}.</li>
 * </ul>
 */
@AndroidEntryPoint
public class ExportDialog extends DialogFragment {

    private static final String TAG = "export";
    static final String ZIP_MIME = "application/zip";

    @Inject
    AppExecutors executors;
    @Inject
    ProjectStore store;

    private DialogExportBinding binding;
    private TemplateEditorViewModel viewModel;
    /** Tujuan setelah ZIP jadi: true = simpan, false = bagikan. */
    private boolean saveAfterBuild;
    private boolean waitingForBuild;

    private final ActivityResultLauncher<String> createDocument = registerForActivityResult(
            new ActivityResultContracts.CreateDocument(ZIP_MIME), this::onLocationPicked);

    static void show(Fragment editor) {
        if (editor.getChildFragmentManager().findFragmentByTag(TAG) == null) {
            new ExportDialog().show(editor.getChildFragmentManager(), TAG);
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
        binding = DialogExportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireParentFragment()).get(TemplateEditorViewModel.class);
        viewModel.resetExport();
        binding.toolbar.setNavigationOnClickListener(v -> dismiss());
        TemplateEditorViewModel.Loaded data = viewModel.data();
        if (data == null) {
            dismiss();
            return;
        }
        binding.projectName.setText(viewModel.projectName());
        binding.pageCount.setText(getString(R.string.export_pages, data.manifest.pages.size()));
        binding.fileName.setText(ExportNames.fileName(viewModel.projectName()));
        binding.contents.setText(contents(data));
        loadThumbnail();
        viewModel.getHeader().observe(getViewLifecycleOwner(), header -> {
            if (header != null) {
                ProjectUi.bindStatusBadge(binding.statusBadge, header.status);
            }
        });
        viewModel.getExport().observe(getViewLifecycleOwner(), this::renderExport);
        binding.saveButton.setOnClickListener(v -> build(true));
        binding.shareButton.setOnClickListener(v -> build(false));
    }

    /** "index.html, tentang.html …", folder utama, lalu custom.css dan perkiraan ukuran. */
    private String contents(TemplateEditorViewModel.Loaded data) {
        List<String> pages = new ArrayList<>();
        for (TemplateManifest.Page page : data.manifest.pages) {
            pages.add(page.file);
        }
        Set<String> folders = new TreeSet<>();
        long size = sizeOf(data.packageDir, "", folders);
        size += sizeOf(new File(viewModel.projectDir(), ProjectStore.IMAGES), null, null);
        folders.add("img");
        StringBuilder text = new StringBuilder(String.join(", ", pages));
        List<String> dirs = new ArrayList<>();
        for (String folder : folders) {
            dirs.add(folder + "/");
        }
        text.append('\n').append(String.join(" · ", dirs));
        text.append('\n').append("custom.css · ").append(getString(R.string.export_size_estimate, FileSizes.format(size)));
        return text.toString();
    }

    private static long sizeOf(File dir, @Nullable String prefix, @Nullable Set<String> topFolders) {
        long total = 0;
        File[] children = dir.listFiles();
        if (children == null) {
            return 0;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                if (topFolders != null && "".equals(prefix)) {
                    topFolders.add(child.getName());
                }
                total += sizeOf(child, prefix == null ? null : prefix + child.getName() + "/", null);
            } else {
                total += child.length();
            }
        }
        return total;
    }

    private void loadThumbnail() {
        String id = viewModel.projectId();
        if (id == null) {
            return;
        }
        File file = store.thumbnail(id);
        executors.diskIO().execute(() -> {
            Bitmap bitmap = file.isFile() ? BitmapFactory.decodeFile(file.getPath()) : null;
            executors.mainThread().execute(() -> {
                if (binding != null && bitmap != null) {
                    binding.thumbnail.setImageBitmap(bitmap);
                }
            });
        });
    }

    private void build(boolean save) {
        String fileName = ExportNames.clean(String.valueOf(binding.fileName.getText()));
        binding.fileName.setText(fileName);
        saveAfterBuild = save;
        waitingForBuild = true;
        viewModel.buildExport(fileName, binding.compress.isChecked());
    }

    private void renderExport(@Nullable TemplateEditorViewModel.ExportState state) {
        if (state == null) {
            return;
        }
        boolean building = state.phase == TemplateEditorViewModel.ExportPhase.BUILDING;
        binding.saveButton.setEnabled(!building);
        binding.shareButton.setEnabled(!building);
        binding.progressBox.setVisibility(state.phase == TemplateEditorViewModel.ExportPhase.IDLE ? View.GONE : View.VISIBLE);
        binding.progress.setVisibility(building ? View.VISIBLE : View.GONE);
        binding.progressText.setTextColor(requireContext().getColor(
                state.phase == TemplateEditorViewModel.ExportPhase.FAILED ? R.color.color_error : R.color.color_muted));
        switch (state.phase) {
            case BUILDING:
                binding.progress.setMax(Math.max(1, state.total));
                binding.progress.setProgressCompat(state.done, true);
                binding.progressText.setText(getString(R.string.export_building, state.done, state.total));
                break;
            case FAILED:
                binding.progressText.setText(R.string.export_failed);
                break;
            case READY:
                binding.progressText.setText(state.zip == null ? null
                        : String.format(Locale.ROOT, "%s · %s", state.zip.getName(), FileSizes.format(state.zip.length())));
                if (waitingForBuild && state.zip != null) {
                    waitingForBuild = false;
                    deliver(state.zip);
                }
                break;
            case IDLE:
            default:
                break;
        }
    }

    private void deliver(File zip) {
        if (saveAfterBuild) {
            createDocument.launch(zip.getName());
        } else {
            share(this, zip);
            viewModel.onExportDelivered();
            openDone(zip, false);
        }
    }

    /** Lokasi dipilih: ZIP di cache disalin ke sana (di thread latar). Batal memilih = tidak terjadi apa-apa. */
    private void onLocationPicked(@Nullable Uri uri) {
        TemplateEditorViewModel.ExportState state = viewModel.getExport().getValue();
        File zip = state == null ? null : state.zip;
        if (uri == null || zip == null) {
            return;
        }
        android.content.ContentResolver resolver = requireContext().getContentResolver();
        executors.diskIO().execute(() -> {
            boolean ok;
            try (InputStream in = new FileInputStream(zip); OutputStream out = resolver.openOutputStream(uri)) {
                if (out == null) {
                    throw new IOException("Lokasi tidak bisa ditulis");
                }
                byte[] buffer = new byte[64 * 1024];
                int n;
                while ((n = in.read(buffer)) > 0) {
                    out.write(buffer, 0, n);
                }
                ok = true;
            } catch (IOException e) {
                ok = false;
            }
            boolean saved = ok;
            executors.mainThread().execute(() -> {
                if (binding == null) {
                    return;
                }
                if (saved) {
                    viewModel.onExportDelivered();
                    openDone(zip, true);
                } else {
                    binding.progressText.setText(R.string.export_save_failed);
                    binding.progressText.setTextColor(requireContext().getColor(R.color.color_error));
                }
            });
        });
    }

    static void share(Fragment from, File zip) {
        Uri uri = FileProvider.getUriForFile(from.requireContext(), from.requireContext().getPackageName() + ".files", zip);
        Intent send = new Intent(Intent.ACTION_SEND)
                .setType(ZIP_MIME)
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        from.startActivity(Intent.createChooser(send, from.getString(R.string.export_share_chooser)));
    }

    private void openDone(File zip, boolean saved) {
        Fragment editor = requireParentFragment();
        dismiss();
        ExportDoneDialog.show(editor, zip, saved);
    }

    @Override
    public void onStart() {
        super.onStart();
        FullScreenDialogs.apply(this, binding.getRoot());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
