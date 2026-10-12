package com.aris.templateapp.ui.website;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.Website;
import com.aris.templateapp.data.model.WebsiteDetail;
import com.aris.templateapp.data.model.WebsitePage;
import com.aris.templateapp.data.model.WebsiteQuery;
import com.aris.templateapp.data.repository.WebsiteRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Tab Website (alur-pembuatan-website.md 4): daftar website ruang kerja aktif dengan cari, chip status, urutan,
 * paginasi 20, dan aksi kartu (ganti nama, duplikat, hapus). Pindah ruang kerja = daftar dimuat ulang.
 */
@HiltViewModel
public class WebsitesViewModel extends ViewModel {

    /** Keadaan layar pada satu waktu (objek baru setiap berubah). */
    public static final class State {
        public final List<Website> items;
        @Nullable public final WebsitePage.Counts counts;
        public final int sitesUsed;
        public final int sitesLimit;
        @Nullable public final Long offlineSince;
        public final boolean loadingFirstPage;
        public final boolean loadingMore;
        @Nullable public final ApiError error;

        State(List<Website> items, @Nullable WebsitePage.Counts counts, int sitesUsed, int sitesLimit,
              @Nullable Long offlineSince, boolean loadingFirstPage, boolean loadingMore, @Nullable ApiError error) {
            this.items = Collections.unmodifiableList(items);
            this.counts = counts;
            this.sitesUsed = sitesUsed;
            this.sitesLimit = sitesLimit;
            this.offlineSince = offlineSince;
            this.loadingFirstPage = loadingFirstPage;
            this.loadingMore = loadingMore;
            this.error = error;
        }
    }

    private final WebsiteRepository repository;
    private final AppExecutors executors;

