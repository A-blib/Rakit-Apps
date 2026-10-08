package com.aris.templateapp.ui.editor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.core.template.CompletenessChecker;
import com.aris.templateapp.core.template.CustomCss;
import com.aris.templateapp.core.template.ImageProcessor;
import com.aris.templateapp.core.template.LinkRules;
import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.ProjectValues;
import com.aris.templateapp.data.model.TemplateManifest;
import com.aris.templateapp.databinding.FragmentTemplateEditorBinding;
import com.aris.templateapp.ui.creator.ProjectUi;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Editor template mode (alur-buat-website-via-template.md bagian 6): preview WebView di tengah, form Isi/Gaya di
 * bottom sheet. Semua perubahan langsung terlihat di preview dan tersimpan otomatis; tidak ada tombol Simpan.
 */
@AndroidEntryPoint
public class TemplateEditorFragment extends Fragment implements EditorForm.Listener, EditorForm.ImageLoader {

    private static final String VIEW_MOBILE = "mobile";
    private static final String VIEW_DESKTOP = "desktop";
    private static final int THUMB_SIDE = 480;

    @Inject
    AppExecutors executors;
    @Inject
    ImageProcessor imageProcessor;

    private FragmentTemplateEditorBinding binding;
    private TemplateEditorViewModel viewModel;
    @Nullable
    private TemplateEditorViewModel.Loaded data;
    @Nullable
    private EditorPreview preview;
    @Nullable
    private EditorForm form;
    private final Gson gson = new Gson();
    private final Map<String, EditorForm.Computed> computed = new HashMap<>();

    // Pilihan tampilan; disimpan di fragment karena hanya berlaku selama layar ini terbuka.
    private String view = VIEW_MOBILE;
    private String page = "index.html";
    @Nullable
    private String sectionId;
    private EditorForm.Tab tab = EditorForm.Tab.CONTENT;
    private boolean previewMode;
    @Nullable
    private String activeKey;
    @Nullable
    private String pendingImageKey;
    // true selama keyboard terbuka (mode fokus, sama seperti langkah Coba di Upload).
    private boolean typing;
    private final ViewTreeObserver.OnGlobalLayoutListener keyboardListener = this::onLayoutChanged;

