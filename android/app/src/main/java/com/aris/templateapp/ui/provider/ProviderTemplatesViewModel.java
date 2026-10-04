package com.aris.templateapp.ui.provider;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.remote.dto.TemplateListDto;
import com.aris.templateapp.data.remote.dto.TemplateListDto.TemplateSummaryDto;
import com.aris.templateapp.data.repository.ProviderRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * "Template Anda": filter, urutan, pencarian, dan paginasi 20 per halaman (alur-provider.md bagian 6).
 * <p>
 * Diambil dengan {@code new ViewModelProvider(requireActivity())} sehingga hidup selama Activity hidup:
 * filter terakhir tetap diingat walau user pindah tab, membuka detail, atau keluar-masuk Dashboard Provider.
 */
@HiltViewModel
public class ProviderTemplatesViewModel extends ViewModel {

    public static final String ALL = "all";
    public static final String STATUS_NEEDS_FIX = "needs_fix";
    public static final String SORT_UPDATED = "updated";

    private final ProviderRepository repository;
    private final AppExecutors executors;
    private final MutableLiveData<TemplateListState> state = new MutableLiveData<>();
    private final MutableLiveData<Event<ApiError>> loadMoreFailed = new MutableLiveData<>();

    private String status = ALL;
    private String category = ALL;
    private String sort = SORT_UPDATED;
    private String query = "";

    private final List<TemplateSummaryDto> items = new ArrayList<>();
    private TemplateListDto.StatusCountsDto counts;
    private int page;
    private int totalPages;
    private boolean loadingFirstPage;
    private boolean loadingMore;
    private int generation;

    @Inject
    public ProviderTemplatesViewModel(ProviderRepository repository, AppExecutors executors) {
        this.repository = repository;
        this.executors = executors;
    }

    public LiveData<TemplateListState> getState() {
        return state;
    }

    public LiveData<Event<ApiError>> getLoadMoreFailed() {
        return loadMoreFailed;
    }

    public String getStatus() {
        return status;
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
        return !ALL.equals(status) || !ALL.equals(category) || !query.isEmpty();
    }

    public void setStatus(String status) {
        if (!status.equals(this.status)) {
            this.status = status;
            refresh();
        }
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

    /** Tombol "Hapus filter": kembali ke semua status & kategori, tanpa pencarian (urutan tetap). */
    public void clearFilters() {
        status = ALL;
        category = ALL;
        query = "";
        refresh();
    }

    /** Muat ulang dari halaman pertama (isi lama tetap tampil sampai jawaban datang). */
    public void refresh() {
        int request = ++generation;
        loadingFirstPage = true;
        loadingMore = false;
        publish(null);
        String s = status, c = category, o = sort, q = query.isEmpty() ? null : query;
        executors.networkIO().execute(() -> {
            Resource<TemplateListDto> result = repository.templates(s, c, o, q, 0);
            executors.mainThread().execute(() -> {
                if (request != generation) {
                    return;
                }
                loadingFirstPage = false;
                if (result.getStatus() == Resource.Status.SUCCESS) {
                    items.clear();
                    accept(result.getData());
                    publish(null);
                } else {
                    items.clear();
                    publish(result.getError());
                }
            });
        });
    }

    /** Dipanggil saat daftar di-scroll mendekati akhir. Tidak melakukan apa-apa jika tidak ada halaman lagi. */
    public void loadMore() {
        if (loadingFirstPage || loadingMore || page + 1 >= totalPages) {
            return;
        }
        int request = generation;
        loadingMore = true;
        publish(null);
        int next = page + 1;
        String s = status, c = category, o = sort, q = query.isEmpty() ? null : query;
        executors.networkIO().execute(() -> {
            Resource<TemplateListDto> result = repository.templates(s, c, o, q, next);
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

    private void accept(TemplateListDto data) {
        if (data.items != null) {
            items.addAll(data.items);
        }
        counts = data.counts;
        page = data.page;
        totalPages = data.totalPages;
    }

    private void publish(ApiError error) {
        state.setValue(new TemplateListState(new ArrayList<>(items), counts, loadingFirstPage, loadingMore,
                page + 1 < totalPages, error));
    }
}
