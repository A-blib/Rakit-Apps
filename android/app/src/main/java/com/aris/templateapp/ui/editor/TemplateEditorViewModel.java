package com.aris.templateapp.ui.editor;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.template.AndroidImageCompressor;
import com.aris.templateapp.core.template.CompletenessChecker;
import com.aris.templateapp.core.template.ProjectExporter;
import com.aris.templateapp.core.template.ProjectStore;
import com.aris.templateapp.core.template.TemplateFiles;
import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.local.ProjectEntity;
import com.aris.templateapp.data.local.TemplatePackageEntity;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.ProjectMode;
import com.aris.templateapp.data.model.ProjectStatus;
import com.aris.templateapp.data.model.ProjectValues;
import com.aris.templateapp.data.model.TemplateManifest;
import com.aris.templateapp.data.repository.ProjectRepository;
import com.aris.templateapp.data.repository.TemplateEventRepository;
import com.aris.templateapp.data.repository.TemplatePackageRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Otak editor template mode (alur-buat-website-via-template.md bagian 6). Menyimpan nilai isian di memori, riwayat
 * undo/redo, dan status kelengkapan; menyimpan ke HP otomatis (autosave) 500 ms setelah perubahan terakhir.
 * <p>
 * Project baru baru dibuat di database saat perubahan pertama (bagian 3): membuka editor lalu kembali tanpa mengubah
 * apa pun tidak meninggalkan project kosong.
 */
@HiltViewModel
public class TemplateEditorViewModel extends ViewModel {

    static final long SAVE_DELAY_MS = 500;
    static final int MAX_UNDO = 50;
    /** Ketikan beruntun di isian yang sama dalam jeda ini digabung menjadi satu langkah undo. */
    static final long TYPING_MERGE_MS = 1500;
    private static final int THUMBNAIL_WIDTH = 480;

    /** Data yang tidak berubah selama editor terbuka. */
    static final class Loaded {
        final TemplateManifest manifest;
        final File packageDir;
        final String templateId;
        final int templateVersion;

        Loaded(TemplateManifest manifest, File packageDir, String templateId, int templateVersion) {
            this.manifest = manifest;
            this.packageDir = packageDir;
            this.templateId = templateId;
            this.templateVersion = templateVersion;
        }
    }

    enum SaveState { IDLE, SAVING, SAVED, FAILED }

    /** Bagian app bar & chip yang berubah setiap kali isi diubah. */
    static final class Header {
        final String name;
        final ProjectStatus status;
        final boolean changedSinceExport;
        final SaveState save;
        final boolean canUndo;
        final boolean canRedo;
        final CompletenessChecker.Result completeness;

        Header(String name, ProjectStatus status, boolean changedSinceExport, SaveState save, boolean canUndo,
               boolean canRedo, CompletenessChecker.Result completeness) {
            this.name = name;
            this.status = status;
            this.changedSinceExport = changedSinceExport;
            this.save = save;
            this.canUndo = canUndo;
            this.canRedo = canRedo;
            this.completeness = completeness;
        }
    }

    /** Perubahan untuk preview: satu isian, atau null = semua (setelah undo/redo/reset). */
    static final class Change {
        @Nullable
        final String key;
        final boolean styles;

        Change(@Nullable String key, boolean styles) {
            this.key = key;
            this.styles = styles;
        }
    }

    private final ProjectRepository projects;
    private final TemplatePackageRepository packages;
    private final ProjectStore store;
    private final TemplateEventRepository events;
    private final AppExecutors executors;
    private final File exportDir;
    private final Handler main = new Handler(Looper.getMainLooper());

    private final MutableLiveData<Resource<Loaded>> loaded = new MutableLiveData<>();
    private final MutableLiveData<Header> header = new MutableLiveData<>();
    private final MutableLiveData<Event<Change>> changes = new MutableLiveData<>();
    private final MutableLiveData<Event<Integer>> messages = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> closed = new MutableLiveData<>();
    private final MutableLiveData<ExportState> export = new MutableLiveData<>(ExportState.idle());

    private Loaded data;
    private ProjectValues values = new ProjectValues();
    private final Deque<ProjectValues> undo = new ArrayDeque<>();
    private final Deque<ProjectValues> redo = new ArrayDeque<>();
    @Nullable
    private String lastTypingKey;
    private long lastTypingAt;

