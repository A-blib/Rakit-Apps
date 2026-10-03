package com.aris.templateapp.ui.onboarding;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.data.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/** State form onboarding pembuat website & provider (bagian 6.6). */
@HiltViewModel
public class OnboardingViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final AppExecutors executors;

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<Map<OnboardingFormValidator.Field, Integer>> formErrors = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<ApiError>> failure = new MutableLiveData<>();
    private final MutableLiveData<Event<User>> success = new MutableLiveData<>();

    @Inject
    public OnboardingViewModel(UserRepository userRepository, AppExecutors executors) {
        this.userRepository = userRepository;
        this.executors = executors;
        // Untuk mengisi otomatis nama dari Google/GitHub/daftar.
        executors.diskIO().execute(() -> currentUser.postValue(userRepository.getCachedUser()));
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Map<OnboardingFormValidator.Field, Integer>> getFormErrors() {
        return formErrors;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    public LiveData<Event<ApiError>> getFailure() {
        return failure;
    }

    public LiveData<Event<User>> getSuccess() {
        return success;
    }

    /** Tombol "Mulai". websitePurpose/organizationName boleh null. */
    public void submitCreator(String displayName, String websitePurpose, String organizationName) {
        Map<OnboardingFormValidator.Field, Integer> errors = OnboardingFormValidator.validateCreator(displayName);
        formErrors.setValue(errors);
        if (errors.isEmpty()) {
            String organization = organizationName == null || organizationName.trim().isEmpty()
                    ? null : organizationName.trim();
            run(() -> userRepository.completeCreatorOnboarding(displayName.trim(), websitePurpose, organization));
        }
    }

    /** Tombol "Lewati": tetap menyelesaikan onboarding, cukup dengan nama tampilan. */
    public void skipCreator(String displayName) {
        submitCreator(displayName, null, null);
    }

    public void submitProvider(String creatorName, String bio, String portfolioUrl, List<String> specialties,
                               boolean agreedToTerms) {
        Map<OnboardingFormValidator.Field, Integer> errors =
                OnboardingFormValidator.validateProvider(creatorName, bio, portfolioUrl, agreedToTerms);
        formErrors.setValue(errors);
        if (errors.isEmpty()) {
            run(() -> userRepository.becomeProvider(creatorName.trim(), blankToNull(bio), blankToNull(portfolioUrl),
                    specialties));
        }
    }

    private void run(Supplier<Resource<User>> request) {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }
        loading.setValue(true);
        executors.networkIO().execute(() -> {
            Resource<User> result = request.get();
            loading.postValue(false);
            if (result.getStatus() == Resource.Status.SUCCESS) {
                success.postValue(new Event<>(result.getData()));
            } else {
                failure.postValue(new Event<>(result.getError()));
            }
        });
    }

    private static String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
