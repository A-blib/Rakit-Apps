package com.aris.templateapp.data.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.aris.templateapp.core.network.ApiErrorParser;
import com.aris.templateapp.core.upload.LocalSiteStore;
import com.aris.templateapp.core.upload.ZipQuickCheck;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.remote.api.UploadApi;
import com.aris.templateapp.data.remote.dto.CreateUploadSessionDto;
import com.aris.templateapp.data.remote.dto.DeviceWarningsDto;
import com.aris.templateapp.data.remote.dto.DraftDto;
import com.aris.templateapp.data.remote.dto.DraftInfoDto;
import com.aris.templateapp.data.remote.dto.DraftStepDto;
import com.aris.templateapp.data.remote.dto.HelpArticleDto;
import com.aris.templateapp.data.remote.dto.MarkingDto;
import com.aris.templateapp.data.remote.dto.ReportIssueDto;
import com.aris.templateapp.data.remote.dto.SubmitDto;
import com.aris.templateapp.data.remote.dto.UploadCheckDto;
import com.aris.templateapp.data.remote.dto.UploadOverviewDto;
import com.aris.templateapp.data.remote.dto.UploadSessionDto;
import com.aris.templateapp.data.remote.dto.UploadSettingsDto;
import com.aris.templateapp.data.remote.dto.UploadStartedDto;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/** Fitur Upload provider. Semua method {@code @WorkerThread}: panggil dari thread latar. */
@Singleton
public class UploadRepository {

    /** Kemajuan upload untuk progress bar "12,4 dari 18 MB". */
    public interface UploadListener {
        void onProgress(long sent, long total);

        /** Sinyal putus: app menunggu lalu melanjutkan otomatis dari posisi terakhir di server. */
        void onWaitingForNetwork(int attempt);
    }

    private static final String WORK_PREFIX = "work-";
    private static final MediaType OCTET_STREAM = MediaType.get("application/octet-stream");
    // Percobaan ulang otomatis saat sinyal putus: 2, 4, 8, 16, 32 detik.
    private static final int MAX_RETRIES = 5;
    private static final long FIRST_RETRY_DELAY_MS = 2000;

    private final UploadApi api;
    private final ApiErrorParser errorParser;
    private final ContentResolver resolver;
    private final LocalSiteStore siteStore;
    private UploadSettingsDto settings;

    @Inject
    public UploadRepository(UploadApi api, ApiErrorParser errorParser, @ApplicationContext Context context,
                            LocalSiteStore siteStore) {
        this.api = api;
        this.errorParser = errorParser;
        this.resolver = context.getContentResolver();
        this.siteStore = siteStore;
    }

    @WorkerThread
    public Resource<UploadOverviewDto> overview() {
        return execute(api.overview());
    }

    /** Pengaturan jarang berubah, jadi disimpan selama app hidup. */
    @WorkerThread
    public synchronized Resource<UploadSettingsDto> settings() {
        if (settings != null) {
            return Resource.success(settings);
        }
        Resource<UploadSettingsDto> result = execute(api.settings());
        if (result.getStatus() == Resource.Status.SUCCESS) {
            settings = result.getData();
        }
        return result;
    }

