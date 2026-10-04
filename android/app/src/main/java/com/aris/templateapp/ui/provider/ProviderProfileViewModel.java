package com.aris.templateapp.ui.provider;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.remote.dto.ProviderProfileDto;
import com.aris.templateapp.data.repository.ProviderRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Tab Profil: profil kreator + ringkasan (alur-provider.md bagian 7). Beralih mode & keluar memakai
 * {@link com.aris.templateapp.ui.profile.ProfileViewModel} yang sama dengan menu profil.
 */
@HiltViewModel
public class ProviderProfileViewModel extends ViewModel {

    private final ProviderRepository repository;
    private final AppExecutors executors;
    private final MutableLiveData<Resource<ProviderProfileDto>> profile = new MutableLiveData<>();

    @Inject
    public ProviderProfileViewModel(ProviderRepository repository, AppExecutors executors) {
        this.repository = repository;
        this.executors = executors;
        load();
    }

    public LiveData<Resource<ProviderProfileDto>> getProfile() {
        return profile;
    }

    /** Loading penuh hanya jika belum ada data; muat ulang berikutnya (mis. setelah edit) diam-diam. */
    public void load() {
        Resource<ProviderProfileDto> current = profile.getValue();
        if (current == null || current.getData() == null) {
            profile.setValue(Resource.loading());
        }
        executors.networkIO().execute(() -> {
            Resource<ProviderProfileDto> result = repository.profile();
            if (result.getStatus() == Resource.Status.ERROR && current != null && current.getData() != null) {
                return;
            }
            profile.postValue(result);
        });
    }
}
