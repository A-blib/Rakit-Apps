package com.aris.templateapp.ui.website;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.WebsiteDetail;
import com.aris.templateapp.data.repository.WebsiteRepository;

import java.util.function.Supplier;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/** Halaman kelola website: detail dari server + aksi menu ⋮ + "Minta diterbitkan". */
@HiltViewModel
public class WebsiteManageViewModel extends ViewModel {

    /** Hasil aksi yang perlu ditangani layar. */
    public enum Done { RENAMED, DUPLICATED, DELETED, REQUESTED }

    public static final class Outcome {
        public final Done kind;
        /** Nama salinan (DUPLICATED); selain itu null. */
        @Nullable public final String name;

        Outcome(Done kind, @Nullable String name) {
            this.kind = kind;
            this.name = name;
        }
    }

    private final WebsiteRepository repository;
    private final AppExecutors executors;
    private final MutableLiveData<Resource<WebsiteDetail>> detail = new MutableLiveData<>();
    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    private final MutableLiveData<Event<Outcome>> done = new MutableLiveData<>();
    private final MutableLiveData<Event<ApiError>> failure = new MutableLiveData<>();
    private String organizationId;
    private String websiteId;

    @Inject
    public WebsiteManageViewModel(WebsiteRepository repository, AppExecutors executors) {
        this.repository = repository;
        this.executors = executors;
    }

    public void start(String organizationId, String websiteId) {
        if (this.websiteId == null) {
            this.organizationId = organizationId;
            this.websiteId = websiteId;
            load();
        }
    }

    public LiveData<Resource<WebsiteDetail>> getDetail() {
        return detail;
    }

    public LiveData<Boolean> isBusy() {
        return busy;
    }

    public LiveData<Event<Outcome>> getDone() {
        return done;
    }

    public LiveData<Event<ApiError>> getFailure() {
        return failure;
    }

    public void load() {
        if (detail.getValue() == null || detail.getValue().getStatus() == Resource.Status.ERROR) {
            detail.setValue(Resource.loading());
        }
        executors.networkIO().execute(() -> {
            Resource<WebsiteDetail> result = repository.detail(organizationId, websiteId);
            if (result.getStatus() == Resource.Status.SUCCESS) {
                repository.updateCached(result.getData());
            }
            detail.postValue(result);
        });
    }

    public void rename(String name) {
        run(() -> repository.rename(organizationId, websiteId, name), result -> {
            detail.postValue(result);
            repository.updateCached(result.getData());
            return new Outcome(Done.RENAMED, null);
        });
    }

    public void duplicate() {
        run(() -> repository.duplicate(organizationId, websiteId),
                result -> new Outcome(Done.DUPLICATED, result.getData().website.name));
    }

    public void delete(@Nullable String confirmName) {
        run(() -> repository.delete(websiteId, confirmName), result -> new Outcome(Done.DELETED, null));
    }

    public void requestPublish(@Nullable String note) {
        run(() -> repository.requestPublish(websiteId, note == null || note.isBlank() ? null : note.trim()),
                result -> {
                    load();
                    return new Outcome(Done.REQUESTED, null);
                });
    }

    private interface OnSuccess<T> {
        Outcome handle(Resource<T> result);
    }

    private <T> void run(Supplier<Resource<T>> action, OnSuccess<T> onSuccess) {
        if (Boolean.TRUE.equals(busy.getValue())) {
            return;
        }
        busy.setValue(true);
        executors.networkIO().execute(() -> {
            Resource<T> result = action.get();
            busy.postValue(false);
            if (result.getStatus() == Resource.Status.SUCCESS) {
                done.postValue(new Event<>(onSuccess.handle(result)));
            } else {
                failure.postValue(new Event<>(result.getError()));
            }
        });
    }
}
