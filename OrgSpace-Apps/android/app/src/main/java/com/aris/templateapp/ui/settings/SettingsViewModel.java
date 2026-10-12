package com.aris.templateapp.ui.settings;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.Identity;
import com.aris.templateapp.data.model.LoginMethod;
import com.aris.templateapp.data.repository.UserRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/** Pengaturan → Metode login terhubung: lihat, sambungkan (Google/GitHub), lepaskan. */
@HiltViewModel
public class SettingsViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final AppExecutors executors;

    private final MutableLiveData<Resource<List<Identity>>> identities = new MutableLiveData<>();
    private final MutableLiveData<Boolean> busy = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> openUrl = new MutableLiveData<>();
    private final MutableLiveData<Event<ApiError>> failure = new MutableLiveData<>();
    private final MutableLiveData<Event<LoginMethod>> linked = new MutableLiveData<>();
    private final MutableLiveData<Event<LoginMethod>> unlinked = new MutableLiveData<>();

    @Inject
    public SettingsViewModel(UserRepository userRepository, AppExecutors executors) {
        this.userRepository = userRepository;
        this.executors = executors;
        load();
    }

    public LiveData<Resource<List<Identity>>> getIdentities() {
        return identities;
    }

    public LiveData<Boolean> isBusy() {
        return busy;
    }

    public LiveData<Event<String>> getOpenUrl() {
        return openUrl;
    }

    public LiveData<Event<ApiError>> getFailure() {
        return failure;
    }

    public LiveData<Event<LoginMethod>> getLinked() {
        return linked;
    }

    public LiveData<Event<LoginMethod>> getUnlinked() {
        return unlinked;
    }

    public void load() {
        identities.setValue(Resource.loading());
        executors.networkIO().execute(() -> identities.postValue(userRepository.identities()));
    }

    public void linkGoogle(String idToken) {
        runListChange(() -> userRepository.linkGoogle(idToken), LoginMethod.GOOGLE, linked);
    }

    public void unlink(LoginMethod method) {
        runListChange(() -> userRepository.unlink(method), method, unlinked);
    }

    /** Meminta URL login GitHub khusus penyambungan; hasilnya datang lewat deep link ?result=linked. */
    public void startLinkGitHub() {
        busy.setValue(true);
        executors.networkIO().execute(() -> {
            Resource<String> url = userRepository.linkGitHubUrl();
            busy.postValue(false);
            if (url.getStatus() == Resource.Status.SUCCESS) {
                openUrl.postValue(new Event<>(url.getData()));
            } else {
                failure.postValue(new Event<>(url.getError()));
            }
        });
    }

    /** Deep link setelah menyambungkan GitHub: ?result=linked atau ?error=IDENTITY_IN_USE / KODE lain. */
    public void onGitHubCallback(Uri uri) {
        if ("linked".equals(uri.getQueryParameter("result"))) {
            linked.setValue(new Event<>(LoginMethod.GITHUB));
            load();
        } else if (uri.getQueryParameter("error") != null) {
            failure.setValue(new Event<>(ApiError.of(uri.getQueryParameter("error"))));
        }
    }

    private void runListChange(java.util.function.Supplier<Resource<List<Identity>>> request, LoginMethod method,
                               MutableLiveData<Event<LoginMethod>> onSuccess) {
        busy.setValue(true);
        executors.networkIO().execute(() -> {
            Resource<List<Identity>> result = request.get();
            busy.postValue(false);
            if (result.getStatus() == Resource.Status.SUCCESS) {
                identities.postValue(result);
                onSuccess.postValue(new Event<>(method));
            } else {
                failure.postValue(new Event<>(result.getError()));
            }
        });
    }
}
