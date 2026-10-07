package com.aris.templateapp.ui.upload;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.R;
import com.aris.templateapp.core.upload.MarkingBackupStore;
import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.remote.dto.DraftDto;
import com.aris.templateapp.data.remote.dto.MarkingDto;
import com.aris.templateapp.data.remote.dto.TechInfoDto;
import com.aris.templateapp.data.remote.dto.UploadSettingsDto;
import com.aris.templateapp.data.repository.UploadRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Editor Tandai bagian (alur-fitur-upload.md bagian 7). Isi tandaan ada di {@link MarkingEditor}; ViewModel ini
 * memuat data dari server, mencadangkan perubahan yang belum disimpan ke HP (bagian 7.12), dan menyimpan ke server.
 */
@HiltViewModel
public class MarkEditorViewModel extends ViewModel {

    /** Semua bahan editor siap. */
    static final class Ready {
        final File siteRoot;
        final List<String> allowedHosts;
        final List<String> pages;
        final List<TechInfoDto.CssVariableDto> cssVariables;

        Ready(File siteRoot, List<String> allowedHosts, List<String> pages, List<TechInfoDto.CssVariableDto> cssVariables) {
            this.siteRoot = siteRoot;
            this.allowedHosts = allowedHosts;
            this.pages = pages;
            this.cssVariables = cssVariables;
        }
    }

    private final UploadRepository repository;
    private final MarkingBackupStore backupStore;
    private final AppExecutors executors;
    private final MarkingEditor editor = new MarkingEditor();
    private final MutableLiveData<Resource<Ready>> ready = new MutableLiveData<>();
    // Naik setiap kali isi editor berubah, agar layar menggambar ulang penanda, penghitung, dan tombol.
    private final MutableLiveData<Integer> version = new MutableLiveData<>(0);
    private final MutableLiveData<Event<String>> restoreOffer = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saving = new MutableLiveData<>(false);
    private final MutableLiveData<Event<Integer>> message = new MutableLiveData<>();
    private final MutableLiveData<Event<Runnable>> afterSave = new MutableLiveData<>();

    private String templateId;
    private String page = "index.html";
    private String view = "mobile";
    private int sectionIndex;
    @Nullable
    private Map<String, List<PageScanner.ScannedSection>> scanned;

    @Inject
    public MarkEditorViewModel(UploadRepository repository, MarkingBackupStore backupStore, AppExecutors executors) {
        this.repository = repository;
        this.backupStore = backupStore;
        this.executors = executors;
    }

    LiveData<Resource<Ready>> getReady() {
        return ready;
    }

    LiveData<Integer> getVersion() {
        return version;
    }

    LiveData<Event<String>> getRestoreOffer() {
        return restoreOffer;
    }

    LiveData<Boolean> getSaving() {
        return saving;
    }

    LiveData<Event<Integer>> getMessage() {
        return message;
    }

    LiveData<Event<Runnable>> getAfterSave() {
        return afterSave;
    }

    MarkingEditor editor() {
        return editor;
    }

    String templateId() {
        return templateId;
    }

    void start(String templateId) {
        if (templateId.equals(this.templateId)) {
            return;
        }
        this.templateId = templateId;
        load();
    }

    void load() {
        ready.setValue(Resource.loading());
        String id = templateId;
        executors.networkIO().execute(() -> {
            Resource<DraftDto> draft = repository.draft(id);
            Resource<MarkingDto> marking = repository.marking(id);
            Resource<UploadSettingsDto> settings = repository.settings();
            Resource<File> site = repository.prepareWorkSite(id);
            ApiError error = firstError(draft, marking, settings, site);
            if (error != null) {
                ready.postValue(Resource.error(error));
                return;
            }
            TechInfoDto tech = draft.getData().techInfo;
            List<String> pages = tech == null || tech.pages == null ? Collections.singletonList("index.html") : tech.pages;
            List<TechInfoDto.CssVariableDto> variables = tech == null || tech.cssVariables == null
                    ? Collections.emptyList() : tech.cssVariables;
            String backup = backupStore.load(id);
            executors.mainThread().execute(() -> {
                editor.loadSaved(marking.getData());
                editor.ensurePages(pages);
                editor.markSaved();
                ready.setValue(Resource.success(new Ready(site.getData(), settings.getData().allowedHosts,
                        new ArrayList<>(pages), variables)));
                if (backup != null && !backup.equals(editor.toJson())) {
                    restoreOffer.setValue(new Event<>(backup));
                }
                bump();
            });
        });
    }