    private final ActivityResultLauncher<PickVisualMediaRequest> photoPicker = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(), this::onPhotoPicked);

    private final OnBackPressedCallback back = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (previewMode) {
                setPreviewMode(false);
            } else {
                close();
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTemplateEditorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), back);
        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(keyboardListener);
        binding.toolbar.setNavigationOnClickListener(v -> close());
        binding.toolbar.setOnMenuItemClickListener(item -> onMenu(item.getItemId()));
        binding.titleArea.setOnClickListener(v -> askRename());
        binding.pageButton.setOnClickListener(this::pickPage);
        binding.closePreview.setOnClickListener(v -> setPreviewMode(false));
        binding.viewToggle.check(R.id.view_mobile);
        binding.viewToggle.addOnButtonCheckedListener((group, id, checked) -> {
            String selected = id == R.id.view_desktop ? VIEW_DESKTOP : VIEW_MOBILE;
            if (checked && !selected.equals(view)) {
                view = selected;
                createPreview();
            }
        });
        binding.tabToggle.check(R.id.tab_content);
        binding.tabToggle.addOnButtonCheckedListener((group, id, checked) -> {
            EditorForm.Tab selected = id == R.id.tab_style ? EditorForm.Tab.STYLE : EditorForm.Tab.CONTENT;
            if (checked && selected != tab) {
                tab = selected;
                buildForm();
            }
        });
        form = new EditorForm(binding.form, this, this);

        viewModel = new ViewModelProvider(this).get(TemplateEditorViewModel.class);
        viewModel.getLoaded().observe(getViewLifecycleOwner(), this::onLoaded);
        viewModel.getHeader().observe(getViewLifecycleOwner(), this::renderHeader);
        viewModel.getChanges().observe(getViewLifecycleOwner(), event -> {
            TemplateEditorViewModel.Change change = event.getContentIfNotHandled();
            if (change != null) {
                applyChange(change);
            }
        });
        viewModel.getMessages().observe(getViewLifecycleOwner(), event -> {
            Integer message = event.getContentIfNotHandled();
            if (message != null) {
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_retry, v -> viewModel.retrySave()).show();
            }
        });
        viewModel.getClosed().observe(getViewLifecycleOwner(), event -> {
            if (event.getContentIfNotHandled() != null) {
                NavHostFragment.findNavController(this).navigateUp();
            }
        });
        Bundle args = requireArguments();
        viewModel.start(args.getString(EditorNav.ARG_PROJECT_ID), args.getString(EditorNav.ARG_TEMPLATE_ID),
                args.getInt(EditorNav.ARG_TEMPLATE_VERSION), args.getString(EditorNav.ARG_TITLE));
    }

    private void onLoaded(@Nullable Resource<TemplateEditorViewModel.Loaded> resource) {
        if (resource == null) {
            return;
        }
        if (resource.getStatus() == Resource.Status.LOADING) {
            binding.state.setVisibility(View.VISIBLE);
            binding.state.showLoading();
            return;
        }
        if (resource.getStatus() == Resource.Status.ERROR) {
            ApiError error = resource.getError();
            boolean missingProject = error != null && EditorErrors.PROJECT_NOT_FOUND.equals(error.getCode());
            binding.state.setVisibility(View.VISIBLE);
            binding.state.showEmpty(getString(missingProject ? R.string.editor_project_missing
                    : R.string.editor_package_missing), getString(R.string.cd_back), this::close);
            binding.sheet.setVisibility(View.GONE);
            return;
        }
        if (data != null) {
            return;
        }
        data = resource.getData();
        TemplateManifest manifest = data.manifest;
        if (!manifest.pages.isEmpty()) {
            page = manifest.pages.get(0).file;
        }
        binding.pageButton.setVisibility(manifest.pages.size() > 1 ? View.VISIBLE : View.INVISIBLE);
        buildSectionChips();
        buildForm();
        createPreview();
    }

    // ---------- app bar ----------

    private void renderHeader(@Nullable TemplateEditorViewModel.Header header) {
        if (header == null || binding == null) {
            return;
        }
        binding.projectName.setText(header.name);
        ProjectUi.bindStatusBadge(binding.statusBadge, header.status);
        switch (header.save) {
            case SAVING:
                binding.saveState.setText(R.string.editor_saving);
                break;
            case FAILED:
                binding.saveState.setText(R.string.editor_save_error);
                break;
            case SAVED:
                binding.saveState.setText(R.string.editor_saved);
                break;
            case IDLE:
            default:
                binding.saveState.setText(R.string.editor_unsaved);
                break;
        }
        binding.exportNote.setVisibility(header.changedSinceExport && !previewMode ? View.VISIBLE : View.GONE);
        Menu menu = binding.toolbar.getMenu();
        menu.findItem(R.id.action_undo).setEnabled(header.canUndo).getIcon().setAlpha(header.canUndo ? 255 : 80);
        menu.findItem(R.id.action_redo).setEnabled(header.canRedo).getIcon().setAlpha(header.canRedo ? 255 : 80);
        menu.findItem(R.id.action_delete).setVisible(viewModel.projectId() != null);
        updateSectionChips(header.completeness);
        if (form != null && data != null) {
            form.updateWarnings(data.manifest, header.completeness);
        }
    }

    private boolean onMenu(int id) {
        if (id == R.id.action_undo) {
            viewModel.undo();
            buildForm();
        } else if (id == R.id.action_redo) {
            viewModel.redo();
            buildForm();
        } else if (id == R.id.action_preview) {
            setPreviewMode(true);
        } else if (id == R.id.action_completeness) {
            openCompleteness();
        } else if (id == R.id.action_export) {
            startExport();
        } else if (id == R.id.action_reset) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.editor_reset_title)
                    .setMessage(R.string.editor_reset_body)
                    .setNegativeButton(R.string.action_cancel, null)
                    .setPositiveButton(R.string.editor_reset_confirm, (d, w) -> {
                        viewModel.resetToTemplate();
                        buildForm();
                    })
                    .show();
        } else if (id == R.id.action_delete) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.editor_delete_title)
                    .setMessage(R.string.editor_delete_body)
                    .setNegativeButton(R.string.action_cancel, null)
                    .setPositiveButton(R.string.editor_delete_confirm, (d, w) -> viewModel.deleteProject())
                    .show();
        } else {
            return false;
        }
        return true;
    }

    private void askRename() {
        TextInputLayout layout = new TextInputLayout(requireContext(), null,
                com.google.android.material.R.attr.textInputOutlinedStyle);
        layout.setHint(getString(R.string.editor_rename_hint));
        TextInputEditText input = new TextInputEditText(layout.getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setMaxLines(1);
        input.setText(viewModel.projectName());
        layout.addView(input);
        FrameLayout frame = new FrameLayout(requireContext());
        int padding = getResources().getDimensionPixelSize(R.dimen.screen_padding_horizontal);
        frame.setPadding(padding, getResources().getDimensionPixelSize(R.dimen.space_2), padding, 0);
        frame.addView(layout);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.editor_rename)
                .setView(frame)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.editor_use, (d, w) -> viewModel.rename(String.valueOf(input.getText())))
                .show();
    }

    // ---------- chip section & form ----------

    private void buildSectionChips() {
        if (data == null) {
            return;
        }
        binding.sectionChips.removeAllViews();
        addSectionChip(null, getString(R.string.editor_all_sections));
        for (TemplateManifest.Section section : data.manifest.sections) {
            if (!EditorForm.fieldsOf(data.manifest, section.id).isEmpty()) {
                addSectionChip(section.id, section.name);
            }
        }
        CompletenessChecker.Result completeness = viewModel.completeness();
        if (completeness != null) {
            updateSectionChips(completeness);
        }
    }

    private void addSectionChip(@Nullable String id, String name) {
        Chip chip = new Chip(requireContext());
        chip.setCheckable(true);
        chip.setTag(id == null ? "" : id);
        chip.setText(name);
        chip.setChecked(id == null ? sectionId == null : id.equals(sectionId));
        chip.setOnClickListener(v -> {
            selectSection(id);
            scrollPreviewToSection(id);
        });
        binding.sectionChips.addView(chip);
    }

    /** Chip berisi tanda ✓ (lengkap) atau ·N (N isian belum lengkap), bagian 6.4. */
    private void updateSectionChips(CompletenessChecker.Result completeness) {
        if (data == null) {
            return;
        }
        for (int i = 0; i < binding.sectionChips.getChildCount(); i++) {
            Chip chip = (Chip) binding.sectionChips.getChildAt(i);
            String id = (String) chip.getTag();
            if (id.isEmpty()) {
                continue;
            }
            String name = sectionName(id);
            Integer missing = completeness.missingBySection.get(id);
            chip.setText(missing == null || missing == 0 ? getString(R.string.editor_chip_done, name)
                    : getString(R.string.editor_chip_missing, name, missing));
        }
    }

    private String sectionName(String id) {
        if (data != null) {
            for (TemplateManifest.Section section : data.manifest.sections) {
                if (section.id.equals(id)) {
                    return section.name;
                }
            }
        }
        return id;
    }

    private void selectSection(@Nullable String id) {
        sectionId = id;
        for (int i = 0; i < binding.sectionChips.getChildCount(); i++) {
            Chip chip = (Chip) binding.sectionChips.getChildAt(i);
            chip.setChecked(chip.getTag().equals(id == null ? "" : id));
        }
        buildForm();
    }

    private void buildForm() {
        if (form == null || data == null) {
            return;
        }
        CompletenessChecker.Result completeness = viewModel.completeness();
        if (completeness == null) {
            completeness = CompletenessChecker.check(data.manifest, viewModel.values());
        }
        form.build(data.manifest, viewModel.values(), completeness, tab, sectionId, computed);
    }

    // ---------- EditorForm.Listener ----------

    @Override
    public void onText(String key, String text) {
        viewModel.setText(key, text);
    }

    @Override
    public void onHref(String key, @Nullable String href) {
        viewModel.setHref(key, href);
    }

    @Override
    public void onPickImage(String key) {
        pendingImageKey = key;
        photoPicker.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    @Override
    public void onResetImage(String key) {
        viewModel.setImage(key, null);
        buildForm();
    }

    @Override
    public void onStyle(String key, String prop, @Nullable String value) {
        viewModel.setStyle(key, prop, value);
    }

    @Override
    public void onTheme(String variable, @Nullable String value) {
        viewModel.setTheme(variable, value);
    }

    /** Isian diketuk/diketik: elemennya disorot di preview (pindah halaman jika elemennya tidak ada di sini). */
    @Override
    public void onFieldFocused(String key) {
        activeKey = key;
        if (typing && binding != null) {
            binding.formScroll.post(this::scrollFocusedIntoView);
        }
        TemplateManifest.Field field = data == null ? null : data.manifest.field(key);
        if (field == null || preview == null) {
            return;
        }
        if (!field.pages.isEmpty() && !field.pages.contains(page)) {
            loadPage(field.pages.get(0));
            return;
        }
        highlight(key, true);
    }

    // ---------- foto (bagian 6.6) ----------

    private void onPhotoPicked(@Nullable Uri uri) {
        String key = pendingImageKey;
        pendingImageKey = null;
        TemplateManifest.Field field = data == null || key == null ? null : data.manifest.field(key);
        if (uri == null || field == null) {
            return;
        }
        if (form != null) {
            form.setImageLoading(key, true);
        }
        File projectDir = viewModel.projectDir();
        ProjectValues.FieldValue old = viewModel.values().peek(key);
        String oldImage = old == null ? null : old.image;
        executors.diskIO().execute(() -> {
            String saved = null;
            try {
                saved = imageProcessor.process(uri, field.aspectRatio, projectDir, key);
            } catch (IOException ignored) {
                // ditangani di bawah: saved tetap null
            }
            String result = saved;
            executors.mainThread().execute(() -> {
                if (binding == null) {
                    return;
                }
                if (form != null) {
                    form.setImageLoading(key, false);
                }
                if (result == null) {
                    Snackbar.make(binding.getRoot(), R.string.editor_image_failed, Snackbar.LENGTH_LONG).show();
                    return;
                }
                viewModel.setImage(key, result);
                imageProcessor.deleteIfUnused(projectDir, oldImage, viewModel.imagesInUse());
                buildForm();
            });
        });
    }

    @Override
    public void load(TemplateManifest.Field field, @Nullable ProjectValues.FieldValue value, ImageView target) {
        if (data == null) {
            return;
        }
        File file = value != null && value.image != null ? new File(viewModel.projectDir(), value.image)
                : field.sample != null && !field.sample.contains("://") ? new File(data.packageDir, field.sample) : null;
        target.setTag(file);
        if (file == null) {
            target.setImageDrawable(null);
            return;
        }
        executors.diskIO().execute(() -> {
            Bitmap bitmap = decodeSmall(file);
            executors.mainThread().execute(() -> {
                if (bitmap != null && file.equals(target.getTag())) {
                    target.setImageBitmap(bitmap);
                }
            });
        });
    }

    @Nullable
    private static Bitmap decodeSmall(File file) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), bounds);
        int sample = 1;
        while (bounds.outWidth / (sample * 2) >= THUMB_SIDE && bounds.outHeight / (sample * 2) >= THUMB_SIDE / 2) {
            sample *= 2;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        return BitmapFactory.decodeFile(file.getPath(), options);
    }

    // ---------- preview ----------

    private void createPreview() {
        if (data == null || binding == null) {
            return;
        }
        if (preview != null) {
            binding.webFrame.removeView(preview.view());
            preview.destroy();
            preview = null;
        }
        Set<String> keys = new HashSet<>();
        for (TemplateManifest.Field field : data.manifest.fields) {
            keys.add(field.key);
        }
        try {
            preview = new EditorPreview(requireContext(), data.packageDir, viewModel.projectDir(),
                    VIEW_DESKTOP.equals(view), !previewMode, keys, new EditorPreview.Listener() {
                        @Override
                        public void onPageLoaded(String loaded) {
                            onPreviewLoaded(loaded);
                        }

                        @Override
                        public void onFieldTapped(String key) {
                            openField(key);
                        }
                    });
        } catch (IOException e) {
            binding.state.setVisibility(View.VISIBLE);
            binding.state.showError(getString(R.string.error_unknown), this::createPreview);
            return;
        }
        binding.webFrame.addView(preview.view(), 0, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        binding.state.setVisibility(View.VISIBLE);
        binding.state.showLoading();
        preview.load(page);
        updatePageButton();
    }

    private void onPreviewLoaded(String loaded) {
        if (binding == null || preview == null || data == null) {
            return;
        }
        if (data.manifest.pageName(loaded) != null && !loaded.equals(page)) {
            // Di preview layar penuh, link antar-halaman berfungsi: pemilih halaman ikut berpindah.
            page = loaded;
            updatePageButton();
        }
        binding.state.hide();
        binding.state.setVisibility(View.GONE);
        applyAll();
        if (activeKey != null && !previewMode) {
            highlight(activeKey, true);
        }
        loadComputed();
    }

    private void loadPage(String file) {
        page = file;
        if (preview != null) {
            binding.state.setVisibility(View.VISIBLE);
            binding.state.showLoading();
            preview.load(file);
        }
        updatePageButton();
    }

    /** Semua nilai + CSS ke halaman yang sedang tampil (setelah halaman dimuat, undo/redo, reset). */
    private void applyAll() {
        if (preview == null || data == null) {
            return;
        }
        Map<String, Object> fields = new LinkedHashMap<>();
        for (TemplateManifest.Field field : data.manifest.fields) {
            fields.put(field.key, fieldPayload(field));
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("fields", fields);
        payload.put("css", CustomCss.build(data.manifest, viewModel.values(), CustomCss.Target.PREVIEW));
        preview.run("window.RakitEditor && RakitEditor.apply(" + gson.toJson(payload) + ")", null);
    }

    private Map<String, Object> fieldPayload(TemplateManifest.Field field) {
        Map<String, Object> value = new HashMap<>();
        ProjectValues.FieldValue chosen = viewModel.values().peek(field.key);
        if (chosen != null) {
            if (chosen.text != null && field.hasText()) {
                value.put("text", chosen.text);
            }
            // Link yang belum valid tidak dipasang: preview tetap memakai link template (dan javascript: tidak pernah).
            if (chosen.href != null && field.hasHref() && LinkRules.isValid(chosen.href)) {
                value.put("href", chosen.href.trim());
            }
            if (chosen.image != null && TemplateManifest.TYPE_IMAGE.equals(field.type)) {
                value.put("image", EditorPreview.imageUrl(chosen.image));
            }
        }
        Map<String, Object> item = new HashMap<>();
        item.put("type", field.type);
        item.put("value", value);
        return item;
    }

    private void applyChange(TemplateEditorViewModel.Change change) {
        if (preview == null || data == null) {
            return;
        }
        if (change.key == null) {
            applyAll();
            return;
        }
        TemplateManifest.Field field = data.manifest.field(change.key);
        if (field == null) {
            return;
        }
        Map<String, Object> item = fieldPayload(field);
        preview.run("window.RakitEditor && RakitEditor.setField(" + gson.toJson(field.key) + ","
                + gson.toJson(field.type) + "," + gson.toJson(item.get("value")) + ")", null);
        if (change.styles) {
            preview.run("window.RakitEditor && RakitEditor.css("
                    + gson.toJson(CustomCss.build(data.manifest, viewModel.values(), CustomCss.Target.PREVIEW)) + ")", null);
        }
    }

    private void highlight(@Nullable String key, boolean scroll) {
        if (preview != null) {
            preview.run("window.RakitEditor && RakitEditor.highlight(" + gson.toJson(key) + "," + scroll + ")", null);
        }
    }

    /** Ukuran huruf, sudut, dan warna yang sedang berlaku, untuk posisi awal slider dan cek kontras di tab Gaya. */
    private void loadComputed() {
        if (preview == null || data == null) {
            return;
        }
        List<String> keys = new ArrayList<>();
        for (TemplateManifest.Field field : data.manifest.fields) {
            if (!field.styles.isEmpty() && !computed.containsKey(field.key) && field.pages.contains(page)) {
                keys.add(field.key);
            }
        }
        if (keys.isEmpty()) {
            return;
        }
        preview.run("window.RakitEditor ? RakitEditor.computed(" + gson.toJson(keys) + ") : null", value -> {
            Map<String, EditorForm.Computed> found = parseComputed(value);
            computed.putAll(found);
            if (tab == EditorForm.Tab.STYLE && !found.isEmpty()) {
                buildForm();
            }
        });
    }

    private Map<String, EditorForm.Computed> parseComputed(@Nullable String evaluated) {
        try {
            if (evaluated == null || evaluated.equals("null")) {
                return Collections.emptyMap();
            }
            // evaluateJavascript membungkus string JSON dengan tanda kutip, jadi di-parse dua kali.
            String json = JsonParser.parseString(evaluated).getAsString();
            Map<String, EditorForm.Computed> map = gson.fromJson(json,
                    new TypeToken<Map<String, EditorForm.Computed>>() { }.getType());
            return map == null ? Collections.emptyMap() : map;
        } catch (RuntimeException e) {
            return Collections.emptyMap();
        }
    }

    /** Elemen di preview diketuk / baris Kelengkapan dipilih: buka isiannya di form (bagian 6.4, 7.2). */
    void openField(String key) {
        if (data == null || form == null) {
            return;
        }
        TemplateManifest.Field field = data.manifest.field(key);
        if (field == null) {
            return;
        }
        activeKey = key;
        highlight(key, false);
        boolean rebuild = tab != EditorForm.Tab.CONTENT || (sectionId != null && !sectionId.equals(field.sectionId));
        if (tab != EditorForm.Tab.CONTENT) {
            tab = EditorForm.Tab.CONTENT;
            binding.tabToggle.check(R.id.tab_content);
        }
        if (rebuild) {
            selectSection(sectionId == null ? null : field.sectionId);
        }
        BottomSheetBehavior<View> sheet = BottomSheetBehavior.from(binding.sheet);
        if (sheet.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            sheet.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        }
        focusWhenBuilt(key, 0);
    }

    /** Form dibangun bertahap; tunggu sampai baris isian ada, lalu gulir & fokus. */
    private void focusWhenBuilt(String key, int attempt) {
        if (binding == null || form == null) {
            return;
        }
        if (!form.hasRow(key)) {
            if (attempt < 30) {
                binding.form.postDelayed(() -> focusWhenBuilt(key, attempt + 1), 32);
            }
            return;
        }
        View row = form.focusField(key);
        if (row != null) {
            binding.formScroll.post(() -> binding.formScroll.smoothScrollTo(0, row.getTop()));
        }
    }

    /** Chip section diketuk: preview digulir ke isian pertama section itu (pindah halaman jika perlu). */
    private void scrollPreviewToSection(@Nullable String id) {
        if (id == null || data == null) {
            return;
        }
        List<TemplateManifest.Field> fields = EditorForm.fieldsOf(data.manifest, id);
        if (fields.isEmpty()) {
            return;
        }
        TemplateManifest.Field first = fields.get(0);
        activeKey = first.key;
        if (!first.pages.isEmpty() && !first.pages.contains(page)) {
            loadPage(first.pages.get(0));
        } else {
            highlight(first.key, true);
        }
    }

    private void pickPage(View anchor) {
        if (data == null) {
            return;
        }
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        List<TemplateManifest.Page> pages = data.manifest.pages;
        for (int i = 0; i < pages.size(); i++) {
            menu.getMenu().add(0, i, i, data.manifest.pageName(pages.get(i).file));
        }
        menu.setOnMenuItemClickListener(item -> {
            activeKey = null;
            loadPage(pages.get(item.getItemId()).file);
            return true;
        });
        menu.show();
    }

    private void updatePageButton() {
        if (binding != null && data != null) {
            binding.pageButton.setText(data.manifest.pageName(page));
        }
    }

    /** Preview layar penuh (bagian 6.2 👁): tanpa form, link antar-halaman berfungsi. */
    private void setPreviewMode(boolean on) {
        if (previewMode == on || binding == null) {
            return;
        }
        previewMode = on;
        viewModel.flush();
        int editing = on ? View.GONE : View.VISIBLE;
        binding.toolbar.setVisibility(editing);
        binding.sheet.setVisibility(editing);
        binding.exportNote.setVisibility(View.GONE);
        binding.closePreview.setVisibility(on ? View.VISIBLE : View.GONE);
        binding.content.setPadding(0, 0, 0, on ? 0 : getResources().getDimensionPixelSize(R.dimen.editor_sheet_peek));
        createPreview();
    }

    // ---------- mode fokus keyboard ----------

    /**
     * Saat keyboard terbuka, form dinaikkan dan baris yang tidak dipakai saat mengetik disembunyikan, agar kolom
     * tidak tertutup keyboard (pelajaran dari langkah Coba Upload).
     */
    private void onLayoutChanged() {
        if (binding == null || previewMode) {
            return;
        }
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(binding.getRoot());
        boolean keyboard = insets != null && insets.isVisible(WindowInsetsCompat.Type.ime());
        if (keyboard == typing) {
            return;
        }
        typing = keyboard;
        int hidden = keyboard ? View.GONE : View.VISIBLE;
        for (View v : new View[] {binding.controlsRow, binding.dragHandle, binding.sectionScroll, binding.tabToggle}) {
            v.setVisibility(hidden);
        }
        binding.content.setPadding(0, 0, 0, keyboard ? 0 : getResources().getDimensionPixelSize(R.dimen.editor_sheet_peek));
        BottomSheetBehavior<View> sheet = BottomSheetBehavior.from(binding.sheet);
        if (keyboard) {
            sheet.setExpandedOffset(binding.toolbar.getBottom()
                    + getResources().getDimensionPixelSize(R.dimen.editor_sheet_typing_preview));
            sheet.setState(BottomSheetBehavior.STATE_EXPANDED);
            binding.formScroll.post(this::scrollFocusedIntoView);
        } else {
            sheet.setExpandedOffset(getResources().getDimensionPixelSize(R.dimen.editor_sheet_expanded_offset));
        }
    }

    private void scrollFocusedIntoView() {
        if (binding == null) {
            return;
        }
        View focused = binding.formScroll.findFocus();
        View row = focused;
        while (row != null && row.getParent() != binding.form) {
            row = row.getParent() instanceof View ? (View) row.getParent() : null;
        }
        if (focused == null || row == null) {
            return;
        }
        Rect rect = new Rect();
        focused.getDrawingRect(rect);
        binding.formScroll.offsetDescendantRectToMyCoords(focused, rect);
        Rect shown = new Rect();
        int visible = binding.formScroll.getGlobalVisibleRect(shown) ? shown.height() : binding.formScroll.getHeight();
        int target = rect.bottom - row.getTop() <= visible ? row.getTop()
                : rect.top - getResources().getDimensionPixelSize(R.dimen.space_8);
        binding.formScroll.smoothScrollTo(0, Math.max(0, target));
    }

    // ---------- kelengkapan & export ----------

    private void openCompleteness() {
        viewModel.flush();
        CompletenessDialog.show(this);
    }

    /** Export (bagian 7.2): saat masih Draft, layar Kelengkapan muncul dulu. */
    private void startExport() {
        CompletenessChecker.Result result = viewModel.completeness();
        if (result != null && result.missingCount() > 0) {
            openCompleteness();
        } else {
            startExportConfirmed();
        }
    }

    void startExportConfirmed() {
        viewModel.flush();
        ExportDialog.show(this);
    }

    // ---------- keluar ----------

    private void close() {
        captureThumbnail();
        viewModel.flush();
        NavHostFragment.findNavController(this).navigateUp();
    }

    /**
     * Thumbnail project (bagian 6.7) diambil dari preview yang sedang tampil, hanya jika itu halaman utama. Halaman
     * lain dilewati agar thumbnail selalu menunjukkan beranda website.
     */
    private void captureThumbnail() {
        if (preview == null || data == null || viewModel.projectId() == null || data.manifest.pages.isEmpty()
                || !data.manifest.pages.get(0).file.equals(page)) {
            return;
        }
        WebView web = preview.view();
        int width = web.getWidth();
        int height = Math.min(web.getHeight(), Math.round(width * 0.75f));
        if (width <= 0 || height <= 0) {
            return;
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(-web.getScrollX(), -web.getScrollY());
        web.draw(canvas);
        viewModel.saveThumbnail(bitmap);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(keyboardListener);
        if (preview != null) {
            preview.destroy();
            preview = null;
        }
        data = null;
        form = null;
        binding = null;
    }
}
