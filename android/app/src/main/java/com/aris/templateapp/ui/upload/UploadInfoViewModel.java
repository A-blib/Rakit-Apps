package com.aris.templateapp.ui.upload;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.R;
import com.aris.templateapp.core.network.ThumbnailLoader;
import com.aris.templateapp.core.upload.ThumbnailImages;
import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.remote.dto.DraftDto;
import com.aris.templateapp.data.remote.dto.DraftInfoDto;
import com.aris.templateapp.data.remote.dto.UploadSettingsDto;
import com.aris.templateapp.data.repository.UploadRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Langkah 3 Upload: Info template (alur-fitur-upload.md bagian 6). Setiap isian disimpan otomatis setelah provider
 * berhenti mengetik sebentar; hanya isian yang berubah yang dikirim.
 */
@HiltViewModel
public class UploadInfoViewModel extends ViewModel {

    /** Status simpan otomatis di bawah form. */
    enum SaveStatus { IDLE, SAVING, SAVED, FAILED }

    /** Situs siap dipotret untuk thumbnail. */
    static final class SiteReady {
        final File root;
        final List<String> allowedHosts;

        SiteReady(File root, List<String> allowedHosts) {
            this.root = root;
            this.allowedHosts = allowedHosts;
        }
    }

    // Jeda setelah berhenti mengetik sebelum menyimpan, agar tidak mengirim request di setiap huruf.
    private static final long SAVE_DELAY_MS = 800;

    private final UploadRepository repository;
    private final ThumbnailImages images;
    private final ThumbnailLoader thumbnailLoader;
    private final AppExecutors executors;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<Resource<DraftDto>> draft = new MutableLiveData<>();
    private final MutableLiveData<SaveStatus> saveStatus = new MutableLiveData<>(SaveStatus.IDLE);
    private final MutableLiveData<Boolean> nameDuplicate = new MutableLiveData<>(false);
    private final MutableLiveData<Event<SiteReady>> siteReady = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> thumbnail = new MutableLiveData<>();
    private final MutableLiveData<Boolean> thumbnailBusy = new MutableLiveData<>(false);
    private final MutableLiveData<Event<Integer>> message = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> goNext = new MutableLiveData<>();

    private String templateId;
    private DraftInfoDto pending = new DraftInfoDto();
    private boolean hasPending;
    private final Runnable saveRunnable = this::savePending;

    @Inject
    public UploadInfoViewModel(UploadRepository repository, ThumbnailImages images, ThumbnailLoader thumbnailLoader,
                               AppExecutors executors) {
        this.repository = repository;
        this.images = images;
        this.thumbnailLoader = thumbnailLoader;
        this.executors = executors;
    }

    LiveData<Resource<DraftDto>> getDraft() {
        return draft;
    }

    LiveData<SaveStatus> getSaveStatus() {
        return saveStatus;
    }

    LiveData<Boolean> getNameDuplicate() {
        return nameDuplicate;
    }

    LiveData<Event<SiteReady>> getSiteReady() {
        return siteReady;
    }

    /** Thumbnail yang baru dibuat di HP (sebelum/selagi diupload). */
    LiveData<Bitmap> getThumbnail() {
        return thumbnail;
    }

    LiveData<Boolean> getThumbnailBusy() {
        return thumbnailBusy;
    }

    LiveData<Event<Integer>> getMessage() {
        return message;
    }

    LiveData<Event<Boolean>> getGoNext() {
        return goNext;
    }

    String getTemplateId() {
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
        draft.setValue(Resource.loading());
        String id = templateId;
        executors.networkIO().execute(() -> {
            Resource<DraftDto> result = repository.draft(id);
            draft.postValue(result);
            if (result.getStatus() == Resource.Status.SUCCESS) {
                nameDuplicate.postValue(result.getData().nameDuplicate);
                prepareSite(id);
            }
        });
    }

    private void prepareSite(String id) {
        Resource<UploadSettingsDto> settings = repository.settings();
        Resource<File> site = repository.prepareSite(id, null);
        if (settings.getStatus() == Resource.Status.SUCCESS && site.getStatus() == Resource.Status.SUCCESS) {
            siteReady.postValue(new Event<>(new SiteReady(site.getData(), settings.getData().allowedHosts)));
        }
    }

    // ---------- simpan otomatis ----------

