package com.aris.templateapp.ui.upload;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.PopupMenu;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

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

    /** Teks dan alamat link asli satu elemen (dari {@code RakitTry.originals}). */
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
    // Mencegah permintaan ganda saat halaman melapor "selesai dimuat" lebih dari sekali.
    private boolean loadingOriginals;
    // Jarak (piksel CSS) di atas elemen yang diedit saat preview digulir: kira-kira tinggi header situs yang menempel.
    private static final int PREVIEW_TOP_GAP_CSS = 88;
    // true selama keyboard terbuka: form dinaikkan dan tombol yang tidak dipakai saat mengetik disembunyikan.
    private boolean typing;
    private int formHeight;
    private final ViewTreeObserver.OnGlobalLayoutListener keyboardListener = this::onLayoutChanged;
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
        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(keyboardListener);
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
        // Jawaban dari WebView lama tidak akan datang lagi.
        loadingOriginals = false;
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
                        binding.state.hide();
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

    /**
     * Teks dan link asli elemen di halaman ini dipakai sebagai isi awal form (sekali per isian). Semua diambil dalam
     * satu panggilan JavaScript, lalu form dibangun sekali; membangun form berkali-kali membuat layar beku (ANR).
     */
    private void loadOriginals() {
        if (webView == null || ready == null || loadingOriginals) {
            return;
        }
        String page = viewModel.page();
        Map<Integer, List<String>> keysById = new LinkedHashMap<>();
        for (MarkingDto.Field field : ready.marking.fields) {
            if (originals.containsKey(field.key)) {
                continue;
            }
            for (MarkingDto.Element e : field.elements) {
                if (e.page.equals(page)) {
                    keysById.computeIfAbsent(e.tplId, id -> new ArrayList<>()).add(field.key);
                    break;
                }
            }
        }
        if (keysById.isEmpty()) {
            return;
        }
        loadingOriginals = true;
        String ids = gson.toJson(new ArrayList<>(keysById.keySet()));
        webView.evaluateJavascript("window.RakitTry ? RakitTry.originals(" + ids + ") : null", value -> {
            loadingOriginals = false;
            if (binding == null) {
                return;
            }
            Map<String, Original> found = parseOriginals(value);
            for (Map.Entry<Integer, List<String>> entry : keysById.entrySet()) {
                Original original = found.get(String.valueOf(entry.getKey()));
                for (String key : entry.getValue()) {
                    // Disimpan walau elemennya tidak ditemukan, agar tidak diminta ulang terus.
                    originals.put(key, original == null ? null : original.text);
                    originalHrefs.put(key, original == null ? null : original.href);
                }
            }
            buildForm();
        });
    }

    private Map<String, Original> parseOriginals(@Nullable String evaluated) {
        try {
            if (evaluated == null || evaluated.equals("null")) {
                return Collections.emptyMap();
            }
            JsonElement element = JsonParser.parseString(evaluated);
            Map<String, Original> map = gson.fromJson(element.getAsString(),
                    new TypeToken<Map<String, Original>>() { }.getType());
            return map == null ? Collections.emptyMap() : map;
        } catch (RuntimeException e) {
            return Collections.emptyMap();
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

    /** Preview digulir ke elemen isian yang sedang diketik, agar perubahannya langsung terlihat. */
    @Override
    public void onFieldFocused(String key) {
        if (binding != null && typing) {
            // Pindah kolom saat keyboard terbuka (mis. tombol "berikutnya"): label kolom baru ikut terlihat.
            binding.formScroll.post(this::scrollFocusedIntoView);
        }
        if (webView == null || ready == null) {
            return;
        }
        for (MarkingDto.Field field : ready.marking.fields) {
            if (!field.key.equals(key)) {
                continue;
            }
            for (MarkingDto.Element element : field.elements) {
                if (element.page.equals(viewModel.page())) {
                    // Elemen diletakkan sedikit di bawah tepi atas, agar tidak tertutup header situs yang menempel.
                    webView.evaluateJavascript("(function(){var e=document.querySelector('[data-tpl-id=\"" + element.tplId
                            + "\"]');if(e){window.scrollTo({top:e.getBoundingClientRect().top+window.scrollY-"
                            + PREVIEW_TOP_GAP_CSS + ",behavior:'smooth'});}})()", null);
                    return;
                }
            }
        }
    }

    /**
     * Mode fokus saat keyboard terbuka: hanya judul wizard, strip preview (digulir ke elemen yang diedit), dan isian
     * yang sedang diketik yang tampil. Pemilih halaman, chip section, pegangan sheet, "Uji isi panjang", dan tombol
     * Kirim/Lanjut menandai disembunyikan sementara. Tanpa ini, form tetap di posisi semula dan kolomnya tertutup
     * keyboard.
     */
    private void onLayoutChanged() {
        if (binding == null) {
            return;
        }
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(binding.getRoot());
        boolean keyboard = insets != null && insets.isVisible(WindowInsetsCompat.Type.ime());
        if (keyboard == typing) {
            // Form masih bergerak naik setelah keyboard muncul: gulir ulang begitu ukurannya berubah.
            Rect shown = new Rect();
            int height = binding.formScroll.getGlobalVisibleRect(shown) ? shown.height() : 0;
            if (typing && height != formHeight) {
                formHeight = height;
                binding.formScroll.post(this::scrollFocusedIntoView);
            }
            return;
        }
        typing = keyboard;
        formHeight = 0;
        int hidden = keyboard ? View.GONE : View.VISIBLE;
        for (View view : new View[] {binding.controlsRow, binding.toolsRow, binding.dragHandle, binding.sectionScroll,
                binding.sheetActions}) {
            view.setVisibility(hidden);
        }
        // Preview memanjang sampai ke balik form agar tidak ada celah kosong di antara keduanya.
        binding.content.setPadding(0, 0, 0, keyboard ? 0 : getResources().getDimensionPixelSize(R.dimen.try_sheet_peek));
        BottomSheetBehavior<View> sheet = BottomSheetBehavior.from(binding.sheet);
        if (keyboard) {
            sheet.setExpandedOffset(binding.header.getRoot().getBottom()
                    + getResources().getDimensionPixelSize(R.dimen.try_sheet_typing_preview));
            sheet.setState(BottomSheetBehavior.STATE_EXPANDED);
            binding.formScroll.post(this::scrollFocusedIntoView);
        } else {
            sheet.setExpandedOffset(getResources().getDimensionPixelSize(R.dimen.try_sheet_expanded_offset));
        }
    }

    /**
     * Kolom yang sedang diketik digulir ke atas form, tepat di atas keyboard. Label isian ikut terlihat jika muat;
     * jika tidak (mis. kolom kode warna jauh di bawah label), kolom itu sendiri yang diutamakan.
     */
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
        // Sheet setinggi layar lalu digeser ke bawah, jadi sebagian form ada di luar layar: pakai bagian yang terlihat.
        Rect shown = new Rect();
        int visible = binding.formScroll.getGlobalVisibleRect(shown) ? shown.height() : binding.formScroll.getHeight();
        int target = rect.bottom - row.getTop() <= visible ? row.getTop()
                : rect.top - getResources().getDimensionPixelSize(R.dimen.space_8);
        binding.formScroll.smoothScrollTo(0, Math.max(0, target));
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
        binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(keyboardListener);
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        ready = null;
        form = null;
        binding = null;
    }
}
