package com.aris.templateapp.data.repository;

import android.os.SystemClock;

import androidx.annotation.WorkerThread;

import com.aris.templateapp.core.network.ApiErrorParser;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.remote.api.ProviderApi;
import com.aris.templateapp.data.remote.dto.FolderRequestDto;
import com.aris.templateapp.data.remote.dto.MoveTemplateDto;
import com.aris.templateapp.data.remote.dto.ProviderDashboardDto;
import com.aris.templateapp.data.remote.dto.ProviderProfileDto;
import com.aris.templateapp.data.remote.dto.ProviderProfileUpdateDto;
import com.aris.templateapp.data.remote.dto.TemplateDetailDto;
import com.aris.templateapp.data.remote.dto.TemplateFolderDto;
import com.aris.templateapp.data.remote.dto.TemplateListDto;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Response;

/** Data Dashboard Provider dari backend. Semua method {@code @WorkerThread}: panggil dari thread latar. */
@Singleton
public class ProviderRepository {

    private final ProviderApi api;
    private final ApiErrorParser errorParser;

    @Inject
    public ProviderRepository(ProviderApi api, ApiErrorParser errorParser) {
        this.api = api;
        this.errorParser = errorParser;
    }

    @WorkerThread
    public Resource<ProviderDashboardDto> dashboard(String period) {
        return execute(api.dashboard(period));
    }

    /**
     * Data Beranda yang diambil lebih dulu oleh layar awal (selagi animasi loading berjalan), supaya Beranda provider
     * langsung tampil tanpa loading kedua. Sekali pakai, dan hanya berlaku sebentar agar tidak menampilkan data basi.
     */
    private static final long PREFETCH_MAX_AGE_MS = 30_000;
    private ProviderDashboardDto prefetched;
    private String prefetchedPeriod;
    private long prefetchedAt;

    @WorkerThread
    public synchronized void prefetchDashboard(String period) {
        Resource<ProviderDashboardDto> result = dashboard(period);
        if (result.getStatus() == Resource.Status.SUCCESS) {
            prefetched = result.getData();
            prefetchedPeriod = period;
            prefetchedAt = SystemClock.elapsedRealtime();
        }
    }

    /** Mengambil (dan menghapus) data hasil prefetch untuk periode itu; null jika tidak ada atau sudah basi. */
    public synchronized ProviderDashboardDto takePrefetchedDashboard(String period) {
        ProviderDashboardDto data = prefetched;
        boolean fresh = data != null && period.equals(prefetchedPeriod)
                && SystemClock.elapsedRealtime() - prefetchedAt < PREFETCH_MAX_AGE_MS;
        prefetched = null;
        return fresh ? data : null;
    }

    @WorkerThread
    public Resource<TemplateListDto> templates(String folder, String status, String category, String sort,
                                               String query, int page) {
        return execute(api.templates(folder, status, category, sort, query, page));
    }

    @WorkerThread
    public Resource<List<TemplateFolderDto>> folders() {
        return execute(api.folders());
    }

    @WorkerThread
    public Resource<TemplateFolderDto> createFolder(String name) {
        return execute(api.createFolder(new FolderRequestDto(name)));
    }

    @WorkerThread
    public Resource<TemplateFolderDto> renameFolder(String id, String name) {
        return execute(api.renameFolder(id, new FolderRequestDto(name)));
    }

    @WorkerThread
    public Resource<Boolean> deleteFolder(String id) {
        return executeNoContent(api.deleteFolder(id));
    }

    /** @param folderId null = keluarkan dari folder */
    @WorkerThread
    public Resource<Boolean> moveTemplate(String templateId, String folderId) {
        return executeNoContent(api.moveTemplate(templateId, new MoveTemplateDto(folderId)));
    }

    @WorkerThread
    public Resource<TemplateDetailDto> template(String id) {
        return execute(api.template(id));
    }

    @WorkerThread
    public Resource<ProviderProfileDto> profile() {
        return execute(api.profile());
    }

    @WorkerThread
    public Resource<ProviderProfileDto> updateProfile(String creatorName, String bio, String portfolioUrl,
                                                      List<String> specialties) {
        return execute(api.updateProfile(new ProviderProfileUpdateDto(creatorName, bio, portfolioUrl, specialties)));
    }

    /** Untuk jawaban 204 tanpa isi: berhasil = {@code Resource.success(true)}. */
    private Resource<Boolean> executeNoContent(Call<Void> call) {
        try {
            Response<Void> response = call.execute();
            return response.isSuccessful() ? Resource.success(true) : Resource.error(errorParser.parse(response));
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
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
}
