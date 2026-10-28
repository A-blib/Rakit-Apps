package com.aris.templateapp.ui.organization;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.PortalCredential;
import com.aris.templateapp.data.model.WorkspaceRole;
import com.aris.templateapp.data.repository.OrganizationRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/** Kredensial Portal Pengurus organisasi (hanya Pemilik): daftar peran yang sudah diatur dan simpan satu peran. */
@HiltViewModel
public class PortalAccessViewModel extends ViewModel {

    private final OrganizationRepository repository;
    private final AppExecutors executors;

    private final MutableLiveData<Resource<List<PortalCredential>>> credentials = new MutableLiveData<>();
    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    private final MutableLiveData<Event<ApiError>> failure = new MutableLiveData<>();
    private final MutableLiveData<Event<WorkspaceRole>> saved = new MutableLiveData<>();
    private String organizationId;

    @Inject
    public PortalAccessViewModel(OrganizationRepository repository, AppExecutors executors) {
        this.repository = repository;
        this.executors = executors;
    }

    public void start(String id) {
        if (organizationId == null) {
            organizationId = id;
            load();
        }
    }

    public LiveData<Resource<List<PortalCredential>>> getCredentials() {
        return credentials;
    }

    public LiveData<Boolean> isBusy() {
        return busy;
    }

    public LiveData<Event<ApiError>> getFailure() {
        return failure;
    }

    /** Peran yang baru saja disimpan (untuk pesan). */
    public LiveData<Event<WorkspaceRole>> getSaved() {
        return saved;
    }

    public void load() {
        if (credentials.getValue() == null || credentials.getValue().getStatus() == Resource.Status.ERROR) {
            credentials.setValue(Resource.loading());
        }
        executors.networkIO().execute(() -> credentials.postValue(repository.portalCredentials(organizationId)));
    }

    public void save(WorkspaceRole role, String username, String password) {
        if (Boolean.TRUE.equals(busy.getValue())) {
            return;
        }
        busy.setValue(true);
        executors.networkIO().execute(() -> {
            Resource<PortalCredential> result = repository.savePortalCredential(organizationId, role, username.strip(), password);
            busy.postValue(false);
            if (result.getStatus() == Resource.Status.SUCCESS) {
                saved.postValue(new Event<>(role));
                credentials.postValue(repository.portalCredentials(organizationId));
            } else {
                failure.postValue(new Event<>(result.getError()));
            }
        });
    }
}
