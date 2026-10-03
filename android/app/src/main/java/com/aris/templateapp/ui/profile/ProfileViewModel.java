package com.aris.templateapp.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.data.model.UserRole;
import com.aris.templateapp.data.repository.AuthRepository;
import com.aris.templateapp.data.repository.UserRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/** Menu profil: data user, beralih mode, dan keluar (bagian 6.6 & 6.7). */
@HiltViewModel
public class ProfileViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final AppExecutors executors;

    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<User>> modeChanged = new MutableLiveData<>();
    private final MutableLiveData<Event<ApiError>> failure = new MutableLiveData<>();
    private final MutableLiveData<Event<Boolean>> signedOut = new MutableLiveData<>();

    @Inject
    public ProfileViewModel(UserRepository userRepository, AuthRepository authRepository, AppExecutors executors) {
        this.userRepository = userRepository;
        this.authRepository = authRepository;
        this.executors = executors;
        executors.diskIO().execute(() -> user.postValue(userRepository.getCachedUser()));
    }

    public LiveData<User> getUser() {
        return user;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    public LiveData<Event<User>> getModeChanged() {
        return modeChanged;
    }

    public LiveData<Event<ApiError>> getFailure() {
        return failure;
    }

    public LiveData<Event<Boolean>> getSignedOut() {
        return signedOut;
    }

    public void switchMode(UserRole target) {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }
        loading.setValue(true);
        executors.networkIO().execute(() -> {
            Resource<User> result = userRepository.changeActiveMode(target);
            loading.postValue(false);
            if (result.getStatus() == Resource.Status.SUCCESS) {
                modeChanged.postValue(new Event<>(result.getData()));
            } else {
                failure.postValue(new Event<>(result.getError()));
            }
        });
    }

    /** Keluar selalu berhasil di sisi HP, walau sedang offline (bagian 6.7). */
    public void signOut() {
        loading.setValue(true);
        executors.networkIO().execute(() -> {
            authRepository.logout();
            loading.postValue(false);
            signedOut.postValue(new Event<>(true));
        });
    }
}
