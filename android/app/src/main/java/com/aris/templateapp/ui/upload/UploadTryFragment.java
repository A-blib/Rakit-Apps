package com.aris.templateapp.ui.upload;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.PopupMenu;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

import com.aris.templateapp.R;
import com.aris.templateapp.core.upload.ThumbnailImages;
import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.remote.dto.MarkingDto;
import com.aris.templateapp.databinding.FragmentUploadTryBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Langkah 5 Upload: Coba sebagai pengguna (alur-fitur-upload.md bagian 8). Preview di atas berubah langsung saat form
 * di bawah diisi. Percobaan tidak dikirim ke server dan tidak mengubah isi contoh template.
 */
@AndroidEntryPoint
public class UploadTryFragment extends Fragment implements TryForm.Listener {

    private static final int IMAGE_MAX_SIDE = 1200;

    /** Teks dan alamat link asli satu elemen (dari {@code RakitTry.original}). */
    static class Original {
        String text;
        String href;
    }

    @Inject
    TrySession session;
    @Inject
    ThumbnailImages images;
    @Inject
    AppExecutors executors;

    private FragmentUploadTryBinding binding;
    private TryViewModel viewModel;
    @Nullable
    private TryViewModel.Ready ready;
    @Nullable
    private WebView webView;
    @Nullable
    private TryForm form;
    private final Map<String, String> originals = new HashMap<>();
    private final Map<String, String> originalHrefs = new HashMap<>();
    private final Gson gson = new Gson();
    @Nullable
    private String pendingImageKey;

