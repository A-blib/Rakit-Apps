package com.aris.templateapp.ui.template;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.template.DownloadHandle;
import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.local.TemplatePackageEntity;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.remote.dto.GalleryTemplateDetailDto;
import com.aris.templateapp.data.repository.TemplateEventRepository;
import com.aris.templateapp.data.repository.TemplatePackageRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Layar Unduh: catat "Dilihat", lewati unduhan jika paket sudah ada, muat detail, minta izin data seluler untuk paket
 * besar, unduh dengan progres, lalu minta layar membuka editor.
 */
@HiltViewModel
public class TemplateDownloadViewModel extends ViewModel {

    /** Paket lebih besar dari ini perlu konfirmasi saat memakai data seluler (bagian 5). */
    static final long MOBILE_CONFIRM_BYTES = 10L * 1024 * 1024;
    /** Progres dikirim ke layar paling sering sekali per ukuran ini, agar thread utama tidak kebanjiran. */
    private static final long PROGRESS_STEP_BYTES = 64L * 1024;

    private final TemplatePackageRepository repository;
    private final TemplateEventRepository events;
    private final AppExecutors executors;
    private final MutableLiveData<DownloadState> state = new MutableLiveData<>();
    private final MutableLiveData<Event<TemplatePackageEntity>> openEditor = new MutableLiveData<>();
    private String templateId;
    private DownloadHandle handle;

    @Inject
    public TemplateDownloadViewModel(TemplatePackageRepository repository, TemplateEventRepository events,
                                     AppExecutors executors) {
        this.repository = repository;
        this.events = events;
        this.executors = executors;
    }

    LiveData<DownloadState> getState() {
        return state;
    }

    /** Paket siap: layar membuka editor dengan project baru yang belum tersimpan. */
    LiveData<Event<TemplatePackageEntity>> getOpenEditor() {
        return openEditor;
    }

    /** @param metered koneksi saat ini berbayar per MB (data seluler) */
    void start(String templateId, boolean metered) {
        if (templateId.equals(this.templateId)) {
            return;
        }
        this.templateId = templateId;
        events.record(templateId, TemplateEventRepository.VIEW, null);
        state.setValue(DownloadState.loading());
        executors.diskIO().execute(() -> {
            TemplatePackageEntity installed = repository.installed(templateId);
            if (installed != null) {
                repository.markUsed(installed);
                openEditor.postValue(new Event<>(installed));
            } else {
                loadDetail(metered);
            }
        });
    }

    void retryDetail(boolean metered) {
        state.setValue(DownloadState.loading());
        executors.networkIO().execute(() -> loadDetail(metered));
    }

    private void loadDetail(boolean metered) {
        Resource<GalleryTemplateDetailDto> result = repository.detail(templateId);
        GalleryTemplateDetailDto detail = result.getData();
        if (result.getStatus() != Resource.Status.SUCCESS || detail == null) {
            state.postValue(DownloadState.detailError(result.getError()));
        } else if (detail.packageSizeBytes == null) {
            state.postValue(DownloadState.failed(detail, ApiError.of(TemplatePackageRepository.PACKAGE_MISSING)));
        } else if (metered && detail.packageSizeBytes > MOBILE_CONFIRM_BYTES) {
            state.postValue(DownloadState.askMobile(detail));
        } else {
            download(detail);
        }
    }

    /** Dialog data seluler dijawab "Lanjutkan", atau tombol Coba lagi setelah gagal. */
    void startDownload() {
        DownloadState current = state.getValue();
        if (current != null && current.detail != null) {
            GalleryTemplateDetailDto detail = current.detail;
            state.setValue(DownloadState.downloading(detail, 0, sizeOf(detail)));
            executors.networkIO().execute(() -> download(detail));
        }
    }

    private void download(GalleryTemplateDetailDto detail) {
        DownloadHandle current = new DownloadHandle();
        handle = current;
        state.postValue(DownloadState.downloading(detail, 0, sizeOf(detail)));
        long[] lastPosted = {0};
        Resource<TemplatePackageEntity> result = repository.download(detail, current, (downloaded, total) -> {
            if (downloaded - lastPosted[0] >= PROGRESS_STEP_BYTES || downloaded == total) {
                lastPosted[0] = downloaded;
                if (downloaded == total) {
                    state.postValue(DownloadState.extracting(detail));
                } else {
                    state.postValue(DownloadState.downloading(detail, downloaded, total));
                }
            }
        });
        if (current.isCancelled()) {
            return;
        }
        if (result.getStatus() == Resource.Status.SUCCESS) {
            openEditor.postValue(new Event<>(result.getData()));
        } else {
            state.postValue(DownloadState.failed(detail, result.getError()));
        }
    }

    /** Batal: hentikan unduhan; file sementara dihapus repository. */
    void cancel() {
        if (handle != null) {
            handle.cancel();
        }
    }

    private static long sizeOf(GalleryTemplateDetailDto detail) {
        return detail.packageSizeBytes == null ? -1 : detail.packageSizeBytes;
    }

    @Override
    protected void onCleared() {
        cancel();
    }
}
