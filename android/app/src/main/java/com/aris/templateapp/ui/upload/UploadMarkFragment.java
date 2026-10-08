package com.aris.templateapp.ui.upload;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupMenu;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.aris.templateapp.R;
import com.aris.templateapp.core.storage.EditorTourStore;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.remote.dto.MarkingDto;
import com.aris.templateapp.databinding.FragmentUploadMarkBinding;
import com.aris.templateapp.ui.common.ErrorMessages;
import com.aris.templateapp.ui.guide.Guide;
import com.aris.templateapp.ui.guide.GuideFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Langkah 4 Upload: Tandai bagian (alur-fitur-upload.md bagian 7). Website tampil per section seperti slide; provider
 * mengetuk elemen lalu menetapkannya sebagai isian lewat bottom sheet. Semua data tandaan ada di
 * {@link MarkingEditor}; layar ini menghubungkan WebView ({@link MarkWebView}) dengan editor itu.
 */
@AndroidEntryPoint
public class UploadMarkFragment extends Fragment implements MarkWebView.Listener {

    private static final int[] TOUR = {R.string.mark_tour_1, R.string.mark_tour_2, R.string.mark_tour_3,
            R.string.mark_tour_4};
    private static final Type SUGGESTIONS = new TypeToken<List<MarkingEditor.Suggestion>>() { }.getType();
    private static final Type ELEMENTS = new TypeToken<List<MarkPanels.ElementItem>>() { }.getType();
    private static final Type SIMILAR = new TypeToken<List<Similar>>() { }.getType();
    private static final Type VISIBILITY = new TypeToken<Map<Integer, Boolean>>() { }.getType();

    /** Satu hasil {@code RakitMark.similarUnlinked}. */
    static class Similar {
        int id;
        String text;
        int matchId;
    }

    /** Info halaman dari {@code RakitMark.metrics}. */
    static class Metrics {
        int innerWidth;
        int docHeight;
    }

    @Inject
    EditorTourStore tourStore;

    private FragmentUploadMarkBinding binding;
    private MarkEditorViewModel viewModel;
    @Nullable
    private MarkWebView web;
    @Nullable
    private MarkEditorViewModel.Ready ready;
    @Nullable
    private OffscreenPage scanPage;
    private final List<int[]> ranges = new ArrayList<>();
    private Metrics metrics;
    private boolean pageReady;
    private int tourIndex;
    private boolean focusHandled;
    private final Gson gson = new Gson();