    /** null sampai project dibuat (perubahan pertama). Hanya diubah di thread utama. */
    @Nullable
    private String projectId;
    private String projectName;
    @Nullable
    private Long lastExportedAt;
    private long updatedAt;
    private SaveState saveState = SaveState.IDLE;
    private boolean started;
    private boolean saveRetried;
    /** Ada perubahan yang menunggu disimpan (autosave belum berjalan). */
    private boolean savePending;
    private final Runnable saveRunnable = this::saveNow;

    @Inject
    public TemplateEditorViewModel(ProjectRepository projects, TemplatePackageRepository packages, ProjectStore store,
                                   TemplateEventRepository events, AppExecutors executors,
                                   @ApplicationContext Context context) {
        this.projects = projects;
        this.packages = packages;
        this.store = store;
        this.events = events;
        this.executors = executors;
        // Hanya folder ini yang dibagikan FileProvider (res/xml/file_paths.xml).
        this.exportDir = new File(context.getCacheDir(), "exports");
    }

    LiveData<Resource<Loaded>> getLoaded() {
        return loaded;
    }

    LiveData<Header> getHeader() {
        return header;
    }

    LiveData<Event<Change>> getChanges() {
        return changes;
    }

    /** Pesan snackbar (id string). */
    LiveData<Event<Integer>> getMessages() {
        return messages;
    }

    /** Project dihapus: layar ditutup. */
    LiveData<Event<Boolean>> getClosed() {
        return closed;
    }

    // ---------- memuat ----------

    /**
     * @param projectId       project tersimpan, atau null untuk project baru dari paket
     * @param templateId      paket untuk project baru
     * @param templateVersion versi paket untuk project baru
     */
    void start(@Nullable String projectId, @Nullable String templateId, int templateVersion, @Nullable String title) {
        if (started) {
            return;
        }
        started = true;
        loaded.setValue(Resource.loading());
        executors.diskIO().execute(() -> {
            if (projectId != null) {
                loadProject(projectId);
            } else {
                loadNew(templateId, templateVersion, title);
            }
        });
    }

    @WorkerThread
    private void loadProject(String id) {
        ProjectEntity project = projects.findSync(id);
        if (project == null || project.sourceTemplateId == null) {
            loaded.postValue(Resource.error(ApiError.of(EditorErrors.PROJECT_NOT_FOUND)));
            return;
        }
        TemplatePackageEntity pkg = project.templateVersion == null ? packages.installed(project.sourceTemplateId)
                : packages.find(project.sourceTemplateId, project.templateVersion);
        TemplateManifest manifest = pkg == null ? null : packages.manifest(pkg);
        if (manifest == null) {
            loaded.postValue(Resource.error(ApiError.of(EditorErrors.PACKAGE_MISSING)));
            return;
        }
        packages.markUsed(pkg);
        ProjectValues stored = store.read(id);
        main.post(() -> {
            projectId = project.id;
            projectName = project.name;
            lastExportedAt = project.lastExportedAt;
            updatedAt = project.updatedAt;
            values = stored;
            saveState = SaveState.SAVED;
            finishLoad(new Loaded(manifest, new File(pkg.path), pkg.templateId, pkg.version));
        });
    }

    @WorkerThread
    private void loadNew(@Nullable String templateId, int version, @Nullable String title) {
        TemplatePackageEntity pkg = templateId == null ? null : packages.find(templateId, version);
        TemplateManifest manifest = pkg == null ? null : packages.manifest(pkg);
        if (manifest == null) {
            loaded.postValue(Resource.error(ApiError.of(EditorErrors.PACKAGE_MISSING)));
            return;
        }
        main.post(() -> {
            projectName = title != null ? title : pkg.name;
            finishLoad(new Loaded(manifest, new File(pkg.path), pkg.templateId, pkg.version));
        });
    }

    private void finishLoad(Loaded result) {
        data = result;
        loaded.setValue(Resource.success(result));
        publishHeader();
    }

    // ---------- membaca nilai (untuk form & preview) ----------

    ProjectValues values() {
        return values;
    }

    @Nullable
    String projectId() {
        return projectId;
    }

