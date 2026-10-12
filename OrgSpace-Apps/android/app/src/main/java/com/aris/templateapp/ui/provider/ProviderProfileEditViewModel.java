package com.aris.templateapp.ui.provider;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.remote.dto.ProviderProfileDto;
import com.aris.templateapp.data.repository.ProviderRepository;
import com.aris.templateapp.ui.onboarding.OnboardingFormValidator;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/** "Edit profil": isi awal dari server, validasi sama dengan form provider onboarding, lalu PATCH. */
@HiltViewModel
public class ProviderProfileEditViewModel extends ViewModel {

    private final ProviderRepository repository;
    private final AppExecutors executors;

    private final MutableLiveData<Resource<ProviderProfileDto>> initial = new MutableLiveData<>();
    private final MutableLiveData<Map<OnboardingFormValidator.Field, Integer>> formErrors = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saving = new MutableLiveData<>(false);
    private final MutableLiveData<Event<ApiError>> failure = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> saved = new MutableLiveData<>();

    @Inject
    public ProviderProfileEditViewModel(ProviderRepository repository, AppExecutors executors) {
        this.repository = repository;
        this.executors = executors;
        loadInitial();
    }

    public LiveData<Resource<ProviderProfileDto>> getInitial() {
        return initial;
    }

    public LiveData<Map<OnboardingFormValidator.Field, Integer>> getFormErrors() {
        return formErrors;
    }

    public LiveData<Boolean> isSaving() {
        return saving;
    }

    public LiveData<Event<ApiError>> getFailure() {
        return failure;
    }

    public LiveData<Event<Boolean>> getSaved() {
        return saved;
    }

    public void loadInitial() {
        initial.setValue(Resource.loading());
        executors.networkIO().execute(() -> initial.postValue(repository.profile()));
    }

    public void save(String creatorName, String bio, String portfolioUrl, List<String> specialties) {
        // Persetujuan aturan sudah diberikan saat jadi provider, jadi dianggap true.
        Map<OnboardingFormValidator.Field, Integer> errors =
                OnboardingFormValidator.validateProvider(creatorName, bio, portfolioUrl, true);
        formErrors.setValue(errors);
        if (!errors.isEmpty() || Boolean.TRUE.equals(saving.getValue())) {
            return;
        }
        saving.setValue(true);
        executors.networkIO().execute(() -> {
            Resource<ProviderProfileDto> result = repository.updateProfile(creatorName.trim(), blankToNull(bio),
                    blankToNull(portfolioUrl), specialties);
            saving.postValue(false);
            if (result.getStatus() == Resource.Status.SUCCESS) {
                saved.postValue(new Event<>(true));
            } else {
                failure.postValue(new Event<>(result.getError()));
            }
        });
    }

    private static String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
