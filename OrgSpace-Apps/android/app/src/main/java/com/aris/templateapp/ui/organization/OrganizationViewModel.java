package com.aris.templateapp.ui.organization;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.Organization;
import com.aris.templateapp.data.repository.OrganizationRepository;

import java.util.function.Supplier;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/** Layar Organisasi (menu kelola): profil singkat, keluar, dan hapus organisasi. */
@HiltViewModel
public class OrganizationViewModel extends ViewModel {

    /** Hasil aksi yang membuat user meninggalkan layar ini. */
    public enum Exit { LEFT, DELETED }

    private final OrganizationRepository repository;
    private final AppExecutors executors;
    private final MutableLiveData<Resource<Organization>> organization = new MutableLiveData<>();
    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    private final MutableLiveData<Event<ApiError>> failure = new MutableLiveData<>();
    private final MutableLiveData<Event<Exit>> exit = new MutableLiveData<>();
    private String organizationId;

    @Inject
    public OrganizationViewModel(OrganizationRepository repository, AppExecutors executors) {
        this.repository = repository;
        this.executors = executors;
    }

    public void start(String id) {
        if (organizationId == null) {
            organizationId = id;
            load();
        }
    }

    public LiveData<Resource<Organization>> getOrganization() {
        return organization;
    }

    public LiveData<Boolean> isBusy() {
        return busy;
    }

    public LiveData<Event<ApiError>> getFailure() {
        return failure;
    }

    public LiveData<Event<Exit>> getExit() {
        return exit;
    }

    /** Dipanggil juga saat layar tampil lagi, mis. setelah profil diubah atau serah terima. */
    public void load() {
        if (organization.getValue() == null || organization.getValue().getStatus() == Resource.Status.ERROR) {
            organization.setValue(Resource.loading());
        }
        executors.networkIO().execute(() -> organization.postValue(repository.get(organizationId)));
    }

    public void leave() {
        run(() -> repository.leave(organizationId), Exit.LEFT);
    }

    public void delete(String confirmName) {
        run(() -> repository.delete(organizationId, confirmName), Exit.DELETED);
    }

    private void run(Supplier<Resource<Boolean>> action, Exit result) {
        if (Boolean.TRUE.equals(busy.getValue())) {
            return;
        }
        busy.setValue(true);
        executors.networkIO().execute(() -> {
            Resource<Boolean> outcome = action.get();
            busy.postValue(false);
            if (outcome.getStatus() == Resource.Status.SUCCESS) {
                exit.postValue(new Event<>(result));
            } else {
                failure.postValue(new Event<>(outcome.getError()));
            }
        });
    }
}
