package com.aris.templateapp.ui.creator;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.remote.dto.GalleryPageDto;
import com.aris.templateapp.data.remote.dto.GalleryPageDto.GalleryTemplateDto;
import com.aris.templateapp.data.repository.GalleryRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Tab Template: kategori, urutan, pencarian, paginasi 20 per halaman (alur-pembuatan-website.md 6.2).
 * Diambil dari Activity ({@code requireActivity()}) agar "Lihat semua" di Beranda bisa memilih kategori lebih dulu.
 */
@HiltViewModel
public class GalleryViewModel extends ViewModel {

    public static final String ALL = "all";
    static final int PAGE_SIZE = 20;

    private final GalleryRepository repository;
    private final AppExecutors executors;
    private final MutableLiveData<GalleryListState> state = new MutableLiveData<>();
    private final MutableLiveData<Event<ApiError>> loadMoreFailed = new MutableLiveData<>();

    private String category = ALL;
    private String sort = GalleryRepository.SORT_POPULAR;
    private String query = "";

    private final List<GalleryTemplateDto> items = new ArrayList<>();
    private int page;
    private int totalPages;
    private boolean loadingFirstPage;
    private boolean loadingMore;
    private int generation;

    @Inject
    public GalleryViewModel(GalleryRepository repository, AppExecutors executors) {
        this.repository = repository;
        this.executors = executors;
    }

    public LiveData<GalleryListState> getState() {
        return state;
    }

    public LiveData<Event<ApiError>> getLoadMoreFailed() {
        return loadMoreFailed;
    }

    public String getCategory() {
        return category;
    }

    public String getSort() {
        return sort;
    }

    public String getQuery() {
        return query;
    }

    public boolean hasActiveFilter() {
        return !ALL.equals(category) || !query.isEmpty();
    }

    public void setCategory(String category) {
        if (!category.equals(this.category)) {
            this.category = category;
            refresh();
        }
    }

    public void setSort(String sort) {
        if (!sort.equals(this.sort)) {
            this.sort = sort;
            refresh();
        }
    }

    public void setQuery(String query) {
        String trimmed = query == null ? "" : query.trim();
        if (!trimmed.equals(this.query)) {
            this.query = trimmed;
            refresh();
        }
    }

    /** "Hapus filter": semua kategori, tanpa pencarian (urutan tetap). */
    public void clearFilters() {
        category = ALL;
        query = "";
        refresh();
    }

    public void refresh() {
        int request = ++generation;
        loadingFirstPage = true;
        loadingMore = false;
        publish(null);
        String c = category, o = sort, q = query.isEmpty() ? null : query;
        executors.networkIO().execute(() -> {
            Resource<GalleryPageDto> result = repository.templates(c, q, o, 0, PAGE_SIZE);
            executors.mainThread().execute(() -> {
                if (request != generation) {
                    return;
                }
                loadingFirstPage = false;
                items.clear();
                if (result.getStatus() == Resource.Status.SUCCESS) {
                    accept(result.getData());
                    publish(null);
                } else {
                    page = 0;
                    totalPages = 0;
                    publish(result.getError());
                }
            });
        });
    }

    /** Dipanggil saat daftar di-scroll mendekati akhir. */
    public void loadMore() {
        if (loadingFirstPage || loadingMore || page + 1 >= totalPages) {
            return;
        }
        int request = generation;
        loadingMore = true;
        publish(null);
        int next = page + 1;
        String c = category, o = sort, q = query.isEmpty() ? null : query;
        executors.networkIO().execute(() -> {
            Resource<GalleryPageDto> result = repository.templates(c, q, o, next, PAGE_SIZE);
            executors.mainThread().execute(() -> {
                if (request != generation) {
                    return;
                }
                loadingMore = false;
                if (result.getStatus() == Resource.Status.SUCCESS) {
                    accept(result.getData());
                } else {
                    loadMoreFailed.setValue(new Event<>(result.getError()));
                }
                publish(null);
            });
        });
    }

    private void accept(GalleryPageDto data) {
        if (data.items != null) {
            items.addAll(data.items);
        }
        page = data.page;
        totalPages = data.totalPages;
    }

    private void publish(ApiError error) {
        state.setValue(new GalleryListState(new ArrayList<>(items), loadingFirstPage, loadingMore, error));
    }
}