    /** Folder project untuk gambar; dipakai preview walau project belum dibuat (foldernya belum ada). */
    File projectDir() {
        return store.dir(projectId != null ? projectId : pendingId());
    }

    // ID project baru dipesan sejak awal agar gambar yang dipilih sebelum project tersimpan masuk ke folder yang benar.
    @Nullable
    private String reservedId;

    String pendingId() {
        if (projectId != null) {
            return projectId;
        }
        if (reservedId == null) {
            reservedId = UUID.randomUUID().toString();
        }
        return reservedId;
    }

    @Nullable
    CompletenessChecker.Result completeness() {
        Header current = header.getValue();
        return current == null ? null : current.completeness;
    }

    // ---------- mengubah nilai ----------

    void setText(String key, String text) {
        ProjectValues.FieldValue current = values.peek(key);
        if (current != null && Objects.equals(current.text, text)) {
            return;
        }
        recordTyping(key);
        values.field(key).text = text;
        changed(key, false);
    }

    void setHref(String key, @Nullable String href) {
        ProjectValues.FieldValue current = values.peek(key);
        if (current != null && Objects.equals(current.href, href)) {
            return;
        }
        recordTyping(key + "#href");
        values.field(key).href = href;
        changed(key, false);
    }

    /** @param projectRelativePath mis. "images/foto_hero-1.webp"; null = kembali ke foto contoh */
    void setImage(String key, @Nullable String projectRelativePath) {
        pushUndo();
        values.field(key).image = projectRelativePath;
        values.compact();
        changed(key, false);
    }

    /** @param value null = kembali ke gaya asli template */
    void setStyle(String key, String prop, @Nullable String value) {
        Map<String, String> styles = values.style(key);
        if (Objects.equals(styles.get(prop), value)) {
            return;
        }
        recordTyping(key + "@" + prop);
        if (value == null) {
            styles.remove(prop);
        } else {
            styles.put(prop, value);
        }
        values.compact();
        changed(key, true);
    }

    void setTheme(String variable, @Nullable String value) {
        if (Objects.equals(values.theme.get(variable), value)) {
            return;
        }
        recordTyping("theme" + variable);
        if (value == null) {
            values.theme.remove(variable);
        } else {
            values.theme.put(variable, value);
        }
        changed(null, true);
    }

    /** Menu ⋮ → Kembalikan ke isi template: semua isian, gaya, dan tema dikosongkan (bisa di-undo). */
    void resetToTemplate() {
        pushUndo();
        values = new ProjectValues();
        changed(null, true);
    }

    void undo() {
        if (undo.isEmpty()) {
            return;
        }
        redo.push(values.copy());
        values = undo.pop();
        lastTypingKey = null;
        changed(null, true);
    }

    void redo() {
        if (redo.isEmpty()) {
            return;
        }
        undo.push(values.copy());
        values = redo.pop();
        lastTypingKey = null;
        changed(null, true);
    }

    private void recordTyping(String key) {
        long now = System.currentTimeMillis();
        if (!key.equals(lastTypingKey) || now - lastTypingAt > TYPING_MERGE_MS) {
            pushUndo();
        }
        lastTypingKey = key;
        lastTypingAt = now;
    }

    private void pushUndo() {
        undo.push(values.copy());
        while (undo.size() > MAX_UNDO) {
            undo.removeLast();
        }
        redo.clear();
        lastTypingKey = null;
    }

    private void changed(@Nullable String key, boolean styles) {
        changes.setValue(new Event<>(new Change(key, styles)));
        main.removeCallbacks(saveRunnable);
        main.postDelayed(saveRunnable, SAVE_DELAY_MS);
        savePending = true;
        saveState = SaveState.SAVING;
        publishHeader();
    }

    // ---------- menyimpan ----------

    /** Menyimpan sekarang juga (dipanggil saat editor ditutup) jika ada perubahan yang belum tersimpan. */
    void flush() {
        if (savePending) {
            main.removeCallbacks(saveRunnable);
            saveNow();
        }
    }

