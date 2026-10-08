package com.aris.templateapp.data.repository;

import androidx.annotation.WorkerThread;

import com.aris.templateapp.core.network.ApiErrorParser;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.remote.api.GalleryApi;
import com.aris.templateapp.data.remote.dto.GalleryPageDto;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Response;

/** Galeri Template dari backend (butuh internet). "Dilihat" dicatat {@link TemplateEventRepository}. */
@Singleton
public class GalleryRepository {

    public static final String SORT_POPULAR = "popular";
    public static final String SORT_NEWEST = "newest";

    private final GalleryApi api;
    private final ApiErrorParser errorParser;

    @Inject
    public GalleryRepository(GalleryApi api, ApiErrorParser errorParser) {
        this.api = api;
        this.errorParser = errorParser;
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
}
