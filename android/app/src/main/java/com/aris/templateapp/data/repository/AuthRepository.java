package com.aris.templateapp.data.repository;

import androidx.annotation.WorkerThread;

import com.aris.templateapp.core.network.ApiErrorParser;
import com.aris.templateapp.core.storage.SessionStore;
import com.aris.templateapp.core.storage.TokenStorage;
import com.aris.templateapp.core.util.Resource;
import com.aris.templateapp.data.mapper.UserMapper;
import com.aris.templateapp.data.model.AuthResult;
import com.aris.templateapp.data.remote.api.AuthApi;
import com.aris.templateapp.data.remote.dto.AuthResponseDto;
import com.aris.templateapp.data.remote.dto.LoginRequestDto;
import com.aris.templateapp.data.remote.dto.RegisterRequestDto;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Daftar, masuk, dan keluar. Setelah berhasil, token disimpan terenkripsi (TokenStorage) dan salinan user
 * disimpan (SessionStore), sehingga app langsung masuk saat dibuka lagi (skenario 3).
 * Semua method {@code @WorkerThread}: panggil dari thread latar.
 */
@Singleton
public class AuthRepository {

    private final AuthApi authApi;
    private final TokenStorage tokenStorage;
    private final SessionStore sessionStore;
    private final ApiErrorParser errorParser;

    @Inject
    public AuthRepository(AuthApi authApi, TokenStorage tokenStorage, SessionStore sessionStore,
                          ApiErrorParser errorParser) {
        this.authApi = authApi;
        this.tokenStorage = tokenStorage;
        this.sessionStore = sessionStore;
        this.errorParser = errorParser;
    }

    @WorkerThread
    public Resource<AuthResult> register(String displayName, String email, String password) {
        return execute(authApi.register(new RegisterRequestDto(displayName, email, password)));
    }

    /** @param linkToken null untuk masuk biasa; diisi saat menyambungkan akun Google/GitHub tertunda. */
    @WorkerThread
    public Resource<AuthResult> login(String email, String password, String linkToken) {
        return execute(authApi.login(new LoginRequestDto(email, password, linkToken)));
    }

    /** Menjalankan request auth dan menyimpan sesi jika berhasil. Dipakai juga oleh login Google/GitHub. */
    @WorkerThread
    public Resource<AuthResult> execute(Call<AuthResponseDto> call) {
        try {
            Response<AuthResponseDto> response = call.execute();
            AuthResponseDto body = response.body();
            if (response.isSuccessful() && body != null) {
                tokenStorage.saveTokens(body.accessToken, body.refreshToken);
                sessionStore.saveUser(body.user);
                return Resource.success(new AuthResult(UserMapper.toModel(body.user), body.isNewUser));
            }
            return Resource.error(errorParser.parse(response));
        } catch (IOException e) {
            return Resource.error(errorParser.parse(e));
        }
    }
}