    private void saveNow() {
        savePending = false;
        if (data == null) {
            return;
        }
        ProjectValues snapshot = values.copy();
        CompletenessChecker.Result result = CompletenessChecker.check(data.manifest, snapshot);
        // ID project baru sudah dipesan (pendingId), jadi simpanan beruntun selalu menuju project yang sama. Thread
        // disk hanya satu, sehingga simpanan kedua pasti melihat baris yang dibuat simpanan pertama.
        String id = pendingId();
        String name = projectName;
        Long exported = lastExportedAt;
        executors.diskIO().execute(() -> {
            try {
                long now = projects.now();
                ProjectStatus status = CompletenessChecker.status(result.missingCount(), exported);
                store.write(id, snapshot);
                if (projects.findSync(id) == null) {
                    String uniqueName = projects.uniqueName(name);
                    ProjectEntity project = new ProjectEntity(id, uniqueName, ProjectMode.TEMPLATE, status, now, now);
                    project.sourceTemplateId = data.templateId;
                    project.templateVersion = data.templateVersion;
                    project.missingCount = result.missingCount();
                    project.contentPath = store.dir(id).getAbsolutePath();
                    projects.insertSync(project);
                    main.post(() -> {
                        projectId = id;
                        projectName = uniqueName;
                    });
                } else {
                    projects.updateContent(id, status, result.missingCount(), now);
                }
                main.post(() -> {
                    updatedAt = now;
                    saveRetried = false;
                    if (!savePending) {
                        saveState = SaveState.SAVED;
                    }
                    publishHeader();
                });
            } catch (IOException e) {
                main.post(this::onSaveFailed);
            }
        });
    }

    /** Gagal simpan (bagian 6.7): snackbar + coba ulang otomatis sekali. */
    private void onSaveFailed() {
        saveState = SaveState.FAILED;
        publishHeader();
        messages.setValue(new Event<>(com.aris.templateapp.R.string.editor_save_failed));
        if (!saveRetried) {
            saveRetried = true;
            savePending = true;
            main.postDelayed(saveRunnable, SAVE_DELAY_MS * 4);
        }
    }

    void retrySave() {
        main.removeCallbacks(saveRunnable);
        saveNow();
    }

    void rename(String newName) {
        String clean = newName.trim();
        if (clean.isEmpty() || clean.equals(projectName)) {
            return;
        }
        projectName = clean;
        publishHeader();
        String id = projectId;
        if (id != null) {
            projects.rename(id, clean);
        }
    }

    /** Menu ⋮ → Hapus project. Project yang belum tersimpan cukup ditutup. */
    void deleteProject() {
        main.removeCallbacks(saveRunnable);
        savePending = false;
        String id = projectId != null ? projectId : reservedId;
        if (id != null) {
            // Lewat thread disk yang sama dengan autosave, jadi simpanan yang sedang berjalan selesai dulu.
            projects.delete(id);
        }
        projectId = null;
        closed.setValue(new Event<>(true));
    }

