package com.aris.templateapp.ui.auth;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.AuthResult;
import com.aris.templateapp.data.model.LoginMethod;
import com.aris.templateapp.data.repository.AuthRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * State layar Masuk & Daftar: email, Google, GitHub, dan penyambungan akun.
 * <p>
 * Layar mengamati:
 * - {@link #getFormErrors()}   → pesan error per field
 * - {@link #isLoading()}       → tombol nonaktif selama request
 * - {@link #getFailure()}      → snackbar error (Event)
 * - {@link #getSuccess()}      → berhasil masuk (Event, untuk pindah layar sekali saja)
 * - {@link #getOpenUrl()}      → buka URL login GitHub di Custom Tab (Event)
 * - {@link #getLinkRequired()} → tampilkan LinkAccountDialog (Event)
 * - {@link #getContinueWith()} → user memilih metode lama di dialog; layar menjalankan metode itu (Event)
 * - {@link #getPendingLink()}  → penyambungan yang sedang berlangsung (untuk banner info)
 */
@HiltViewModel
public class AuthViewModel extends ViewModel {

    private static final String ACCOUNT_LINK_REQUIRED = "ACCOUNT_LINK_REQUIRED";

    private final AuthRepository authRepository;
    private final AppExecutors executors;

    private final MutableLiveData<Map<AuthFormValidator.Field, Integer>> formErrors = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<Resource<AuthResult>>> failure = new MutableLiveData<>();
    private final MutableLiveData<Event<AuthResult>> success = new MutableLiveData<>();
    private final MutableLiveData<Event<String>> openUrl = new MutableLiveData<>();
    private final MutableLiveData<Event<LinkRequest>> linkRequired = new MutableLiveData<>();
    private final MutableLiveData<Event<LoginMethod>> continueWith = new MutableLiveData<>();
    private final MutableLiveData<LinkRequest> pendingLink = new MutableLiveData<>();

    /** Permintaan penyambungan yang sedang ditampilkan dialog (agar dialog bisa dibuat ulang setelah layar diputar). */
    private LinkRequest linkCandidate;

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

    public LiveData<Event<String>> getOpenUrl() {
        return openUrl;
    }

    public LiveData<Event<LinkRequest>> getLinkRequired() {
        return linkRequired;
    }

    public LiveData<Event<LoginMethod>> getContinueWith() {
        return continueWith;
    }

    public LiveData<LinkRequest> getPendingLink() {
        return pendingLink;
    }

    // ---------- email ----------

    public void login(String email, String password) {
        Map<AuthFormValidator.Field, Integer> errors = AuthFormValidator.validateLogin(email, password);
        formErrors.setValue(errors);
        if (errors.isEmpty()) {
            String linkToken = linkTokenFor(LoginMethod.LOCAL);
            run(LoginMethod.LOCAL, () -> authRepository.login(email.trim(), password, linkToken));
        }
    }

    public void register(String name, String email, String password) {
        Map<AuthFormValidator.Field, Integer> errors = AuthFormValidator.validateRegister(name, email, password);
        formErrors.setValue(errors);
        if (errors.isEmpty()) {
            run(LoginMethod.LOCAL, () -> authRepository.register(name.trim(), email.trim(), password));
        }
    }

    // ---------- Google ----------

    /** Dipanggil setelah Credential Manager memberikan idToken. */
    public void signInWithGoogle(String idToken) {
        String linkToken = linkTokenFor(LoginMethod.GOOGLE);
        run(LoginMethod.GOOGLE, () -> authRepository.loginWithGoogle(idToken, linkToken));
    }

    // ---------- GitHub ----------

    /** Meminta URL login GitHub ke backend, lalu layar membukanya di Custom Tab. */
    public void startGitHub() {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }
        String linkToken = linkTokenFor(LoginMethod.GITHUB);
        loading.setValue(true);
        executors.networkIO().execute(() -> {
            Resource<String> url = authRepository.gitHubAuthorizeUrl(linkToken);
            loading.postValue(false);
            if (url.getStatus() == Resource.Status.SUCCESS) {
                openUrl.postValue(new Event<>(url.getData()));
            } else {
                failure.postValue(new Event<>(Resource.error(url.getError())));
            }
        });
    }

    /**
     * Deep link dari backend setelah login GitHub:
     * ?ticket=... (berhasil), ?error=ACCOUNT_LINK_REQUIRED&linkToken=..&methods=google, atau ?error=KODE.
     */
    public void onGitHubCallback(Uri uri) {
        String ticket = uri.getQueryParameter("ticket");
        String error = uri.getQueryParameter("error");
        if (ticket != null) {
            run(LoginMethod.GITHUB, () -> authRepository.exchangeGitHubTicket(ticket));
        } else if (ACCOUNT_LINK_REQUIRED.equals(error)) {
            String methods = uri.getQueryParameter("methods");
            linkRequired.setValue(new Event<>(new LinkRequest(uri.getQueryParameter("linkToken"),
                    methods == null ? List.of() : Arrays.asList(methods.split(",")), LoginMethod.GITHUB)));
        } else if (error != null) {
            failure.setValue(new Event<>(Resource.error(ApiError.of(error))));
        }
    }

    // ---------- penyambungan akun ----------

    void rememberLinkCandidate(LinkRequest request) {
        linkCandidate = request;
    }

    LinkRequest getPendingLinkCandidate() {
        return linkCandidate;
    }

    /** User memilih masuk dengan metode lama di LinkAccountDialog. */
    public void continueLinkWith(LinkRequest request, LoginMethod method) {
        linkCandidate = null;
        pendingLink.setValue(request);
        continueWith.setValue(new Event<>(method));
    }

    public void cancelLink() {
        linkCandidate = null;
        pendingLink.setValue(null);
    }

    /** linkToken hanya dikirim jika user sedang menyambungkan dan masuk dengan salah satu metode lama. */
    private String linkTokenFor(LoginMethod method) {
        LinkRequest link = pendingLink.getValue();
        return link != null && link.getExistingMethods().contains(method) ? link.getLinkToken() : null;
    }

    /** Untuk tombol "Coba lagi" setelah gagal karena koneksi. */
    public void retry() {
        if (lastRequest != null) {
            run(null, lastRequest);
        }
    }

    /** @param method metode login request ini; dipakai jika backend meminta penyambungan akun. */
    private void run(LoginMethod method, Supplier<Resource<AuthResult>> request) {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return; // Cegah request ganda jika tombol ditekan berkali-kali.
        }
        lastRequest = request;
        loading.setValue(true);
        executors.networkIO().execute(() -> {
            Resource<AuthResult> result = request.get();
            loading.postValue(false);
            if (result.getStatus() == Resource.Status.SUCCESS) {
                pendingLink.postValue(null);
                success.postValue(new Event<>(result.getData()));
            } else if (result.getError() != null && ACCOUNT_LINK_REQUIRED.equals(result.getError().getCode())) {
                // Email akun ini sudah dipakai akun lain: minta user membuktikan kepemilikan akun lama.
                linkRequired.postValue(new Event<>(new LinkRequest(result.getError().getLinkToken(),
                        result.getError().getExistingMethods(), method)));
            } else {
                failure.postValue(new Event<>(result));
            }
        });
    }
}
