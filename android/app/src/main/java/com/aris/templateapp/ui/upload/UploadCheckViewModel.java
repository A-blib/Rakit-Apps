package com.aris.templateapp.ui.upload;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.R;
import com.aris.templateapp.core.upload.ZipQuickCheck;
import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.remote.dto.DeviceWarningsDto;
import com.aris.templateapp.data.remote.dto.UploadCheckDto;
import com.aris.templateapp.data.remote.dto.UploadSettingsDto;
import com.aris.templateapp.data.remote.dto.UploadStartedDto;
import com.aris.templateapp.data.repository.UploadRepository;
import com.aris.templateapp.ui.upload.UploadCheckState.Phase;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Langkah 2 Upload: cek kilat di HP → upload per potongan → memantau tahap pengecekan server → tahap C di HP → hasil.
 * <p>
 * Tahap C butuh WebView (sebuah View), jadi ViewModel hanya meminta lewat {@link #getDeviceCheckRequest()};
 * Fragment yang menjalankannya lalu mengembalikan hasilnya ke {@link #onDeviceChecked(List)}.
 */
@HiltViewModel
public class UploadCheckViewModel extends ViewModel {

    /** Bahan tahap C untuk Fragment. */
    static final class DeviceCheckRequest {
        final File siteRoot;
        final List<String> allowedHosts;
        final List<String> pages;

        DeviceCheckRequest(File siteRoot, List<String> allowedHosts, List<String> pages) {
            this.siteRoot = siteRoot;
            this.allowedHosts = allowedHosts;
            this.pages = pages;
        }
    }

    private static final long POLL_MS = 700;
    // Peringatan data seluler untuk file di atas 10 MB (bagian 5.7b).
    static final long MOBILE_DATA_WARN_BYTES = 10L * 1024 * 1024;

    private final UploadRepository repository;
    private final AppExecutors executors;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private final MutableLiveData<UploadCheckState> state = new MutableLiveData<>();
    private final MutableLiveData<Event<DeviceCheckRequest>> deviceCheckRequest = new MutableLiveData<>();
    private final MutableLiveData<Event<Integer>> message = new MutableLiveData<>();

    private boolean started;
    @Nullable
    private Uri uri;
    private String fileName;
    private long fileSize;
    private long maxZipBytes;
    @Nullable
    private String fixTemplateId;
    @Nullable
    private String templateId;
    @Nullable
    private String sessionId;
    // Tahap C hanya dijalankan jika pengecekan server selesai saat layar ini terbuka (bukan saat membuka hasil lama).
    private boolean sawChecking;
    // Pengecekan baru saja lolos di layar ini; tanpa peringatan, layar langsung lanjut ke Info template.
    private boolean autoContinue;

    @Inject
    public UploadCheckViewModel(UploadRepository repository, AppExecutors executors) {
        this.repository = repository;
        this.executors = executors;
    }

    LiveData<UploadCheckState> getState() {
        return state;
    }

    LiveData<Event<DeviceCheckRequest>> getDeviceCheckRequest() {
        return deviceCheckRequest;
    }

    /** Pesan singkat (Snackbar), mis. "Terima kasih, laporanmu kami tinjau." */
    LiveData<Event<Integer>> getMessage() {
        return message;
    }

    @Nullable
    String getTemplateId() {
        return templateId != null ? templateId : fixTemplateId;
    }

    String getFileName() {
        return fileName;
    }

    long getFileSize() {
        return fileSize;
    }

    long getMaxZipBytes() {
        return maxZipBytes;
    }

    /** Mulai dari file yang baru dipilih. {@code metered} = HP memakai data seluler. */
    void startUpload(Uri uri, String fileName, long fileSize, @Nullable String fixTemplateId, boolean metered) {
        if (started) {
            return;
        }
        started = true;
        this.uri = uri;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fixTemplateId = fixTemplateId;
        state.setValue(UploadCheckState.of(Phase.LOCAL_CHECKING));
        executors.networkIO().execute(() -> {
            Resource<UploadSettingsDto> settings = repository.settings();
            if (settings.getStatus() != Resource.Status.SUCCESS) {
                state.postValue(UploadCheckState.error(Phase.UPLOAD_ERROR, settings.getError()));
                return;
            }
            maxZipBytes = settings.getData().maxZipBytes;
            ZipQuickCheck.Result local;
            try {
                local = repository.quickCheck(uri, fileName, fileSize, settings.getData().maxZipBytes);
            } catch (IOException e) {
                local = null;
            }
            if (local == null) {
                state.postValue(UploadCheckState.localFailed(null));
            } else if (!local.passed()) {
                state.postValue(UploadCheckState.localFailed(local));
            } else if (metered && fileSize > MOBILE_DATA_WARN_BYTES) {
                state.postValue(UploadCheckState.of(Phase.CONFIRM_MOBILE_DATA));
            } else {
                upload();
            }
        });
    }

    /** Membuka hasil/progres upload yang sudah ada (dari kartu di tab Upload). */
    void monitor(String templateId) {
        if (started) {
            return;
        }
        started = true;
        this.templateId = templateId;
        state.setValue(UploadCheckState.checking(null, null));
        poll();
    }

    void confirmMobileData() {
        executors.networkIO().execute(this::upload);
    }

    /** "Coba lagi" setelah upload berhenti: lanjut dari posisi terakhir di server jika sesinya masih ada. */
    void retry() {
        UploadCheckState current = state.getValue();
        if (current != null && current.phase == Phase.LOAD_ERROR) {
            state.setValue(UploadCheckState.checking(null, null));
            poll();
            return;
        }
        executors.networkIO().execute(this::upload);
    }

    private void upload() {
        if (uri == null) {
            return;
        }
        String[] sessionOut = {sessionId};
        state.postValue(UploadCheckState.uploading(0, fileSize, 0));
        Resource<UploadStartedDto> result = repository.upload(uri, fileName, fileSize, fixTemplateId, sessionId,
                sessionOut, new UploadRepository.UploadListener() {
                    private long sent;

                    @Override
                    public void onProgress(long sent, long total) {
                        this.sent = sent;
                        state.postValue(UploadCheckState.uploading(sent, total, 0));
                    }

                    @Override
                    public void onWaitingForNetwork(int attempt) {
                        state.postValue(UploadCheckState.uploading(sent, fileSize, attempt));
                    }
                }, cancelled);
        sessionId = sessionOut[0];
        if (cancelled.get()) {
            return;
        }
        if (result.getStatus() != Resource.Status.SUCCESS) {
            // Sesi tetap disimpan: "Coba lagi" melanjutkan dari posisi terakhir, bukan dari awal.
            state.postValue(UploadCheckState.error(Phase.UPLOAD_ERROR, result.getError()));
            return;
        }
        templateId = result.getData().templateId;
        sessionId = null;
        sawChecking = true;
        state.postValue(UploadCheckState.checking("uploaded", null));
        handler.post(this::poll);
    }

    private void poll() {
        String id = templateId;
        if (id == null || cancelled.get()) {
            return;
        }
        executors.networkIO().execute(() -> {
            Resource<UploadCheckDto> result = repository.check(id);
            if (cancelled.get()) {
                return;
            }
            if (result.getStatus() != Resource.Status.SUCCESS) {
                state.postValue(UploadCheckState.error(Phase.LOAD_ERROR, result.getError()));
                return;
            }
            UploadCheckDto check = result.getData();
            if ("checking".equals(check.status)) {
                sawChecking = true;
                state.postValue(UploadCheckState.checking(check.check == null ? null : check.check.stage, check));
                handler.postDelayed(this::poll, POLL_MS);
            } else if ("check_failed".equals(check.status)) {
                state.postValue(UploadCheckState.result(Phase.FAILED, check));
            } else if (sawChecking && check.techInfo != null && check.techInfo.pages != null) {
                sawChecking = false;
                startDeviceCheck(check);
            } else {
                state.postValue(UploadCheckState.result(Phase.PASSED, check));
            }
        });
    }

    /** Tahap C: siapkan situs di HP, lalu minta Fragment menjalankan halaman di WebView. */
    private void startDeviceCheck(UploadCheckDto check) {
        state.postValue(UploadCheckState.result(Phase.DEVICE_CHECK, check));
        // ZIP baru (termasuk upload perbaikan): salinan situs lama di HP tidak berlaku lagi.
        repository.forgetSite(check.templateId);
        Resource<File> site = repository.prepareSite(check.templateId, uri);
        Resource<UploadSettingsDto> settings = repository.settings();
        if (site.getStatus() != Resource.Status.SUCCESS || settings.getStatus() != Resource.Status.SUCCESS) {
            // Tahap C hanya pembantu; jika situs tidak bisa disiapkan, hasil server tetap berlaku.
            state.postValue(UploadCheckState.result(Phase.PASSED, check));
            return;
        }
        deviceCheckRequest.postValue(new Event<>(new DeviceCheckRequest(site.getData(),
                settings.getData().allowedHosts, check.techInfo.pages)));
    }

    void onDeviceChecked(List<DeviceWarningsDto.Warning> warnings) {
        String id = templateId;
        if (id == null) {
            return;
        }
        executors.networkIO().execute(() -> {
            repository.deviceWarnings(id, warnings);
            Resource<UploadCheckDto> result = repository.check(id);
            if (result.getStatus() == Resource.Status.SUCCESS) {
                autoContinue = true;
                state.postValue(UploadCheckState.result(Phase.PASSED, result.getData()));
            } else {
                state.postValue(UploadCheckState.error(Phase.LOAD_ERROR, result.getError()));
            }
        });
    }

    /** true sekali saja setelah pengecekan baru lolos, agar membuka hasil lama tidak langsung berpindah layar. */
    boolean consumeAutoContinue() {
        boolean value = autoContinue;
        autoContinue = false;
        return value;
    }

    void report(String issueId, @Nullable String reason) {
        executors.networkIO().execute(() -> {
            Resource<Boolean> result = repository.report(issueId, reason);
            message.postValue(new Event<>(result.getStatus() == Resource.Status.SUCCESS
                    ? R.string.report_thanks : R.string.error_unknown));
            String id = templateId != null ? templateId : fixTemplateId;
            if (id != null) {
                Resource<UploadCheckDto> refreshed = repository.check(id);
                if (refreshed.getStatus() == Resource.Status.SUCCESS) {
                    UploadCheckDto dto = refreshed.getData();
                    state.postValue(UploadCheckState.result("check_failed".equals(dto.status) ? Phase.FAILED : Phase.PASSED, dto));
                }
            }
        });
    }

    /** Membatalkan upload yang sedang berjalan (✕ saat upload, atau layar ditutup). */
    void cancel() {
        cancelled.set(true);
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onCleared() {
        cancel();
    }
}