    private final ActivityResultLauncher<String> imagePicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                String key = pendingImageKey;
                if (uri == null || key == null) {
                    return;
                }
                executors.diskIO().execute(() -> {
                    String dataUrl = images.dataUrl(uri, IMAGE_MAX_SIDE);
                    executors.mainThread().execute(() -> {
                        if (dataUrl != null && binding != null) {
                            session.value(viewModel.templateId(), key).image = dataUrl;
                            applyPreview();
                        }
                    });
                });
            });

    private final OnBackPressedCallback back = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            close();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUploadTryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        WizardHeader.bind(binding.header, 5, R.string.upload_step_try, this::close);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), back);
        viewModel = new ViewModelProvider(this).get(TryViewModel.class);
        viewModel.getReady().observe(getViewLifecycleOwner(), this::onReady);

        binding.viewToggle.check("desktop".equals(viewModel.view()) ? R.id.view_desktop : R.id.view_mobile);
        binding.viewToggle.addOnButtonCheckedListener((group, id, checked) -> {
            String selected = id == R.id.view_desktop ? "desktop" : "mobile";
            if (checked && !selected.equals(viewModel.view())) {
                viewModel.setView(selected);
                createWebView();
            }
        });
        binding.pageButton.setOnClickListener(this::pickPage);
        binding.resetButton.setOnClickListener(v -> {
            session.reset(viewModel.templateId());
            buildForm();
            createWebView();
        });
        binding.longButton.setOnClickListener(v -> fillLongContent());
        binding.backMarkButton.setOnClickListener(v ->
                UploadNav.replaceStep(this, R.id.uploadMarkFragment, viewModel.templateId()));
        binding.nextButton.setOnClickListener(v ->
                UploadNav.replaceStep(this, R.id.uploadSendFragment, viewModel.templateId()));
        viewModel.start(requireArguments().getString(UploadNav.ARG_TEMPLATE_ID, ""));
    }

    private void onReady(@Nullable Resource<TryViewModel.Ready> resource) {
        if (resource == null) {
            return;
        }
        if (resource.getStatus() != Resource.Status.SUCCESS) {
            binding.state.setVisibility(View.VISIBLE);
            if (resource.getStatus() == Resource.Status.LOADING) {
                binding.state.showLoading();
            } else {
                binding.state.showError(ErrorMessages.forError(requireContext(), resource.getError()), viewModel::load);
            }
            return;
        }
        if (ready != null) {
            return;
        }
        ready = resource.getData();
        MarkingDto marking = ready.marking;
        int fields = marking.fields == null ? 0 : marking.fields.size();
        boolean enough = fields >= MarkingEditor.MIN_FIELDS_TO_SEND;
        // "Lanjut: Kirim" aktif jika minimal 3 isian tersimpan (bagian 7.12).
        binding.nextButton.setEnabled(enough);
        binding.needFields.setVisibility(enough ? View.GONE : View.VISIBLE);
        binding.needFields.setText(getString(R.string.try_need_fields, MarkingEditor.MIN_FIELDS_TO_SEND, fields));
        binding.pageButton.setVisibility(marking.pages.size() > 1 ? View.VISIBLE : View.INVISIBLE);
        form = new TryForm(binding.form, session, viewModel.templateId(), this);
        buildSectionChips(marking);
        buildForm();
        createWebView();
    }

    // ---------- preview ----------

    @SuppressLint("SetJavaScriptEnabled")
    private void createWebView() {
        if (ready == null) {
            return;
        }
        if (webView != null) {
            binding.webFrame.removeView(webView);
            webView.destroy();
        }
        WebView view = new WebView(requireContext());
        boolean desktop = "desktop".equals(viewModel.view());
        try {
            // Di mode Coba link antar-halaman berfungsi normal (bagian 8).
            SiteWebView.configure(view, ready.siteRoot, ready.allowedHosts, desktop, true, page -> {
                viewModel.setPage(page);
                updatePageButton();
            });
            String tryScript = MarkWebView.readAsset(requireContext(), "upload/try.js");
            if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
                Set<String> origin = Collections.singleton("https://" + SiteWebView.HOST);
                if (desktop) {
                    WebViewCompat.addDocumentStartJavaScript(view, MarkWebView.readAsset(requireContext(),
                            "upload/desktop.js"), origin);
                }
                WebViewCompat.addDocumentStartJavaScript(view, tryScript, origin);
            }
            view.getSettings().setSupportZoom(true);
            view.getSettings().setBuiltInZoomControls(true);
            view.getSettings().setDisplayZoomControls(false);
            view.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView v, int progress) {
                    if (progress == 100 && binding != null) {
                        if (!WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
                            v.evaluateJavascript(tryScript, null);
                        }
                        binding.state.setVisibility(View.GONE);
                        loadOriginals();
                        applyPreview();
                    }
                }
            });
        } catch (IOException e) {
            binding.state.showError(getString(R.string.error_unknown), this::createWebView);
            return;
        }
        binding.webFrame.addView(view, 0, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        webView = view;
        binding.state.setVisibility(View.VISIBLE);
        binding.state.showLoading();
        view.loadUrl(SiteWebView.urlOf(viewModel.page()));
        updatePageButton();
    }

    /** Menerapkan semua isian percobaan ke halaman yang sedang tampil. */
    private void applyPreview() {
        if (webView == null || ready == null) {
            return;
        }
        String page = viewModel.page();
        List<Map<String, Object>> fields = new ArrayList<>();
        for (MarkingDto.Field field : ready.marking.fields) {
            List<Integer> ids = new ArrayList<>();
            for (MarkingDto.Element element : field.elements) {
                if (element.page.equals(page)) {
                    ids.add(element.tplId);
                }
            }
            if (ids.isEmpty()) {
                continue;
            }
            TrySession.Value value = session.value(viewModel.templateId(), field.key);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("ids", ids);
            item.put("type", field.type);
            item.put("text", value.text);
            item.put("href", value.href);
            item.put("src", value.image);
            item.put("styles", value.styles);
            fields.add(item);
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("fields", fields);
        payload.put("theme", session.theme(viewModel.templateId()));
        webView.evaluateJavascript("window.RakitTry && RakitTry.apply(" + gson.toJson(payload) + ")", null);
    }

    /** Teks dan link asli elemen di halaman ini dipakai sebagai isi awal form (sekali per isian). */
    private void loadOriginals() {
        if (webView == null || ready == null) {
            return;
        }
        String page = viewModel.page();
        for (MarkingDto.Field field : ready.marking.fields) {
            if (originals.containsKey(field.key) || field.elements.isEmpty()) {
                continue;
            }
            MarkingDto.Element element = null;
            for (MarkingDto.Element e : field.elements) {
                if (e.page.equals(page)) {
                    element = e;
                    break;
                }
            }
            if (element == null) {
                continue;
            }
            String key = field.key;
            webView.evaluateJavascript("window.RakitTry && RakitTry.original(" + element.tplId + ")", value -> {
                Original original = parse(value);
                if (original != null && binding != null) {
                    originals.put(key, original.text);
                    originalHrefs.put(key, original.href);
                    if (originals.size() == countOnPage(page)) {
                        buildForm();
                    }
                }
            });
        }
    }

    private int countOnPage(String page) {
        int count = 0;
        for (MarkingDto.Field field : ready.marking.fields) {
            for (MarkingDto.Element element : field.elements) {
                if (element.page.equals(page)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    @Nullable
    private Original parse(@Nullable String evaluated) {
        try {
            if (evaluated == null || evaluated.equals("null")) {
                return null;
            }
            JsonElement element = JsonParser.parseString(evaluated);
            return gson.fromJson(element.getAsString(), Original.class);
        } catch (RuntimeException e) {
            return null;
        }
    }

    // ---------- form ----------

    private void buildSectionChips(MarkingDto marking) {
        binding.sectionChips.removeAllViews();
        addSectionChip(getString(R.string.try_all_sections), null);
        for (MarkingDto.Section section : marking.sections) {
            boolean used = false;
            for (MarkingDto.Field field : marking.fields) {
                used |= section.id.equals(field.sectionId);
            }
            if (used) {
                addSectionChip(section.name, section.id);
            }
        }
    }

    private void addSectionChip(String name, @Nullable String sectionId) {
        Chip chip = new Chip(requireContext());
        chip.setCheckable(true);
        chip.setText(name);
        chip.setChecked(sectionId == null ? viewModel.sectionId() == null : sectionId.equals(viewModel.sectionId()));
        chip.setOnClickListener(v -> {
            viewModel.setSectionId(sectionId);
            buildForm();
        });
        binding.sectionChips.addView(chip);
    }

    private void buildForm() {
        if (form != null && ready != null) {
            form.build(ready.marking, viewModel.sectionId(), originals, originalHrefs);
        }
    }

    @Override
    public void onChanged() {
        applyPreview();
    }

    @Override
    public void onPickImage(String key) {
        pendingImageKey = key;
        imagePicker.launch("image/*");
    }

    /** "Ubah tandaan ini" (bagian 7.12): kembali ke Tandai, langsung di elemen isian itu. */
    @Override
    public void onEditMark(String key) {
        UploadNav.replaceStep(this, R.id.uploadMarkFragment, viewModel.templateId(), key);
    }

    /** Uji isi panjang (bagian 8): teks sepanjang batasnya dan gambar dengan rasio berbeda dari aslinya. */
    private void fillLongContent() {
        if (ready == null) {
            return;
        }
        boolean wide = true;
        for (MarkingDto.Field field : ready.marking.fields) {
            TrySession.Value value = session.value(viewModel.templateId(), field.key);
            if ("image".equals(field.type)) {
                value.image = ThumbnailImages.toDataUrl(placeholder(wide));
                wide = !wide;
            } else if (!"link".equals(field.type)) {
                value.text = TryForm.longText(requireContext(), field.maxLength);
            }
        }
        buildForm();
        applyPreview();
    }

    /** Gambar uji polos: 3:1 (melebar) atau 1:3 (meninggi), untuk melihat apakah layout tetap rapi. */
    private Bitmap placeholder(boolean wide) {
        int w = wide ? 900 : 300;
        int h = wide ? 300 : 900;
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        new Canvas(bitmap).drawColor(Color.GRAY);
        return bitmap;
    }

    private void pickPage(View anchor) {
        if (ready == null) {
            return;
        }
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        List<MarkingDto.Page> pages = ready.marking.pages;
        for (int i = 0; i < pages.size(); i++) {
            menu.getMenu().add(0, i, i, pages.get(i).name);
        }
        menu.setOnMenuItemClickListener(item -> {
            viewModel.setPage(pages.get(item.getItemId()).file);
            if (webView != null) {
                webView.loadUrl(SiteWebView.urlOf(viewModel.page()));
            }
            updatePageButton();
            return true;
        });
        menu.show();
    }

    private void updatePageButton() {
        if (binding == null || ready == null) {
            return;
        }
        String name = viewModel.page();
        for (MarkingDto.Page page : ready.marking.pages) {
            if (page.file.equals(viewModel.page())) {
                name = page.name;
            }
        }
        binding.pageButton.setText(name);
    }

    private void close() {
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
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        ready = null;
        form = null;
        binding = null;
    }
}
