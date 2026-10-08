package com.aris.templateapp.ui.provider;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.remote.dto.TemplateDetailDto;
import com.aris.templateapp.data.remote.dto.DraftDto;
import com.aris.templateapp.data.repository.ProviderRepository;
import com.aris.templateapp.data.repository.UploadRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/** Detail template + hasil pengecekan terakhir (alur-provider.md bagian 4.5 & 6.2). */
@HiltViewModel
public class TemplateDetailViewModel extends ViewModel {

    private final ProviderRepository repository;
    private final UploadRepository uploadRepository;
    private final AppExecutors executors;
    private final MutableLiveData<Resource<TemplateDetailDto>> detail = new MutableLiveData<>();
    private String templateId;

    @Inject
    public TemplateDetailViewModel(ProviderRepository repository, UploadRepository uploadRepository,
                                   AppExecutors executors) {
        this.repository = repository;
        this.uploadRepository = uploadRepository;
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

    /** Langkah terakhir draft diambil dulu agar "Lanjutkan draft" membuka tepat di langkah itu. */
    interface StepCallback {
        void onStep(int step);
    }

    void resumeDraft(StepCallback callback) {
        String id = templateId;
        executors.networkIO().execute(() -> {
            Resource<DraftDto> draft = uploadRepository.draft(id);
            int step = draft.getStatus() == Resource.Status.SUCCESS ? draft.getData().wizardStep : 3;
            executors.mainThread().execute(() -> callback.onStep(step));
        });
    }

    /** "Ini keliru? Laporkan", lalu muat ulang agar tautannya berubah menjadi "Sudah dilaporkan". */
    void report(String issueId, String reason) {
        executors.networkIO().execute(() -> {
            uploadRepository.report(issueId, reason);
            detail.postValue(repository.template(templateId));
        });
    }

    public void load() {
        detail.setValue(Resource.loading());
        String id = templateId;
        executors.networkIO().execute(() -> detail.postValue(repository.template(id)));
    }
}
