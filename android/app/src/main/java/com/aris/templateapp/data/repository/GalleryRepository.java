package com.aris.templateapp.data.repository;

import androidx.annotation.WorkerThread;

import com.aris.templateapp.core.network.ApiErrorParser;
import com.aris.templateapp.core.storage.SessionStore;
import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.remote.api.GalleryApi;
import com.aris.templateapp.data.remote.dto.GalleryPageDto;
import com.aris.templateapp.data.remote.dto.TemplateEventDto;

import java.io.IOException;
import java.time.Instant;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Response;

/** Galeri Template dari backend (butuh internet) + pencatatan "Dilihat". */
@Singleton
public class GalleryRepository {

    public static final String SORT_POPULAR = "popular";
    public static final String SORT_NEWEST = "newest";

    private final GalleryApi api;
    private final ApiErrorParser errorParser;
    private final SessionStore sessionStore;
    private final AppExecutors executors;

    @Inject
    public GalleryRepository(GalleryApi api, ApiErrorParser errorParser, SessionStore sessionStore,
                             AppExecutors executors) {
        this.api = api;
        this.errorParser = errorParser;
        this.sessionStore = sessionStore;
        this.executors = executors;
    }

    @WorkerThread
    public Resource<GalleryPageDto> templates(String category, String query, String sort, int page, int size) {
        try {
            Response<GalleryPageDto> response = api.templates(category, query, sort, page, size).execute();
            GalleryPageDto body = response.body();
            return response.isSuccessful() && body != null
                    ? Resource.success(body) : Resource.error(errorParser.parse(response));
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }

    /**
     * "Dilihat" bertambah saat template diklik (keputusan Aris). Dikirim di latar belakang dan kegagalannya
     * diabaikan: membuka editor tidak boleh tertunda/gagal hanya karena statistik. Versi awal belum memakai antrean
     * offline (alur-provider.md 3.5 hanya mewajibkannya untuk "Didownload").
     */
    public void recordView(String templateId) {
        executors.networkIO().execute(() -> {
            try {
                api.recordEvent(templateId, new TemplateEventDto(TemplateEventDto.VIEW, sessionStore.getInstallId(),
                        Instant.now().toString())).execute();
            } catch (IOException ignored) {
                // Offline: event ini tidak tercatat (selisih kecil yang diterima di versi awal).
            }
        });
    }
}