    /** Thumbnail project (bagian 6.7): bagian atas preview halaman utama, diperkecil, WebP. */
    void saveThumbnail(Bitmap bitmap) {
        String id = projectId;
        if (id == null) {
            bitmap.recycle();
            return;
        }
        executors.diskIO().execute(() -> {
            File file = store.thumbnail(id);
            int height = Math.round(bitmap.getHeight() * (THUMBNAIL_WIDTH / (float) bitmap.getWidth()));
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, THUMBNAIL_WIDTH, Math.max(1, height), true);
            try (OutputStream out = new FileOutputStream(file)) {
                scaled.compress(com.aris.templateapp.core.template.ImageProcessor.webp(), 80, out);
                projects.updateThumbnail(id, file.getAbsolutePath());
            } catch (IOException ignored) {
                // Thumbnail hanya hiasan; gagal menyimpannya tidak mengganggu project.
            } finally {
                if (scaled != bitmap) {
                    scaled.recycle();
                }
                bitmap.recycle();
            }
        });
    }

    private void publishHeader() {
        if (data == null) {
            return;
        }
        CompletenessChecker.Result result = CompletenessChecker.check(data.manifest, values);
        ProjectStatus status = CompletenessChecker.status(result.missingCount(), lastExportedAt);
        boolean changedSinceExport = lastExportedAt != null && (updatedAt > lastExportedAt || saveState == SaveState.SAVING);
        header.setValue(new Header(projectName, status, changedSinceExport, saveState, !undo.isEmpty(), !redo.isEmpty(),
                result));
    }

    // ---------- export (bagian 8) ----------

    enum ExportPhase { IDLE, BUILDING, READY, FAILED }

    /** Keadaan pembuatan ZIP untuk layar Export. */
    static final class ExportState {
        final ExportPhase phase;
        final int done;
        final int total;
        @Nullable
        final File zip;
        final long bytes;

        private ExportState(ExportPhase phase, int done, int total, @Nullable File zip, long bytes) {
            this.phase = phase;
            this.done = done;
            this.total = total;
            this.zip = zip;
            this.bytes = bytes;
        }

        static ExportState idle() {
            return new ExportState(ExportPhase.IDLE, 0, 0, null, 0);
        }
    }

    LiveData<ExportState> getExport() {
        return export;
    }

    void resetExport() {
        export.setValue(ExportState.idle());
    }

    /**
     * Membuat ZIP di cache (bagian 8.1). Project yang belum pernah disimpan dibuat dulu, karena status "Diexport"
     * dan event download butuh project sungguhan. Berjalan di thread disk yang sama dengan autosave, jadi simpanan
     * terakhir pasti sudah tertulis sebelum ZIP dibuat.
     */
    void buildExport(String fileName, boolean compressPhotos) {
        if (data == null) {
            return;
        }
        main.removeCallbacks(saveRunnable);
        savePending = true;
        saveNow();
        ProjectValues snapshot = values.copy();
        Loaded loadedData = data;
        File projectDir = projectDir();
        export.setValue(new ExportState(ExportPhase.BUILDING, 0, 1, null, 0));
        executors.diskIO().execute(() -> {
            TemplateFiles.deleteRecursively(exportDir);
            File zip = new File(exportDir, fileName);
            try {
                if (!exportDir.mkdirs() && !exportDir.isDirectory()) {
                    throw new IOException("Folder export tidak bisa dibuat");
                }
                ProjectExporter.Result result;
                try (OutputStream out = new FileOutputStream(zip)) {
                    result = ProjectExporter.export(loadedData.packageDir, projectDir, loadedData.manifest, snapshot,
                            compressPhotos ? new AndroidImageCompressor() : null, out,
                            (done, total) -> export.postValue(new ExportState(ExportPhase.BUILDING, done, total, null, 0)));
                }
                export.postValue(new ExportState(ExportPhase.READY, 1, 1, zip, result.bytes));
            } catch (IOException | RuntimeException e) {
                //noinspection ResultOfMethodCallIgnored
                zip.delete();
                export.postValue(new ExportState(ExportPhase.FAILED, 0, 0, null, 0));
            }
        });
    }

    /**
     * ZIP sudah disimpan/dibagikan (bagian 8.3): status menjadi Diexport. Event "download" hanya untuk export
     * pertama project berbasis template provider; antrean memastikan tetap terkirim walau sedang offline.
     */
    void onExportDelivered() {
        String id = pendingId();
        boolean first = lastExportedAt == null;
        long now = projects.now();
        lastExportedAt = now;
        updatedAt = now;
        publishHeader();
        executors.diskIO().execute(() -> projects.markExported(id, now));
        if (first && data != null) {
            events.record(data.templateId, TemplateEventRepository.DOWNLOAD, id);
        }
    }

    /** Gambar yang masih dipakai nilai sekarang atau riwayat undo/redo; file lain boleh dihapus (bagian 6.6). */
    java.util.Set<String> imagesInUse() {
        java.util.Set<String> used = new java.util.HashSet<>();
        collectImages(values, used);
        for (ProjectValues snapshot : undo) {
            collectImages(snapshot, used);
        }
        for (ProjectValues snapshot : redo) {
            collectImages(snapshot, used);
        }
        return used;
    }

    private static void collectImages(ProjectValues snapshot, java.util.Set<String> used) {
        for (ProjectValues.FieldValue value : snapshot.fields.values()) {
            if (value.image != null) {
                used.add(value.image);
            }
        }
    }

    @Nullable
    Loaded data() {
        return data;
    }

    String projectName() {
        return projectName;
    }

    @Nullable
    Long lastExportedAt() {
        return lastExportedAt;
    }

    @Override
    protected void onCleared() {
        flush();
        main.removeCallbacksAndMessages(null);
    }
}
