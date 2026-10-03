package com.aris.templateapp.data.repository;

import androidx.annotation.WorkerThread;

import com.aris.templateapp.core.network.ApiErrorParser;
import com.aris.templateapp.core.storage.SessionStore;
import com.aris.templateapp.core.storage.TokenStorage;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.mapper.UserMapper;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.data.remote.api.UserApi;
import com.aris.templateapp.data.remote.dto.UserDto;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

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
        try {
            Response<UserDto> response = userApi.me().execute();
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
}