    /**
     * Upload ZIP per potongan (alur-fitur-upload.md bagian 5.7b). Jika sinyal putus, server ditanya sudah menerima
     * sampai byte ke berapa, lalu upload dilanjutkan dari situ, bukan dari awal.
     *
     * @param sessionId sesi lama yang mau dilanjutkan (tombol "Coba lagi"), atau null untuk sesi baru
     * @param sessionOut menerima id sesi begitu dibuat, agar bisa dilanjutkan jika semua percobaan otomatis gagal
     */
    @WorkerThread
    public Resource<UploadStartedDto> upload(Uri uri, String fileName, long size, @Nullable String templateId,
                                             @Nullable String sessionId, String[] sessionOut, UploadListener listener,
                                             AtomicBoolean cancelled) {
        UploadSessionDto session;
        if (sessionId == null) {
            Resource<UploadSessionDto> created = execute(api.createSession(new CreateUploadSessionDto(fileName, size, templateId)));
            if (created.getStatus() != Resource.Status.SUCCESS) {
                return Resource.error(created.getError());
            }
            session = created.getData();
        } else {
            Resource<UploadSessionDto> existing = execute(api.session(sessionId));
            if (existing.getStatus() != Resource.Status.SUCCESS) {
                return Resource.error(existing.getError());
            }
            session = existing.getData();
        }
        sessionOut[0] = session.id;

        long offset = session.receivedSize;
        int attempt = 0;
        byte[] buffer = new byte[session.chunkSize];
        while (offset < size) {
            if (cancelled.get()) {
                return Resource.error(ApiError.of(ApiError.UNKNOWN_ERROR));
            }
            listener.onProgress(offset, size);
            Resource<UploadSessionDto> sent;
            try {
                int length = readChunk(uri, offset, buffer);
                sent = execute(api.appendChunk(session.id, offset,
                        RequestBody.create(buffer, OCTET_STREAM, 0, length)));
            } catch (IOException e) {
                return Resource.error(ApiError.of(ApiError.UNKNOWN_ERROR));
            }
            if (sent.getStatus() == Resource.Status.SUCCESS) {
                offset = sent.getData().receivedSize;
                attempt = 0;
                continue;
            }
            ApiError error = sent.getError();
            boolean offsetMismatch = "UPLOAD_OFFSET_MISMATCH".equals(error.getCode());
            if (!error.isNetworkError() && !offsetMismatch) {
                return Resource.error(error);
            }
            if (error.isNetworkError()) {
                if (++attempt > MAX_RETRIES) {
                    return Resource.error(error);
                }
                listener.onWaitingForNetwork(attempt);
                sleep(FIRST_RETRY_DELAY_MS << (attempt - 1));
            }
            // Tanyakan posisi terakhir yang benar-benar diterima server, lalu lanjutkan dari sana.
            Resource<UploadSessionDto> position = execute(api.session(session.id));
            if (position.getStatus() == Resource.Status.SUCCESS) {
                offset = position.getData().receivedSize;
            }
        }
        listener.onProgress(size, size);
        return execute(api.complete(session.id));
    }

    /** Membaca satu potongan mulai {@code offset}. Stream dibuka ulang agar bisa melompat ke posisi mana pun. */
    private int readChunk(Uri uri, long offset, byte[] buffer) throws IOException {
        try (InputStream in = resolver.openInputStream(uri)) {
            if (in == null) {
                throw new IOException("File tidak bisa dibuka");
            }
            long skipped = 0;
            while (skipped < offset) {
                long n = in.skip(offset - skipped);
                if (n <= 0) {
                    // skip() boleh mengembalikan 0; baca satu byte untuk memastikan belum di ujung file.
                    if (in.read() < 0) {
                        throw new IOException("File berubah saat diupload");
                    }
                    n = 1;
                }
                skipped += n;
            }
            int total = 0;
            int n;
            while (total < buffer.length && (n = in.read(buffer, total, buffer.length - total)) > 0) {
                total += n;
            }
            return total;
        }
    }

    /** Cek kilat ZIP di HP sebelum upload (alur-fitur-upload.md bagian 4). */
    @WorkerThread
    public ZipQuickCheck.Result quickCheck(Uri uri, String fileName, long size, long maxBytes) throws IOException {
        return ZipQuickCheck.check(fileName, size, maxBytes, () -> {
            InputStream in = resolver.openInputStream(uri);
            if (in == null) {
                throw new IOException("File tidak bisa dibuka");
            }
            return in;
        });
    }

    @WorkerThread
    public Resource<UploadCheckDto> check(String templateId) {
        return execute(api.check(templateId));
    }

    @WorkerThread
    public Resource<DraftDto> draft(String templateId) {
        return execute(api.draft(templateId));
    }

    @WorkerThread
    public Resource<DraftDto> updateInfo(String templateId, DraftInfoDto info) {
        return execute(api.updateInfo(templateId, info));
    }

    @WorkerThread
    public Resource<DraftDto> updateStep(String templateId, int step) {
        return execute(api.updateStep(templateId, new DraftStepDto(step)));
    }

    @WorkerThread
    public Resource<DraftDto> updateThumbnail(String templateId, byte[] jpeg, String source, String view) {
        return execute(api.updateThumbnail(templateId, source, view,
                RequestBody.create(jpeg, MediaType.get("image/jpeg"))));
    }

