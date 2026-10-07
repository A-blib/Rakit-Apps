package com.aris.templateapp.ui.upload;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.remote.dto.DraftDto;
import com.aris.templateapp.data.remote.dto.MarkingDto;
import com.aris.templateapp.data.remote.dto.UploadSettingsDto;
import com.aris.templateapp.data.repository.UploadRepository;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/** Langkah 5 Upload: Coba sebagai pengguna. Form hanya berisi isian yang sudah disimpan (bagian 7.12). */
@HiltViewModel
public class TryViewModel extends ViewModel {

    static final class Ready {
        final File siteRoot;
        final List<String> allowedHosts;
        final MarkingDto marking;
        final DraftDto draft;

        Ready(File siteRoot, List<String> allowedHosts, MarkingDto marking, DraftDto draft) {
            this.siteRoot = siteRoot;
            this.allowedHosts = allowedHosts;
            this.marking = marking;
            this.draft = draft;
        }
    }

    private final UploadRepository repository;
    private final AppExecutors executors;
    private final MutableLiveData<Resource<Ready>> ready = new MutableLiveData<>();
    private String templateId;
    private String page = "index.html";
    private String view = "mobile";
    @Nullable
    private String sectionId;

    @Inject
    public TryViewModel(UploadRepository repository, AppExecutors executors) {
        this.repository = repository;
        this.executors = executors;
    }

    LiveData<Resource<Ready>> getReady() {
        return ready;
    }

    String templateId() {
        return templateId;
    }

    String page() {
        return page;
    }

    void setPage(String page) {
        this.page = page;
    }

    String view() {
        return view;
    }

    void setView(String view) {
        this.view = view;
    }

    @Nullable
    String sectionId() {
        return sectionId;
    }

    void setSectionId(@Nullable String sectionId) {
        this.sectionId = sectionId;
    }

    void start(String templateId) {
        if (templateId.equals(this.templateId)) {
            return;
        }
        this.templateId = templateId;
        load();
    }

    void load() {
        ready.setValue(Resource.loading());
        String id = templateId;
        executors.networkIO().execute(() -> {
            Resource<DraftDto> draft = repository.draft(id);
            Resource<MarkingDto> marking = repository.marking(id);
            Resource<UploadSettingsDto> settings = repository.settings();
            Resource<File> site = repository.prepareWorkSite(id);
            for (Resource<?> r : new Resource<?>[]{draft, marking, settings, site}) {
                if (r.getStatus() != Resource.Status.SUCCESS) {
                    ApiError error = r.getError();
                    ready.postValue(Resource.error(error));
                    return;
                }
            }
            repository.updateStep(id, 5);
            ready.postValue(Resource.success(new Ready(site.getData(), settings.getData().allowedHosts,
                    marking.getData(), draft.getData())));
        });
    }
}