    private final MutableLiveData<State> state = new MutableLiveData<>();
    private final MutableLiveData<WebsiteQuery> query = new MutableLiveData<>(WebsiteQuery.initial());
    private final MutableLiveData<Event<String>> message = new MutableLiveData<>();
    private final MutableLiveData<Event<ApiError>> failure = new MutableLiveData<>();
    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);

    @Nullable private String organizationId;
    private final List<Website> items = new ArrayList<>();
    @Nullable private WebsitePage lastPage;
    private boolean loadingFirstPage;
    private boolean loadingMore;
    private int generation;

    @Inject
    public WebsitesViewModel(WebsiteRepository repository, AppExecutors executors) {
        this.repository = repository;
        this.executors = executors;
    }

    public LiveData<State> getState() {
        return state;
    }

    public LiveData<WebsiteQuery> getQuery() {
        return query;
    }

    /** Pesan sukses (teks sudah jadi, mis. nama salinan). */
    public LiveData<Event<String>> getMessage() {
        return message;
    }

    public LiveData<Event<ApiError>> getFailure() {
        return failure;
    }

    public LiveData<Boolean> isBusy() {
        return busy;
    }

    /** Dipanggil saat ruang kerja aktif berubah (atau pertama kali diketahui). Null = tamu / belum punya. */
    public void setOrganization(@Nullable String id) {
        if (id == null ? organizationId == null : id.equals(organizationId)) {
            return;
        }
        organizationId = id;
        // Ruang kerja lain = pencarian & filter lama tidak relevan lagi.
        query.setValue(WebsiteQuery.initial());
        refresh();
    }

    public void setFilter(WebsiteQuery.Filter filter) {
        update(current().withFilter(filter));
    }

    public void setSearch(String search) {
        update(current().withSearch(search));
    }

    public void setSort(WebsiteQuery.Sort sort) {
        update(current().withSort(sort));
    }

    public void clearFilters() {
        update(current().withFilter(WebsiteQuery.Filter.ALL).withSearch(""));
    }

    public void refresh() {
        int request = ++generation;
        items.clear();
        lastPage = null;
        loadingMore = false;
        if (organizationId == null) {
            loadingFirstPage = false;
            publish(null);
            return;
        }
        loadingFirstPage = true;
        publish(null);
        String org = organizationId;
        WebsiteQuery q = current();
        executors.networkIO().execute(() -> {
            Resource<WebsitePage> result = repository.page(org, q, 0);
            executors.mainThread().execute(() -> {
                if (request != generation) {
                    return;
                }
                loadingFirstPage = false;
                if (result.getStatus() == Resource.Status.SUCCESS) {
                    accept(result.getData());
                    publish(null);
                } else {
                    publish(result.getError());
                }
            });
        });
    }

    /** Daftar digulir mendekati akhir. Data dari cache (offline) tidak punya halaman berikutnya. */
    public void loadMore() {
        if (loadingFirstPage || loadingMore || lastPage == null || !lastPage.hasMore || organizationId == null) {
            return;
        }
        int request = generation;
        loadingMore = true;
        publish(null);
        String org = organizationId;
        WebsiteQuery q = current();
        int next = lastPage.page + 1;
        executors.networkIO().execute(() -> {
            Resource<WebsitePage> result = repository.page(org, q, next);
            executors.mainThread().execute(() -> {
                if (request != generation) {
                    return;
                }
                loadingMore = false;
                if (result.getStatus() == Resource.Status.SUCCESS) {
                    accept(result.getData());
                } else {
                    failure.setValue(new Event<>(result.getError()));
                }
                publish(null);
            });
        });
    }

    public void rename(Website website, String name, String doneMessage) {
        run(() -> repository.rename(website.organizationId, website.id, name.trim()), detail -> doneMessage);
    }

    /** Salinan muncul paling atas (terakhir diubah); pesan menyebut nama salinannya. */
    public void duplicate(Website website, String messageTemplate) {
        run(() -> repository.duplicate(website.organizationId, website.id),
                detail -> String.format(messageTemplate, detail.website.name));
    }

    public void delete(Website website, @Nullable String confirmName, String doneMessage) {
        runSimple(() -> repository.delete(website.id, confirmName), doneMessage);
    }

    private interface MessageOf {
        String of(WebsiteDetail detail);
    }

    private void run(Supplier<Resource<WebsiteDetail>> action, MessageOf messageOf) {
        if (Boolean.TRUE.equals(busy.getValue())) {
            return;
        }
        busy.setValue(true);
        executors.networkIO().execute(() -> {
            Resource<WebsiteDetail> result = action.get();
            executors.mainThread().execute(() -> {
                busy.setValue(false);
                if (result.getStatus() == Resource.Status.SUCCESS) {
                    message.setValue(new Event<>(messageOf.of(result.getData())));
                    refresh();
                } else {
                    failure.setValue(new Event<>(result.getError()));
                }
            });
        });
    }

    private void runSimple(Supplier<Resource<Boolean>> action, String doneMessage) {
        if (Boolean.TRUE.equals(busy.getValue())) {
            return;
        }
        busy.setValue(true);
        executors.networkIO().execute(() -> {
            Resource<Boolean> result = action.get();
            executors.mainThread().execute(() -> {
                busy.setValue(false);
                if (result.getStatus() == Resource.Status.SUCCESS) {
                    message.setValue(new Event<>(doneMessage));
                    refresh();
                } else {
                    failure.setValue(new Event<>(result.getError()));
                }
            });
        });
    }

    private WebsiteQuery current() {
        WebsiteQuery value = query.getValue();
        return value == null ? WebsiteQuery.initial() : value;
    }

    private void update(WebsiteQuery newQuery) {
        WebsiteQuery old = current();
        if (old.filter == newQuery.filter && old.sort == newQuery.sort && old.search.equals(newQuery.search)) {
            return;
        }
        query.setValue(newQuery);
        refresh();
    }

    private void accept(WebsitePage page) {
        items.addAll(page.items);
        lastPage = page;
    }

    private void publish(@Nullable ApiError error) {
        WebsitePage page = lastPage;
        state.setValue(new State(new ArrayList<>(items), page == null ? null : page.counts,
                page == null ? 0 : page.sitesUsed, page == null ? 0 : page.sitesLimit,
                page == null ? null : page.offlineSince, loadingFirstPage, loadingMore, error));
    }
}
