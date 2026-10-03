package com.aris.templateapp.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.AuthResult;
import com.aris.templateapp.data.repository.AuthRepository;

import java.util.Map;
import java.util.function.Supplier;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * State layar Masuk & Daftar. Memvalidasi form dulu (tanpa jaringan), lalu memanggil AuthRepository
 * di thread latar. Layar mengamati:
 * - {@link #getFormErrors()} → pesan error per field
 * - {@link #getState()}      → LOADING / ERROR (sebagai Event, agar snackbar tidak muncul ulang saat layar diputar)
 * - {@link #getSuccess()}    → berhasil masuk/daftar (Event, untuk pindah layar sekali saja)
 */
@HiltViewModel
public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final AppExecutors executors;

    private final MutableLiveData<Map<AuthFormValidator.Field, Integer>> formErrors = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<Resource<AuthResult>>> failure = new MutableLiveData<>();
    private final MutableLiveData<Event<AuthResult>> success = new MutableLiveData<>();

    /** Request terakhir, disimpan agar tombol "Coba lagi" bisa mengulanginya. */
    private Supplier<Resource<AuthResult>> lastRequest;

    @Inject
    public AuthViewModel(AuthRepository authRepository, AppExecutors executors) {
        this.authRepository = authRepository;
        this.executors = executors;
    }

    public LiveData<Map<AuthFormValidator.Field, Integer>> getFormErrors() {
        return formErrors;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    public LiveData<Event<Resource<AuthResult>>> getFailure() {
        return failure;
    }

    public LiveData<Event<AuthResult>> getSuccess() {
        return success;
    }

    public void login(String email, String password) {
        Map<AuthFormValidator.Field, Integer> errors = AuthFormValidator.validateLogin(email, password);
        formErrors.setValue(errors);
        if (errors.isEmpty()) {
            run(() -> authRepository.login(email.trim(), password, null));
        }
    }

    public void register(String name, String email, String password) {
        Map<AuthFormValidator.Field, Integer> errors = AuthFormValidator.validateRegister(name, email, password);
        formErrors.setValue(errors);
        if (errors.isEmpty()) {
            run(() -> authRepository.register(name.trim(), email.trim(), password));
        }
    }

    /** Untuk tombol "Coba lagi" setelah gagal karena koneksi. */
    public void retry() {
        if (lastRequest != null) {
            run(lastRequest);
        }
    }

    private void run(Supplier<Resource<AuthResult>> request) {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return; // Cegah request ganda jika tombol ditekan berkali-kali.
        }
        lastRequest = request;
        loading.setValue(true);
        executors.networkIO().execute(() -> {
            Resource<AuthResult> result = request.get();
            loading.postValue(false);
            if (result.getStatus() == Resource.Status.SUCCESS) {
                success.postValue(new Event<>(result.getData()));
            } else {
                failure.postValue(new Event<>(result));
            }
        });
    }
}
