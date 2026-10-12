package com.aris.templateapp.core.network;

import androidx.annotation.NonNull;

import com.aris.templateapp.core.storage.TokenStorage;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Menempelkan header {@code Authorization: Bearer <accessToken>} ke setiap request, kecuali endpoint
 * {@code /auth/*} (daftar, masuk, refresh) yang memang tidak butuh token.
 */
@Singleton
public class AuthInterceptor implements Interceptor {

    static final String HEADER = "Authorization";
    static final String BEARER = "Bearer ";
    private static final String AUTH_PATH = "/api/auth/";

    private final TokenStorage tokenStorage;

    @Inject
    public AuthInterceptor(TokenStorage tokenStorage) {
        this.tokenStorage = tokenStorage;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        if (!needsToken(request.url().encodedPath())) {
            return chain.proceed(request);
        }
        String token = tokenStorage.getAccessToken();
        if (token == null) {
            return chain.proceed(request);
        }
        return chain.proceed(request.newBuilder().header(HEADER, BEARER + token).build());
    }

    /** true untuk semua path kecuali /api/auth/... */
    static boolean needsToken(String encodedPath) {
        return !encodedPath.contains(AUTH_PATH);
    }
}
