package com.aris.templateapp.ui.provider;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.R;
import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.remote.dto.UploadOverviewDto;
import com.aris.templateapp.data.remote.dto.UploadSettingsDto;
import com.aris.templateapp.data.repository.UploadRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/** Halaman awal tab Upload (alur-fitur-upload.md bagian 3.1). */
@HiltViewModel
public class ProviderUploadViewModel extends ViewModel {

    private final UploadRepository repository;
    private final AppExecutors executors;
    private final MutableLiveData<Resource<UploadOverviewDto>> overview = new MutableLiveData<>();
    private final MutableLiveData<Long> maxZipBytes = new MutableLiveData<>();
    private final MutableLiveData<Event<Integer>> message = new MutableLiveData<>();

    @Inject
    public ProviderUploadViewModel(UploadRepository repository, AppExecutors executors) {
        this.repository = repository;
        this.executors = executors;
    }

    LiveData<Resource<UploadOverviewDto>> getOverview() {
        return overview;
    }

    LiveData<Long> getMaxZipBytes() {
        return maxZipBytes;
    }

    LiveData<Event<Integer>> getMessage() {
        return message;
    }

    /**
     * Dipanggil setiap tab dibuka. Jika data sudah ada, dimuat ulang diam-diam (tanpa layar loading) agar kartu
     * "Sedang dicek" dan draft selalu terbaru.
     */
    void refresh() {
        if (overview.getValue() == null || overview.getValue().getData() == null) {
            overview.setValue(Resource.loading());
        }
        executors.networkIO().execute(() -> {
            Resource<UploadSettingsDto> settings = repository.settings();
            if (settings.getStatus() == Resource.Status.SUCCESS) {
                maxZipBytes.postValue(settings.getData().maxZipBytes);
            }
            Resource<UploadOverviewDto> result = repository.overview();
            Resource<UploadOverviewDto> current = overview.getValue();
            // Gagal memuat ulang diam-diam: pertahankan data lama, jangan ganti layar dengan pesan error.
            if (result.getStatus() == Resource.Status.ERROR && current != null && current.getData() != null) {
                return;
            }
            overview.postValue(result);
        });
    }

    void delete(String templateId) {
        executors.networkIO().execute(() -> {
            Resource<Boolean> result = repository.delete(templateId);
            message.postValue(new Event<>(result.getStatus() == Resource.Status.SUCCESS
                    ? R.string.upload_deleted : R.string.error_unknown));
            executors.mainThread().execute(this::refresh);
        });
    }
}