    @Nullable
    private static ApiError firstError(Resource<?>... resources) {
        for (Resource<?> resource : resources) {
            if (resource.getStatus() != Resource.Status.SUCCESS) {
                return resource.getError();
            }
        }
        return null;
    }

    // ---------- posisi editor ----------

    String page() {
        return page;
    }

    void setPage(String page) {
        this.page = page;
        sectionIndex = 0;
    }

    String view() {
        return view;
    }

    void setView(String view) {
        this.view = view;
    }

    int sectionIndex() {
        return sectionIndex;
    }

    void setSectionIndex(int index) {
        sectionIndex = Math.max(0, index);
    }

    @Nullable
    Map<String, List<PageScanner.ScannedSection>> scanned() {
        return scanned;
    }

    void setScanned(Map<String, List<PageScanner.ScannedSection>> result) {
        scanned = result;
        // Halaman yang belum dibuka langsung punya daftar section dari hasil pindai.
        for (Map.Entry<String, List<PageScanner.ScannedSection>> entry : result.entrySet()) {
            List<SectionInfo> infos = new ArrayList<>();
            for (PageScanner.ScannedSection section : entry.getValue()) {
                infos.add(section.info);
            }
            editor.applyDetectedSections(entry.getKey(), infos);
        }
        bump();
    }

    // ---------- perubahan ----------

    /** Dipanggil setelah setiap operasi pada {@link #editor()}: gambar ulang + cadangkan ke HP. */
    void changed() {
        bump();
        String id = templateId;
        String json = editor.hasUnsavedChanges() ? editor.toJson() : null;
        executors.diskIO().execute(() -> {
            if (json == null) {
                backupStore.clear(id);
            } else {
                backupStore.save(id, json);
            }
        });
    }

    private void bump() {
        Integer current = version.getValue();
        version.setValue(current == null ? 1 : current + 1);
    }

    void restore(String json) {
        editor.restoreBackup(editor.fromJson(json));
        editor.ensurePages(new ArrayList<>(pagesOfEditor()));
        changed();
    }

    void discardBackup() {
        String id = templateId;
        executors.diskIO().execute(() -> backupStore.clear(id));
    }

    private List<String> pagesOfEditor() {
        List<String> files = new ArrayList<>();
        for (MarkingDto.Page p : editor.data().pages) {
            files.add(p.file);
        }
        return files;
    }

    /**
     * Tombol Simpan (bagian 7.12): cek tandaan, kirim ke server, lalu jalankan {@code then} (mis. buka Coba atau keluar).
     */
    void save(@Nullable Runnable then) {
        String problem = editor.firstProblem();
        if (problem != null) {
            message.setValue(new Event<>("LABEL".equals(problem) ? R.string.mark_error_label : R.string.mark_error_key));
            return;
        }
        saving.setValue(true);
        String id = templateId;
        String json = editor.toJson();
        executors.networkIO().execute(() -> {
            Resource<MarkingDto> result = repository.saveMarking(id, editor.fromJson(json));
            executors.mainThread().execute(() -> {
                saving.setValue(false);
                if (result.getStatus() == Resource.Status.SUCCESS) {
                    // Perubahan baru yang terjadi selama menyimpan tetap dihitung belum disimpan.
                    if (json.equals(editor.toJson())) {
                        editor.markSaved();
                    }
                    message.setValue(new Event<>(R.string.mark_saved));
                    changed();
                    if (then != null) {
                        afterSave.setValue(new Event<>(then));
                    }
                } else {
                    message.setValue(new Event<>(result.getError() != null && result.getError().isNetworkError()
                            ? R.string.error_no_connection : R.string.mark_save_failed));
                }
            });
        });
    }

    /** Catat langkah wizard (4 = Tandai, 5 = Coba) agar "Lanjutkan draft" membuka tepat di langkah itu. */
    void recordStep(int step) {
        String id = templateId;
        executors.networkIO().execute(() -> repository.updateStep(id, step));
    }
}
