package com.aris.templateapp.ui.provider;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.remote.dto.TemplateDetailDto;
import com.aris.templateapp.data.repository.ProviderRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/** Detail template + hasil pengecekan terakhir (alur-provider.md bagian 4.5 & 6.2). */
@HiltViewModel
public class TemplateDetailViewModel extends ViewModel {

    private final ProviderRepository repository;
    private final AppExecutors executors;
    private final MutableLiveData<Resource<TemplateDetailDto>> detail = new MutableLiveData<>();
    private String templateId;

    @Inject
    public TemplateDetailViewModel(ProviderRepository repository, AppExecutors executors) {
        this.repository = repository;
        this.executors = executors;
    }

    public LiveData<Resource<TemplateDetailDto>> getDetail() {
        return detail;
    }

    /** Hanya memuat sekali per template (layar diputar tidak memuat ulang). */
    public void start(String templateId) {
        if (templateId.equals(this.templateId)) {
            return;
        }
        this.templateId = templateId;
        load();
    }

    public void load() {
        detail.setValue(Resource.loading());
        String id = templateId;
        executors.networkIO().execute(() -> detail.postValue(repository.template(id)));
    }
}
