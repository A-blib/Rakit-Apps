package com.aris.templateapp.data.repository;

import androidx.annotation.WorkerThread;

import com.aris.templateapp.core.network.ApiErrorParser;
import com.aris.templateapp.core.storage.SessionStore;
import com.aris.templateapp.core.storage.TokenStorage;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.mapper.UserMapper;
import com.aris.templateapp.data.model.Identity;
import com.aris.templateapp.data.model.LoginMethod;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.data.model.UserRole;
import com.aris.templateapp.data.remote.api.UserApi;
import com.aris.templateapp.data.remote.dto.ActiveModeRequestDto;
import com.aris.templateapp.data.remote.dto.CreatorOnboardingRequestDto;
import com.aris.templateapp.data.remote.dto.GoogleIdTokenRequestDto;
import com.aris.templateapp.data.remote.dto.IdentityDto;
import com.aris.templateapp.data.remote.dto.ProviderOnboardingRequestDto;
import com.aris.templateapp.data.remote.dto.UrlResponseDto;
import com.aris.templateapp.data.remote.dto.UserDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Sumber data user: backend (/users/me) dan salinan di HP (SessionStore).
 * <p>
 * Method bertanda {@code @WorkerThread} melakukan request secara langsung (blocking), jadi WAJIB dipanggil
 * dari thread latar (AppExecutors.networkIO()), bukan dari thread utama. ViewModel yang mengatur thread-nya.
 */
@Singleton
public class UserRepository {

    private final UserApi userApi;
    private final SessionStore sessionStore;
    private final TokenStorage tokenStorage;
    private final ApiErrorParser errorParser;

    @Inject
    public UserRepository(UserApi userApi, SessionStore sessionStore, TokenStorage tokenStorage,
                          ApiErrorParser errorParser) {
        this.userApi = userApi;
        this.sessionStore = sessionStore;
        this.tokenStorage = tokenStorage;
        this.errorParser = errorParser;
    }

    /** true jika ada token tersimpan (user pernah login dan belum keluar). */
    public boolean hasSession() {
        return tokenStorage.hasTokens();
    }

    /** Salinan user terakhir di HP; null jika tamu. */
    public User getCachedUser() {
        return hasSession() ? sessionStore.getCachedUser() : null;
    }

    /** Mengambil user terbaru dari backend dan memperbarui salinan di HP jika berhasil. */
    @WorkerThread
    public Resource<User> fetchMe() {
        return executeUser(userApi.me());
    }

    @WorkerThread
    public Resource<User> completeCreatorOnboarding(String displayName, String websitePurpose, String organizationName) {
        return executeUser(userApi.onboardCreator(
                new CreatorOnboardingRequestDto(displayName, websitePurpose, organizationName)));
    }

    @WorkerThread
    public Resource<User> becomeProvider(String creatorName, String bio, String portfolioUrl, List<String> specialties) {
        // Checkbox persetujuan sudah diwajibkan di form sebelum method ini dipanggil.
        return executeUser(userApi.onboardProvider(
                new ProviderOnboardingRequestDto(creatorName, bio, portfolioUrl, specialties, true)));
    }

    @WorkerThread
    public Resource<User> changeActiveMode(UserRole mode) {
        return executeUser(userApi.changeActiveMode(new ActiveModeRequestDto(mode.value())));
    }

    @WorkerThread
    public Resource<List<Identity>> identities() {
        return executeIdentities(userApi.identities());
    }

    @WorkerThread
    public Resource<List<Identity>> linkGoogle(String idToken) {
        return executeIdentities(userApi.linkGoogle(new GoogleIdTokenRequestDto(idToken)));
    }

    /** URL login GitHub untuk menyambungkan GitHub ke akun ini (hasilnya lewat deep link ?result=linked). */
    @WorkerThread
    public Resource<String> linkGitHubUrl() {
        try {
            Response<UrlResponseDto> response = userApi.linkGitHubUrl().execute();
            UrlResponseDto body = response.body();
            return response.isSuccessful() && body != null && body.url != null
                    ? Resource.success(body.url) : Resource.error(errorParser.parse(response));
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }

    /** Melepas metode login; mengembalikan daftar terbaru. */
    @WorkerThread
    public Resource<List<Identity>> unlink(LoginMethod method) {
        try {
            Response<Void> response = userApi.unlink(method.value()).execute();
            return response.isSuccessful() ? identities() : Resource.error(errorParser.parse(response));
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }

    /** Menjalankan request yang membalas UserResponse dan memperbarui salinan user di HP. */
    private Resource<User> executeUser(Call<UserDto> call) {
        try {
            Response<UserDto> response = call.execute();
            UserDto body = response.body();
            if (response.isSuccessful() && body != null) {
                sessionStore.saveUser(body);
                return Resource.success(UserMapper.toModel(body));
            }
            return Resource.error(errorParser.parse(response));
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }

    private Resource<List<Identity>> executeIdentities(Call<List<IdentityDto>> call) {
        try {
            Response<List<IdentityDto>> response = call.execute();
            List<IdentityDto> body = response.body();
            if (!response.isSuccessful() || body == null) {
                return Resource.error(errorParser.parse(response));
            }
            List<Identity> identities = new ArrayList<>();
            for (IdentityDto dto : body) {
                LoginMethod method = LoginMethod.fromValue(dto.provider);
                if (method != null) {
                    identities.add(new Identity(method, dto.email));
                }
            }
            return Resource.success(identities);
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }
}
