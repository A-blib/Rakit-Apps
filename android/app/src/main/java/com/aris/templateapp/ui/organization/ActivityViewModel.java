package com.aris.templateapp.ui.organization;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.Activity;
import com.aris.templateapp.data.repository.OrganizationRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/** Riwayat aktivitas (20 per halaman); "Muat lebih banyak" menambah halaman berikutnya ke daftar yang sama. */
@HiltViewModel
public class ActivityViewModel extends ViewModel {

    /** Daftar yang sudah dimuat + apakah masih ada halaman berikutnya. */
    public static class Page {
        public final List<Activity> items;
        public final boolean hasMore;

        Page(List<Activity> items, boolean hasMore) {
            this.items = items;
            this.hasMore = hasMore;
        }
    }

    private final OrganizationRepository repository;
    private final AppExecutors executors;
    private final MutableLiveData<Resource<Page>> page = new MutableLiveData<>();
    private final List<Activity> loaded = new ArrayList<>();
    private String organizationId;
    private int nextPage;
    private boolean loading;

    @Inject
    public ActivityViewModel(OrganizationRepository repository, AppExecutors executors) {
        this.repository = repository;
        this.executors = executors;
    }

    public void start(String id) {
        if (organizationId == null) {
            organizationId = id;
            loadMore();
        }
    }

    public LiveData<Resource<Page>> getPage() {
        return page;
    }

    public void loadMore() {
        if (loading) {
            return;
        }
        loading = true;
        if (loaded.isEmpty()) {
            page.setValue(Resource.loading());
        }
        int requested = nextPage;
        executors.networkIO().execute(() -> {
            Resource<OrganizationRepository.ActivityPage> result = repository.activities(organizationId, requested);
            executors.mainThread().execute(() -> {
                loading = false;
                if (result.getStatus() == Resource.Status.SUCCESS) {
                    loaded.addAll(result.getData().items);
                    nextPage = requested + 1;
                    page.setValue(Resource.success(new Page(new ArrayList<>(loaded), result.getData().hasMore)));
                } else {
                    page.setValue(Resource.error(result.getError()));
                }
            });
        });
    }
}