    private final OnBackPressedCallback back = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            close();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUploadMarkBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        WizardHeader.bind(binding.header, 4, R.string.upload_step_mark, this::close);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), back);

        viewModel = new ViewModelProvider(this).get(MarkEditorViewModel.class);
        viewModel.getReady().observe(getViewLifecycleOwner(), this::onReady);
        viewModel.getVersion().observe(getViewLifecycleOwner(), v -> renderEditorState());
        viewModel.getSaving().observe(getViewLifecycleOwner(), saving -> binding.saveButton.setEnabled(!saving));
        viewModel.getMessage().observe(getViewLifecycleOwner(), event -> {
            Integer text = event.getContentIfNotHandled();
            if (text != null) {
                Snackbar.make(binding.getRoot(), text, Snackbar.LENGTH_SHORT).show();
            }
        });
        viewModel.getRejected().observe(getViewLifecycleOwner(), event -> {
            String reason = event.getContentIfNotHandled();
            if (reason != null) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.mark_save_rejected_title)
                        .setMessage(reason)
                        .setPositiveButton(R.string.action_ok, null)
                        .show();
            }
        });
        viewModel.getAfterSave().observe(getViewLifecycleOwner(), event -> {
            Runnable then = event.getContentIfNotHandled();
            if (then != null) {
                then.run();
            }
        });
        viewModel.getRestoreOffer().observe(getViewLifecycleOwner(), event -> {
            String json = event.getContentIfNotHandled();
            if (json != null) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.mark_restore_title)
                        .setMessage(R.string.mark_restore_body)
                        .setNegativeButton(R.string.mark_discard, (d, w) -> viewModel.discardBackup())
                        .setPositiveButton(R.string.mark_restore, (d, w) -> {
                            viewModel.restore(json);
                            refreshSlide();
                        })
                        .setCancelable(false)
                        .show();
            }
        });

        binding.viewToggle.check("desktop".equals(viewModel.view()) ? R.id.view_desktop : R.id.view_mobile);
        binding.viewToggle.addOnButtonCheckedListener((group, id, checked) -> {
            String selected = id == R.id.view_desktop ? "desktop" : "mobile";
            if (checked && !selected.equals(viewModel.view())) {
                viewModel.setView(selected);
                createWebView();
            }
        });
        binding.pageButton.setOnClickListener(this::pickPage);
        binding.undoButton.setOnClickListener(v -> {
            viewModel.editor().undo();
            viewModel.changed();
            refreshSlide();
        });
        binding.redoButton.setOnClickListener(v -> {
            viewModel.editor().redo();
            viewModel.changed();
            refreshSlide();
        });
        binding.elementsButton.setOnClickListener(v -> showElements());
        binding.fieldsButton.setOnClickListener(v -> showFields());
        binding.sectionsButton.setOnClickListener(v -> showSections());
        binding.helpButton.setOnClickListener(v -> GuideFragment.open(this, Guide.MARK_EDITABLE));
        binding.saveButton.setOnClickListener(v -> viewModel.save(null));
        binding.tryButton.setOnClickListener(v -> tryAsUser());
        binding.tourNext.setOnClickListener(v -> nextTip());

        viewModel.start(requireArguments().getString(UploadNav.ARG_TEMPLATE_ID, ""));
    }

    // ---------- memuat ----------

    private void onReady(@Nullable Resource<MarkEditorViewModel.Ready> resource) {
        if (resource == null) {
            return;
        }
        if (resource.getStatus() == Resource.Status.LOADING) {
            binding.state.setVisibility(View.VISIBLE);
            binding.state.showLoading();
            return;
        }
        if (resource.getStatus() == Resource.Status.ERROR) {
            binding.state.setVisibility(View.VISIBLE);
            binding.state.showError(ErrorMessages.forError(requireContext(), resource.getError()), viewModel::load);
            return;
        }
        if (ready != null) {
            return;
        }
        ready = resource.getData();
        viewModel.recordStep(4);
        binding.pageButton.setVisibility(ready.pages.size() > 1 ? View.VISIBLE : View.INVISIBLE);
        createWebView();
        scanOtherPages();
        if (!tourStore.seen()) {
            tourIndex = 0;
            showTip();
        }
    }

    /** WebView baru setiap ganti tampilan HP/Desktop, karena lebar halaman (viewport) ikut berubah. */
    private void createWebView() {
        if (ready == null) {
            return;
        }
        if (web != null) {
            binding.webFrame.removeView(web.view());
            web.destroy();
        }
        try {
            web = new MarkWebView(requireContext(), ready.siteRoot, ready.allowedHosts,
                    "desktop".equals(viewModel.view()), this);
        } catch (IOException e) {
            binding.state.showError(getString(R.string.error_unknown), this::createWebView);
            return;
        }
        binding.webFrame.addView(web.view(), new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        pageReady = false;
        binding.state.setVisibility(View.VISIBLE);
        binding.state.showLoading();
        web.load(viewModel.page());
    }

    /** Halaman lain dipindai di WebView tersembunyi: daftar section + sidik jari untuk bagian 7.6. */
    private void scanOtherPages() {
        if (ready == null || viewModel.scanned() != null || ready.pages.size() < 2) {
            return;
        }
        scanPage = new OffscreenPage(binding.hostFrame);
        new PageScanner(scanPage).scan(ready.siteRoot, ready.allowedHosts, ready.pages, result -> {
            if (binding != null) {
                viewModel.setScanned(result);
            }
        });
    }

    @Override
    public void onPageReady() {
        MarkWebView current = web;
        if (current == null || binding == null) {
            return;
        }
        current.call("unstick", ignored -> {
            String page = viewModel.page();
            if (viewModel.editor().sectionsOf(page).isEmpty()) {
                current.detectSections(value -> {
                    viewModel.editor().applyDetectedSections(page, SectionInfo.parse(value));
                    afterSectionsKnown();
                });
            } else {
                afterSectionsKnown();
            }
        });
    }

    private void afterSectionsKnown() {
        pageReady = true;
        binding.state.hide();
        updateVisibility();
        refreshSlide();
        // "Ubah tandaan ini" dari langkah Coba: langsung buka elemen isian itu (sekali saja).
        String focus = requireArguments().getString(UploadNav.ARG_FOCUS_KEY);
        if (focus != null && !focusHandled) {
            focusHandled = true;
            MarkingDto.Field field = viewModel.editor().field(focus);
            if (field != null) {
                jumpTo(field);
            }
        }
    }

    /** Elemen bertanda di halaman ini terlihat di tampilan sekarang atau tidak (HANYA HP / HANYA DESKTOP). */
    private void updateVisibility() {
        MarkWebView current = web;
        List<Integer> ids = markedIds();
        if (current == null || ids.isEmpty()) {
            return;
        }
        current.call("visibility", value -> {
            Map<Integer, Boolean> visible = parse(value, VISIBILITY);
            if (visible != null) {
                viewModel.editor().updateVisibility(viewModel.page(), viewModel.view(), visible);
                setMarks();
            }
        }, ids);
    }

    // ---------- slide (bagian 7.2) ----------

    /** Hitung batas setiap section dari posisi elemennya, lalu tampilkan section yang sedang dibuka. */
    private void refreshSlide() {
        MarkWebView current = web;
        if (current == null || !pageReady) {
            renderEditorState();
            return;
        }
        List<MarkingDto.Section> sections = viewModel.editor().sectionsOf(viewModel.page());
        List<Integer> ids = new ArrayList<>();
        for (MarkingDto.Section section : sections) {
            ids.add(section.tplId);
        }
        current.query("metrics", Metrics.class, result -> {
            if (result == null || binding == null) {
                return;
            }
            metrics = result;
            current.call("tops", value -> {
                Integer[] tops = parse(value, Integer[].class);
                computeRanges(tops == null ? new Integer[0] : tops, sections.size());
                showSlide();
            }, ids);
        });
    }

    /** Section pertama mulai dari atas halaman, section terakhir sampai ujung halaman (bagian 7.1). */
    private void computeRanges(Integer[] tops, int count) {
        ranges.clear();
        int previous = 0;
        List<Integer> starts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Integer top = i < tops.length ? tops[i] : null;
            int start = i == 0 ? 0 : (top == null ? previous : Math.max(previous, top));
            starts.add(start);
            previous = start;
        }
        for (int i = 0; i < count; i++) {
            int end = i == count - 1 ? metrics.docHeight : Math.max(starts.get(i) + 1, starts.get(i + 1));
            ranges.add(new int[]{starts.get(i), end});
        }
        if (ranges.isEmpty()) {
            ranges.add(new int[]{0, metrics.docHeight});
        }
    }

    private void showSlide() {
        MarkWebView current = web;
        if (current == null || binding == null || ranges.isEmpty()) {
            return;
        }
        int index = Math.min(viewModel.sectionIndex(), ranges.size() - 1);
        viewModel.setSectionIndex(index);
        int[] range = ranges.get(index);
        current.call("slide", null, range[0], range[1]);
        // Tinggi slide mengikuti tinggi asli section × skala, tetapi tidak lebih dari ruang yang tersedia.
        float scale = current.scale(metrics.innerWidth);
        int height = Math.round((range[1] - range[0]) * scale) + 2 * getResources().getDimensionPixelSize(R.dimen.border_width);
        int available = binding.stage.getHeight();
        ViewGroup.LayoutParams params = binding.webFrame.getLayoutParams();
        params.height = available > 0 ? Math.min(height, available) : ViewGroup.LayoutParams.MATCH_PARENT;
        binding.webFrame.setLayoutParams(params);
        setMarks();
        renderEditorState();
        updateHintBar();
    }

    private void setMarks() {
        if (web == null) {
            return;
        }
        List<Map<String, Object>> marks = new ArrayList<>();
        for (MarkingDto.Field field : viewModel.editor().fields()) {
            for (MarkingDto.Element element : field.elements) {
                if (element.page.equals(viewModel.page())) {
                    Map<String, Object> mark = new HashMap<>();
                    mark.put("id", element.tplId);
                    mark.put("label", field.label + onlyLabel(element));
                    marks.add(mark);
                }
            }
        }
        web.call("setMarks", null, marks);
    }

    private String onlyLabel(MarkingDto.Element element) {
        if (element.visibleIn.size() != 1) {
            return "";
        }
        return " · " + getString("desktop".equals(element.visibleIn.get(0)) ? R.string.mark_only_desktop
                : R.string.mark_only_mobile);
    }

    private List<Integer> markedIds() {
        List<Integer> ids = new ArrayList<>();
        for (MarkingDto.Field field : viewModel.editor().fields()) {
            for (MarkingDto.Element element : field.elements) {
                if (element.page.equals(viewModel.page())) {
                    ids.add(element.tplId);
                }
            }
        }
        return ids;
    }

    @Nullable
    private MarkingDto.Section currentSection() {
        List<MarkingDto.Section> sections = viewModel.editor().sectionsOf(viewModel.page());
        int index = viewModel.sectionIndex();
        return index < sections.size() ? sections.get(index) : null;
    }

    // ---------- saran & peringatan teks mirip (bagian 7.5 & 7.10 A4) ----------

    private void updateHintBar() {
        MarkWebView current = web;
        if (current == null || ranges.isEmpty()) {
            return;
        }
        int[] range = ranges.get(viewModel.sectionIndex());
        List<Integer> marked = markedIds();
        if (offerCrossPageLink()) {
            return;
        }
        current.call("similarUnlinked", value -> {
            List<Similar> similar = parse(value, SIMILAR);
            if (similar != null && !similar.isEmpty()) {
                Similar first = similar.get(0);
                showHint(getString(R.string.mark_similar, first.text), R.string.mark_link_now, () -> linkSimilar(first));
                return;
            }
            current.call("suggest", suggestValue -> {
                List<MarkingEditor.Suggestion> suggestions = parse(suggestValue, SUGGESTIONS);
                if (suggestions == null || suggestions.isEmpty()) {
                    binding.hintBar.setVisibility(View.GONE);
                    return;
                }
                showHint(getString(R.string.mark_suggest, suggestions.size()), R.string.mark_mark_all,
                        () -> markAll(suggestions));
            }, range[0], range[1], marked);
        }, range[0], range[1], marked);
    }

    /**
     * Isian di halaman lain yang kembarannya (struktur & isi sama, mis. nama toko di header) ada di halaman ini tapi
     * belum dihubungkan (bagian 7.5–7.6). Tawaran "sama di N halaman" hanya muncul saat menandai, jadi isian yang dulu
     * ditandai "halaman ini saja" diingatkan lagi di sini.
     *
     * @return true jika bar saran dipakai untuk tawaran ini
     */
    private boolean offerCrossPageLink() {
        Map<String, List<PageScanner.ScannedSection>> scanned = viewModel.scanned();
        if (scanned == null) {
            return false;
        }
        String page = viewModel.page();
        MarkingEditor editor = viewModel.editor();
        for (MarkingDto.Field field : editor.fields()) {
            for (MarkingDto.Element element : field.elements) {
                if (element.page.equals(page)) {
                    continue;
                }
                Integer twin = PageScanner.sameElementElsewhere(scanned, element.page, element.tplId).get(page);
                if (twin == null || editor.fieldOf(page, twin) != null) {
                    continue;
                }
                showHint(getString(R.string.mark_same_unlinked, field.label), R.string.mark_link_now, () -> {
                    List<MarkingDto.Element> added = new ArrayList<>();
                    added.add(new MarkingDto.Element(page, twin, new ArrayList<>(element.visibleIn)));
                    editor.linkElements(field.key, added);
                    viewModel.changed();
                    Snackbar.make(binding.getRoot(), getString(R.string.mark_linked, field.label),
                            Snackbar.LENGTH_SHORT).show();
                    setMarks();
                    updateHintBar();
                });
                return true;
            }
        }
        return false;
    }

    private void showHint(String text, int action, Runnable onAction) {
        binding.hintBar.setVisibility(View.VISIBLE);
        binding.hintText.setText(text);
        binding.hintAction.setText(action);
        binding.hintAction.setOnClickListener(v -> onAction.run());
    }

    private void linkSimilar(Similar similar) {
        MarkingDto.Field field = viewModel.editor().fieldOf(viewModel.page(), similar.matchId);
        if (field == null) {
            return;
        }
        viewModel.editor().linkElement(field.key, element(similar.id, true));
        viewModel.changed();
        Snackbar.make(binding.getRoot(), getString(R.string.mark_linked, field.label), Snackbar.LENGTH_SHORT).show();
        setMarks();
        updateHintBar();
    }

    private void markAll(List<MarkingEditor.Suggestion> suggestions) {
        MarkingDto.Section section = currentSection();
        int added = viewModel.editor().addSuggestions(viewModel.page(), section == null ? null : section.id,
                suggestions);
        viewModel.changed();
        Snackbar.make(binding.getRoot(), getString(R.string.mark_suggest_added, added), Snackbar.LENGTH_SHORT).show();
        setMarks();
        updateHintBar();
    }

    // ---------- menandai elemen (bagian 7.3) ----------

    @Override
    public void onElementTapped(int tplId) {
        MarkWebView current = web;
        if (current == null || binding == null) {
            return;
        }
        current.call("select", null, tplId);
        current.query("describe", ElementInfo.class, info -> {
            if (info == null || binding == null) {
                return;
            }
            binding.breadcrumb.setVisibility(View.VISIBLE);
            binding.breadcrumb.setText(info.breadcrumb);
            openSheet(info);
        }, tplId);
    }

    @Override
    public void onGeneratedTapped() {
        Snackbar.make(binding.getRoot(), R.string.mark_generated, Snackbar.LENGTH_LONG).show();
    }

    /** Isian pemilik elemen-elemen bertanda (tanpa duplikat), untuk pengecekan tandaan bersarang. */
    private List<MarkingDto.Field> fieldsOf(String page, @Nullable List<Integer> ids) {
        List<MarkingDto.Field> fields = new ArrayList<>();
        if (ids == null) {
            return fields;
        }
        for (Integer id : ids) {
            MarkingDto.Field field = viewModel.editor().fieldOf(page, id);
            if (field != null && !fields.contains(field)) {
                fields.add(field);
            }
        }
        return fields;
    }

    private void openSheet(ElementInfo info) {
        MarkingEditor editor = viewModel.editor();
        String page = viewModel.page();
        MarkingDto.Field existing = editor.fieldOf(page, info.id);
        MarkElementSheet.show(this, info, existing, editor.fields(), fieldsOf(page, info.markedAncestors),
                fieldsOf(page, info.markedDescendants), new MarkElementSheet.Callbacks() {
            @Override
            public void onSave(@Nullable String existingKey, MarkingDto.Field edited) {
                MarkingDto.Section section = currentSection();
                edited.sectionId = section == null ? null : section.id;
                MarkingDto.Field saved = editor.saveField(existingKey, edited, element(info.id, info.visible));
                viewModel.changed();
                clearSelection();
                setMarks();
                updateHintBar();
                if (existingKey == null) {
                    offerSameElsewhere(saved, info.id);
                }
            }

            @Override
            public void onUnmark() {
                editor.unmark(page, info.id);
                viewModel.changed();
                clearSelection();
                setMarks();
                updateHintBar();
            }

            @Override
            public void onLinkTo(String key) {
                editor.linkElement(key, element(info.id, info.visible));
                viewModel.changed();
                clearSelection();
                setMarks();
                MarkingDto.Field target = editor.field(key);
                if (target != null) {
                    Snackbar.make(binding.getRoot(), getString(R.string.mark_linked, target.label), Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onParent() {
                relative("parentOf", info.id);
            }

            @Override
            public void onChild() {
                relative("childOf", info.id);
            }

            @Override
            public void onMakeSection() {
                makeSection(info);
            }

            @Override
            public void onClosed() {
                clearSelection();
            }
        });
    }

    private MarkingDto.Element element(int tplId, boolean visibleNow) {
        List<String> views = new ArrayList<>();
        if (visibleNow) {
            // Kebanyakan elemen tampil di kedua tampilan; label HANYA HP/DESKTOP muncul setelah tampilan lain
            // dibuka dan elemen ternyata tersembunyi di sana (updateVisibility).
            views.add("mobile");
            views.add("desktop");
        } else {
            // Elemen tersembunyi di tampilan ini (mis. menu HP saat Desktop): dianggap tampil di tampilan lain.
            views.add("desktop".equals(viewModel.view()) ? "mobile" : "desktop");
        }
        return new MarkingDto.Element(viewModel.page(), tplId, views);
    }

    private void relative(String function, int tplId) {
        MarkWebView current = web;
        if (current == null) {
            return;
        }
        current.call(function, value -> {
            Integer target = parse(value, Integer.class);
            if (target != null) {
                onElementTapped(target);
            } else {
                clearSelection();
            }
        }, tplId);
    }

    private void clearSelection() {
        if (web != null) {
            web.call("clearSelection", null);
        }
        if (binding != null) {
            binding.breadcrumb.setVisibility(View.GONE);
        }
    }

    /** "Jadikan section" (bagian 7.1): elemen terpilih menjadi awal section baru. */
    private void makeSection(ElementInfo info) {
        clearSelection();
        int index = 0;
        for (int i = 0; i < ranges.size(); i++) {
            if (ranges.get(i)[0] <= info.top) {
                index = i + 1;
            }
        }
        String name = info.text == null || info.text.isEmpty()
                ? getString(R.string.cd_slide, index + 1, info.tag) : info.text;
        viewModel.editor().splitAt(viewModel.page(), info.id, name.length() > 40 ? name.substring(0, 40) : name, index);
        viewModel.setSectionIndex(index);
        viewModel.changed();
        refreshSlide();
    }

    /** Header/footer yang sama di beberapa halaman: tawarkan tandai sekali untuk semua (bagian 7.6). */
    private void offerSameElsewhere(MarkingDto.Field field, int tplId) {
        Map<String, List<PageScanner.ScannedSection>> scanned = viewModel.scanned();
        if (scanned == null) {
            return;
        }
        Map<String, Integer> matches = PageScanner.sameElementElsewhere(scanned, viewModel.page(), tplId);
        if (matches.isEmpty()) {
            return;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.mark_same_title, matches.size() + 1))
                .setMessage(R.string.mark_same_body)
                .setNegativeButton(R.string.mark_same_no, null)
                .setPositiveButton(R.string.mark_same_yes, (d, w) -> {
                    List<MarkingDto.Element> elements = new ArrayList<>();
                    for (Map.Entry<String, Integer> match : matches.entrySet()) {
                        elements.add(new MarkingDto.Element(match.getKey(), match.getValue(),
                                new ArrayList<>(field.elements.get(0).visibleIn)));
                    }
                    viewModel.editor().linkElements(field.key, elements);
                    viewModel.changed();
                })
                .show();
    }

    // ---------- panel ----------

    private void showElements() {
        MarkWebView current = web;
        if (current == null || ranges.isEmpty()) {
            return;
        }
        int[] range = ranges.get(viewModel.sectionIndex());
        current.call("elementsIn", value -> {
            List<MarkPanels.ElementItem> items = parse(value, ELEMENTS);
            MarkPanels.showElements(this, items == null ? new ArrayList<>() : items, viewModel.editor(),
                    viewModel.page(), this::onElementTapped);
        }, range[0], range[1]);
    }

    private void showFields() {
        if (ready == null) {
            return;
        }
        MarkPanels.showFields(this, viewModel.editor(), ready.cssVariables, this::jumpTo, (var, label, type) -> {
            viewModel.editor().setTheme(var, label, type);
            viewModel.changed();
        });
    }

    /** Ketuk isian di panel → lompat ke elemennya (halaman & section yang benar). */
    private void jumpTo(MarkingDto.Field field) {
        if (field.elements.isEmpty()) {
            return;
        }
        MarkingDto.Element target = field.elements.get(0);
        List<MarkingDto.Section> sections = viewModel.editor().sectionsOf(target.page);
        int index = 0;
        for (int i = 0; i < sections.size(); i++) {
            if (sections.get(i).id.equals(field.sectionId)) {
                index = i;
            }
        }
        if (!target.page.equals(viewModel.page())) {
            viewModel.setPage(target.page);
            viewModel.setSectionIndex(index);
            pageReady = false;
            binding.state.setVisibility(View.VISIBLE);
            binding.state.showLoading();
            if (web != null) {
                web.load(target.page);
            }
            return;
        }
        viewModel.setSectionIndex(index);
        showSlide();
        onElementTapped(target.tplId);
    }

    private void showSections() {
        List<MarkingDto.Section> sections = viewModel.editor().sectionsOf(viewModel.page());
        MarkPanels.showSections(this, sections, new MarkPanels.SectionAction() {
            @Override
            public void rename(MarkingDto.Section section, String name) {
                viewModel.editor().renameSection(section.id, name);
                viewModel.changed();
            }

            @Override
            public void merge(MarkingDto.Section section) {
                viewModel.editor().mergeWithNext(section.id);
                viewModel.changed();
                refreshSlide();
            }

            @Override
            public void delete(MarkingDto.Section section) {
                viewModel.editor().deleteSection(section.id);
                viewModel.setSectionIndex(Math.max(0, viewModel.sectionIndex() - 1));
                viewModel.changed();
                refreshSlide();
            }

            @Override
            public void open(int index) {
                viewModel.setSectionIndex(index);
                showSlide();
            }
        });
    }

    private void pickPage(View anchor) {
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        List<MarkingDto.Page> pages = viewModel.editor().data().pages;
        for (int i = 0; i < pages.size(); i++) {
            menu.getMenu().add(0, i, i, pages.get(i).name);
        }
        menu.setOnMenuItemClickListener(item -> {
            String file = pages.get(item.getItemId()).file;
            if (!file.equals(viewModel.page()) && web != null) {
                viewModel.setPage(file);
                pageReady = false;
                binding.state.setVisibility(View.VISIBLE);
                binding.state.showLoading();
                web.load(file);
                renderEditorState();
            }
            return true;
        });
        menu.show();
    }

    // ---------- tampilan penghitung, strip, tombol ----------

    private void renderEditorState() {
        if (binding == null) {
            return;
        }
        MarkingEditor editor = viewModel.editor();
        MarkingDto.Page page = editor.page(viewModel.page());
        binding.pageButton.setText(page == null ? viewModel.page() : page.name);

        int count = editor.fields().size();
        boolean enough = count >= MarkingEditor.MIN_FIELDS_TO_SEND;
        binding.counter.setText(enough ? getString(R.string.mark_counter_ok, count)
                : getString(R.string.mark_counter, count, MarkingEditor.MIN_FIELDS_TO_SEND));
        binding.counter.setTextColor(ContextCompat.getColor(requireContext(),
                enough ? R.color.color_success : R.color.color_muted));

        int unsaved = editor.unsavedCount();
        binding.unsaved.setText(unsaved > 0 ? getString(R.string.mark_unsaved, unsaved) : getString(R.string.mark_all_saved));
        binding.unsaved.setTextColor(ContextCompat.getColor(requireContext(),
                unsaved > 0 ? R.color.color_warning : R.color.color_muted));
        binding.undoButton.setEnabled(editor.canUndo());
        binding.undoButton.setAlpha(editor.canUndo() ? 1f : 0.38f);
        binding.redoButton.setEnabled(editor.canRedo());
        binding.redoButton.setAlpha(editor.canRedo() ? 1f : 0.38f);
        // Coba aktif begitu ada minimal 1 isian tersimpan (bagian 7.7).
        binding.tryButton.setEnabled(editor.savedFieldCount() > 0);

        List<MarkingDto.Section> sections = editor.sectionsOf(viewModel.page());
        int index = Math.min(viewModel.sectionIndex(), Math.max(0, sections.size() - 1));
        MarkingDto.Section current = sections.isEmpty() ? null : sections.get(index);
        binding.slideInfo.setText(current == null ? getString(R.string.mark_loading_page)
                : getString(R.string.mark_slide, index + 1, sections.size(), current.name));
        renderStrip(sections, index);
    }

    private void renderStrip(List<MarkingDto.Section> sections, int index) {
        binding.strip.removeAllViews();
        for (int i = 0; i < sections.size(); i++) {
            MarkingDto.Section section = sections.get(i);
            boolean marked = false;
            for (MarkingDto.Field field : viewModel.editor().fields()) {
                marked |= section.id.equals(field.sectionId);
            }
            Chip chip = new Chip(requireContext());
            chip.setCheckable(true);
            chip.setText(getString(marked ? R.string.mark_strip_chip_marked : R.string.mark_strip_chip, i + 1, section.name));
            chip.setContentDescription(getString(R.string.cd_slide, i + 1, section.name));
            chip.setChecked(i == index);
            int target = i;
            chip.setOnClickListener(v -> {
                viewModel.setSectionIndex(target);
                clearSelection();
                showSlide();
            });
            binding.strip.addView(chip);
        }
    }

    // ---------- tur pertama kali (bagian 7.10 A6) ----------

    private void showTip() {
        binding.tourCard.setVisibility(View.VISIBLE);
        binding.tourCount.setText(getString(R.string.mark_tour_count, tourIndex + 1));
        binding.tourText.setText(TOUR[tourIndex]);
        binding.tourNext.setText(tourIndex == TOUR.length - 1 ? R.string.mark_tour_done : R.string.mark_tour_next);
    }

    private void nextTip() {
        tourIndex++;
        if (tourIndex >= TOUR.length) {
            binding.tourCard.setVisibility(View.GONE);
            tourStore.markSeen();
        } else {
            showTip();
        }
    }

    // ---------- Coba & keluar (bagian 7.12) ----------

    private void tryAsUser() {
        MarkingEditor editor = viewModel.editor();
        if (editor.hasUnsavedChanges()) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.mark_try_unsaved_title, editor.unsavedCount()))
                    .setMessage(R.string.mark_try_unsaved_body)
                    .setNegativeButton(R.string.action_cancel, null)
                    .setPositiveButton(R.string.mark_save_and_try, (d, w) -> viewModel.save(this::openTry))
                    .show();
        } else {
            openTry();
        }
    }

    private void openTry() {
        viewModel.recordStep(5);
        UploadNav.replaceStep(this, R.id.uploadTryFragment, viewModel.templateId());
    }

    private void close() {
        if (viewModel.editor().hasUnsavedChanges()) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.upload_exit_title)
                    .setMessage(R.string.mark_exit_unsaved_body)
                    .setPositiveButton(R.string.mark_save_and_exit, (d, w) -> viewModel.save(() -> UploadNav.exit(this)))
                    .setNegativeButton(R.string.mark_exit_discard, (d, w) -> {
                        viewModel.discardBackup();
                        UploadNav.exit(this);
                    })
                    .setNeutralButton(R.string.action_cancel, null)
                    .show();
        } else {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.upload_exit_title)
                    .setMessage(R.string.upload_exit_draft_body)
                    .setNegativeButton(R.string.action_cancel, null)
                    .setPositiveButton(R.string.upload_exit, (d, w) -> UploadNav.exit(this))
                    .show();
        }
    }

    @Nullable
    private <T> T parse(@Nullable String evaluated, Type type) {
        try {
            if (evaluated == null || evaluated.equals("null")) {
                return null;
            }
            // Fungsi yang mengembalikan string JSON dibungkus tanda kutip oleh evaluateJavascript; angka tidak.
            JsonElement element = JsonParser.parseString(evaluated);
            String json = element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()
                    ? element.getAsString() : evaluated;
            return gson.fromJson(json, type);
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (web != null) {
            web.destroy();
            web = null;
        }
        if (scanPage != null) {
            scanPage.destroy();
            scanPage = null;
        }
        ready = null;
        binding = null;
    }
}