    @WorkerThread
    public Resource<DraftDto> deviceWarnings(String templateId, List<DeviceWarningsDto.Warning> warnings) {
        return execute(api.deviceWarnings(templateId, new DeviceWarningsDto(warnings)));
    }

    /** Hapus draft/upload gagal di server beserta salinan situsnya di HP. */
    @WorkerThread
    public Resource<Boolean> delete(String templateId) {
        Resource<Boolean> result = executeEmpty(api.delete(templateId));
        if (result.getStatus() == Resource.Status.SUCCESS) {
            forgetSite(templateId);
        }
        return result;
    }

    @WorkerThread
    public Resource<Boolean> report(String issueId, @Nullable String reason) {
        return executeEmpty(api.report(issueId, new ReportIssueDto(reason)));
    }

    @WorkerThread
    public Resource<HelpArticleDto> helpArticle(String code) {
        return execute(api.helpArticle(code));
    }

    /**
     * Situs template siap ditampilkan WebView: memakai hasil ekstrak yang sudah ada, atau mengekstrak dari file
     * yang baru dipilih ({@code localZip}), atau mengunduh ZIP dari server (draft dilanjutkan di HP lain).
     */
    @WorkerThread
    public Resource<File> prepareSite(String templateId, @Nullable Uri localZip) {
        File existing = siteStore.siteRoot(templateId);
        if (existing != null) {
            return Resource.success(existing);
        }
        try {
            if (localZip != null) {
                try (InputStream in = resolver.openInputStream(localZip)) {
                    if (in != null) {
                        return Resource.success(siteStore.extract(templateId, in));
                    }
                }
            }
            Response<ResponseBody> response = api.source(templateId).execute();
            ResponseBody body = response.body();
            if (!response.isSuccessful() || body == null) {
                return Resource.error(errorParser.parse(response));
            }
            try (InputStream in = body.byteStream()) {
                return Resource.success(siteStore.extract(templateId, in));
            }
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }

    /** Tandaan tersimpan; data kosong (bukan error) jika belum pernah disimpan. */
    @WorkerThread
    public Resource<MarkingDto> marking(String templateId) {
        try {
            Response<MarkingDto> response = api.marking(templateId).execute();
            if (!response.isSuccessful()) {
                return Resource.error(errorParser.parse(response));
            }
            return Resource.success(response.body() != null ? response.body() : new MarkingDto());
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }

    @WorkerThread
    public Resource<MarkingDto> saveMarking(String templateId, MarkingDto marking) {
        return execute(api.saveMarking(templateId, marking));
    }

    /**
     * Salinan bernomor untuk mode tandai, diunduh ulang setiap kali editor dibuka (ZIP bisa sudah diganti lewat
     * upload perbaikan). Disimpan terpisah dari salinan asli yang dipakai thumbnail.
     */
    @WorkerThread
    public Resource<File> prepareWorkSite(String templateId) {
        try {
            Response<ResponseBody> response = api.workPackage(templateId).execute();
            ResponseBody body = response.body();
            if (!response.isSuccessful() || body == null) {
                return Resource.error(errorParser.parse(response));
            }
            try (InputStream in = body.byteStream()) {
                return Resource.success(siteStore.extract(WORK_PREFIX + templateId, in));
            }
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }

    /** Kirim template (langkah 6); hasilnya dipantau lewat {@link #check(String)}. */
    @WorkerThread
    public Resource<Boolean> submit(String templateId, boolean agreedAssetRights) {
        return executeEmpty(api.submit(templateId, new SubmitDto(agreedAssetRights)));
    }

    /** Salinan situs di HP sudah usang (mis. setelah upload perbaikan lolos). */
    public void forgetSite(String templateId) {
        siteStore.delete(templateId);
        siteStore.delete(WORK_PREFIX + templateId);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private <T> Resource<T> execute(Call<T> call) {
        try {
            Response<T> response = call.execute();
            T body = response.body();
            return response.isSuccessful() && body != null
                    ? Resource.success(body) : Resource.error(errorParser.parse(response));
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }

    private Resource<Boolean> executeEmpty(Call<Void> call) {
        try {
            Response<Void> response = call.execute();
            return response.isSuccessful() ? Resource.success(true) : Resource.error(errorParser.parse(response));
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }
}
