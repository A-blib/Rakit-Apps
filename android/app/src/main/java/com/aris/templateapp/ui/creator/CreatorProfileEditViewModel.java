package com.aris.templateapp.ui.creator;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.data.repository.UserRepository;
import com.aris.templateapp.ui.onboarding.OnboardingFormValidator;

import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/** "Edit profil" pembuat website (bagian 5.1 & 5.4): isi awal dari salinan user di HP, simpan lewat PATCH. */
@HiltViewModel
public class CreatorProfileEditViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final AppExecutors executors;

    private final MutableLiveData<User> initial = new MutableLiveData<>();
    private final MutableLiveData<Map<OnboardingFormValidator.Field, Integer>> formErrors = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saving = new MutableLiveData<>(false);
    private final MutableLiveData<Event<ApiError>> failure = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> saved = new MutableLiveData<>();

    @Inject
    public CreatorProfileEditViewModel(UserRepository userRepository, AppExecutors executors) {
        this.userRepository = userRepository;
        this.executors = executors;
        executors.diskIO().execute(() -> initial.postValue(userRepository.getCachedUser()));
    }

    public LiveData<User> getInitial() {
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

    /** @param websitePurpose null = tidak dipilih; organizationName kosong = dikosongkan */
    public void save(String displayName, String websitePurpose, String organizationName) {
        Map<OnboardingFormValidator.Field, Integer> errors = OnboardingFormValidator.validateCreator(displayName);
        formErrors.setValue(errors);
        if (!errors.isEmpty() || Boolean.TRUE.equals(saving.getValue())) {
            return;
        }
        saving.setValue(true);
        String organization = organizationName == null || organizationName.trim().isEmpty()
                ? null : organizationName.trim();
        executors.networkIO().execute(() -> {
            Resource<User> result = userRepository.updateCreatorProfile(displayName.trim(), websitePurpose, organization);
            saving.postValue(false);
            if (result.getStatus() == Resource.Status.SUCCESS) {
                saved.postValue(new Event<>(true));
            } else {
                failure.postValue(new Event<>(result.getError()));
            }
        });
    }
}