    void setName(String name) {
        pending.name = name.trim();
        schedule();
    }

    void setCategory(@Nullable String category) {
        if (category != null) {
            pending.category = category;
            schedule();
        }
    }

    void setDescription(String description) {
        pending.description = description.trim();
        schedule();
    }

    void setKeywords(List<String> keywords) {
        pending.keywords = new ArrayList<>(keywords);
        schedule();
    }

    private void schedule() {
        hasPending = true;
        handler.removeCallbacks(saveRunnable);
        handler.postDelayed(saveRunnable, SAVE_DELAY_MS);
    }

    /** Menyimpan isian yang belum terkirim sekarang juga (dipanggil sebelum pindah langkah atau keluar). */
    void flush() {
        handler.removeCallbacks(saveRunnable);
        savePending();
    }

    private void savePending() {
        if (!hasPending) {
            return;
        }
        DraftInfoDto info = pending;
        pending = new DraftInfoDto();
        hasPending = false;
        saveStatus.setValue(SaveStatus.SAVING);
        String id = templateId;
        executors.networkIO().execute(() -> {
            Resource<DraftDto> result = repository.updateInfo(id, info);
            if (result.getStatus() == Resource.Status.SUCCESS) {
                saveStatus.postValue(SaveStatus.SAVED);
                nameDuplicate.postValue(result.getData().nameDuplicate);
            } else {
                // Isian yang gagal dikembalikan ke antrean agar ikut terkirim pada simpan berikutnya.
                executors.mainThread().execute(() -> {
                    merge(info);
                    saveStatus.setValue(SaveStatus.FAILED);
                });
            }
        });
    }

    private void merge(DraftInfoDto failed) {
        if (pending.name == null) {
            pending.name = failed.name;
        }
        if (pending.category == null) {
            pending.category = failed.category;
        }
        if (pending.description == null) {
            pending.description = failed.description;
        }
        if (pending.keywords == null) {
            pending.keywords = failed.keywords;
        }
        hasPending = true;
    }

    // ---------- thumbnail ----------

    void setThumbnailBusy(boolean busy) {
        thumbnailBusy.setValue(busy);
    }

    /** Hasil potret WebView (otomatis/section) atau gambar provider; diupload sebagai JPEG. */
    void uploadThumbnail(Bitmap bitmap, String source, String view) {
        thumbnail.setValue(bitmap);
        thumbnailBusy.setValue(true);
        String id = templateId;
        executors.networkIO().execute(() -> {
            byte[] jpeg = ThumbnailImages.toJpeg(bitmap);
            Resource<DraftDto> result = jpeg == null ? null : repository.updateThumbnail(id, jpeg, source, view);
            thumbnailBusy.postValue(false);
            if (result != null && result.getStatus() == Resource.Status.SUCCESS) {
                if (result.getData().thumbnailUrl != null) {
                    thumbnailLoader.put(result.getData().thumbnailUrl, bitmap);
                }
            } else {
                message.postValue(new Event<>(jpeg == null ? R.string.upload_thumbnail_too_large
                        : R.string.upload_thumbnail_failed));
            }
        });
    }

    void customThumbnail(Uri uri, String view) {
        thumbnailBusy.setValue(true);
        executors.diskIO().execute(() -> {
            Bitmap bitmap = images.fromUri(uri);
            if (bitmap == null) {
                thumbnailBusy.postValue(false);
                message.postValue(new Event<>(R.string.upload_thumbnail_failed));
                return;
            }
            executors.mainThread().execute(() -> uploadThumbnail(bitmap, "custom", view));
        });
    }

    // ---------- lanjut ----------

    /** Simpan isian terakhir, catat langkah 4, lalu pindah ke Tandai bagian. */
    void next() {
        flush();
        String id = templateId;
        executors.networkIO().execute(() -> {
            Resource<DraftDto> result = repository.updateStep(id, 4);
            if (result.getStatus() == Resource.Status.SUCCESS) {
                goNext.postValue(new Event<>(true));
            } else {
                message.postValue(new Event<>(R.string.upload_save_failed));
            }
        });
    }

    @Override
    protected void onCleared() {
        // Isian terakhir tetap disimpan walau layar ditutup sebelum jeda selesai.
        handler.removeCallbacks(saveRunnable);
        savePending();
    }
}
