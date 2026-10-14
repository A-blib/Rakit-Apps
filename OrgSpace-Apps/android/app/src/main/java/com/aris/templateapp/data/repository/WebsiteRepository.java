package com.aris.templateapp.data.repository;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.aris.templateapp.core.network.ApiErrorParser;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.local.WebsiteDao;
import com.aris.templateapp.data.local.WebsiteEntity;
import com.aris.templateapp.data.mapper.WebsiteMapper;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.PublishRequest;
import com.aris.templateapp.data.model.Website;
import com.aris.templateapp.data.model.WebsiteDetail;
import com.aris.templateapp.data.model.WebsitePage;
import com.aris.templateapp.data.model.WebsiteQuery;
import com.aris.templateapp.data.remote.api.WebsiteApi;
import com.aris.templateapp.data.remote.dto.ConfirmNameRequestDto;
import com.aris.templateapp.data.remote.dto.DirectPublishRequestDto;
import com.aris.templateapp.data.remote.dto.DirectPublishResponseDto;
import com.aris.templateapp.data.remote.dto.PublishRequestDto;
import com.aris.templateapp.data.remote.dto.PublishRequestNoteDto;
import com.aris.templateapp.data.remote.dto.WebsiteListDto;
import com.aris.templateapp.data.remote.dto.WebsiteNameRequestDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Response;

/**
 * Website di ruang kerja (alur-pembuatan-website.md 4–5, 10). Server adalah sumber kebenaran; daftar lengkap
 * (tanpa filter) disimpan di Room supaya tab Website tetap bisa dibuka saat offline. Semua method
 * {@code @WorkerThread}.
 */
@Singleton
public class WebsiteRepository {

    public static final int PAGE_SIZE = 20;

    private final WebsiteApi api;
    private final WebsiteDao dao;
    private final ApiErrorParser errorParser;

    @Inject
    public WebsiteRepository(WebsiteApi api, WebsiteDao dao, ApiErrorParser errorParser) {
        this.api = api;
        this.dao = dao;
        this.errorParser = errorParser;
    }

    /**
     * Satu halaman daftar. Jika tidak ada koneksi, hasilnya dari cache (filter & urutan diterapkan di HP) dengan
     * {@link WebsitePage#offlineSince} terisi; error lain dikembalikan apa adanya.
     */
    @WorkerThread
    public Resource<WebsitePage> page(String organizationId, WebsiteQuery query, int page) {
        try {
            Response<WebsiteListDto> response = api.websites(organizationId, query.filter.value,
                    query.search.isEmpty() ? null : query.search, query.sort.value, page, PAGE_SIZE).execute();
            if (!response.isSuccessful() || response.body() == null) {
                return Resource.error(errorParser.parse(response));
            }
            WebsitePage result = WebsiteMapper.toModel(organizationId, response.body());
            if (query.isUnfiltered()) {
                cache(organizationId, result, page);
            }
            return Resource.success(result);
        } catch (IOException e) {
            ApiError error = errorParser.parse(e);
            return page == 0 ? fromCache(organizationId, query, error) : Resource.error(error);
        }
    }

    @WorkerThread
    public Resource<WebsiteDetail> detail(String organizationId, String websiteId) {
        return execute(() -> api.website(websiteId).execute(), dto -> WebsiteMapper.toModel(organizationId, dto));
    }

    @WorkerThread
    public Resource<WebsiteDetail> rename(String organizationId, String websiteId, String name) {
        return execute(() -> api.rename(websiteId, new WebsiteNameRequestDto(name)).execute(),
                dto -> WebsiteMapper.toModel(organizationId, dto));
    }

    @WorkerThread
    public Resource<WebsiteDetail> duplicate(String organizationId, String websiteId) {
        return execute(() -> api.duplicate(websiteId, new WebsiteNameRequestDto(null)).execute(),
                dto -> WebsiteMapper.toModel(organizationId, dto));
    }

    /** @param confirmName wajib (nama website) jika website pernah diterbitkan, selain itu null */
    @WorkerThread
    public Resource<Boolean> delete(String websiteId, @Nullable String confirmName) {
        try {
            Response<Void> response = api.delete(websiteId, new ConfirmNameRequestDto(confirmName)).execute();
            if (!response.isSuccessful()) {
                return Resource.error(errorParser.parse(response));
            }
            dao.delete(websiteId);
            return Resource.success(true);
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }

    @WorkerThread
    public Resource<PublishRequest> requestPublish(String websiteId, @Nullable String note) {
        return execute(() -> api.requestPublish(websiteId, new PublishRequestNoteDto(note)).execute(),
                WebsiteMapper::toModel);
    }

    @WorkerThread
    public Resource<List<PublishRequest>> openRequests(String organizationId) {
        return execute(() -> api.openRequests(organizationId).execute(), list -> {
            List<PublishRequest> result = new ArrayList<>();
            for (PublishRequestDto dto : list) {
                result.add(WebsiteMapper.toModel(dto));
            }
            return result;
        });
    }

    @WorkerThread
    public Resource<Boolean> dismissRequest(String requestId) {
        try {
            Response<Void> response = api.dismissRequest(requestId).execute();
            return response.isSuccessful() ? Resource.success(true) : Resource.error(errorParser.parse(response));
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }

    @WorkerThread
    public Resource<DirectPublishResponseDto> publishDirect(DirectPublishRequestDto request) {
        return execute(() -> api.publishDirect(request).execute(), dto -> dto);
    }

    /** Keluar akun: cache website akun lama tidak boleh terlihat akun berikutnya. */
    @WorkerThread
    public void clearCache() {
        dao.clearAll();
    }

    private void cache(String organizationId, WebsitePage result, int page) {
        long now = System.currentTimeMillis();
        List<WebsiteEntity> rows = new ArrayList<>();
        for (Website website : result.items) {
            rows.add(WebsiteMapper.toEntity(website, now));
        }
        if (page == 0) {
            dao.replace(organizationId, rows);
        } else {
            dao.upsert(rows);
        }
    }

    private Resource<WebsitePage> fromCache(String organizationId, WebsiteQuery query, ApiError networkError) {
        List<WebsiteEntity> rows = dao.forOrganization(organizationId);
        if (rows.isEmpty()) {
            return Resource.error(networkError);
        }
        List<Website> all = new ArrayList<>();
        long cachedAt = 0;
        for (WebsiteEntity row : rows) {
            all.add(WebsiteMapper.toModel(row));
            cachedAt = Math.max(cachedAt, row.cachedAt);
        }
        // Batas website beralamat tidak diketahui saat offline; 0 = info batas disembunyikan.
        return Resource.success(new WebsitePage(query.apply(all), query.count(all), 0, 0, 0, false, cachedAt));
    }

    private interface Call<D> {
        Response<D> run() throws IOException;
    }

    private interface Mapper<D, T> {
        T map(D dto);
    }

    private <D, T> Resource<T> execute(Call<D> call, Mapper<D, T> mapper) {
        try {
            Response<D> response = call.run();
            D body = response.body();
            return response.isSuccessful() && body != null ? Resource.success(mapper.map(body))
                    : Resource.error(errorParser.parse(response));
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }

    /** Dipakai halaman kelola untuk memperbarui kartu di cache setelah ganti nama, tanpa memuat ulang daftar. */
    @WorkerThread
    public void updateCached(WebsiteDetail detail) {
        List<WebsiteEntity> rows = new ArrayList<>();
        rows.add(WebsiteMapper.toEntity(detail.website, System.currentTimeMillis()));
        dao.upsert(rows);
    }
}
