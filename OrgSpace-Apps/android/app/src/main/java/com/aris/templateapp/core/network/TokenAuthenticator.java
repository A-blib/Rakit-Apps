package com.aris.templateapp.core.network;

import android.util.Log;

import androidx.annotation.Nullable;

import com.aris.templateapp.core.di.RefreshClient;
import com.aris.templateapp.core.storage.SessionStore;
import com.aris.templateapp.core.storage.TokenStorage;
import com.aris.templateapp.data.remote.api.AuthApi;
import com.aris.templateapp.data.remote.dto.AuthResponseDto;
import com.aris.templateapp.data.remote.dto.RefreshTokenRequestDto;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Dipanggil OkHttp otomatis setiap kali backend membalas <b>401</b>. Tugasnya: tukar refresh token dengan
 * access token baru, lalu ulangi request yang gagal, tanpa user sadar (skenario 4 bagian 13.2).
 * <p>
 * {@code synchronized}: jika 3 request gagal 401 bersamaan, hanya request pertama yang memanggil /auth/refresh.
 * Request lain menunggu, lalu melihat token sudah berganti dan langsung mengulang dengan token baru.
 * Ini penting karena refresh token dirotasi: memakai token lama dua kali dianggap pencurian oleh backend
 * dan semua sesi dicabut.
 * <p>
 * Refresh memakai {@link RefreshClient} (OkHttp terpisah tanpa authenticator) agar 401 dari /auth/refresh
 * tidak memanggil authenticator ini lagi tanpa akhir.
 */
@Singleton
public class TokenAuthenticator implements Authenticator {

    private static final String TAG = "TokenAuthenticator";

    private final TokenStorage tokenStorage;
    private final SessionStore sessionStore;
    private final AuthApi refreshApi;

    @Inject
    public TokenAuthenticator(TokenStorage tokenStorage, SessionStore sessionStore, @RefreshClient AuthApi refreshApi) {
        this.tokenStorage = tokenStorage;
        this.sessionStore = sessionStore;
        this.refreshApi = refreshApi;
    }

    @Nullable
    @Override
    public synchronized Request authenticate(@Nullable Route route, Response response) {
        String failedToken = bearerToken(response.request());
        // Request tanpa token (mis. login dengan password salah) memang seharusnya 401; tidak perlu refresh.
        if (failedToken == null) {
            return null;
        }
        // Sudah dicoba ulang dengan token baru tetapi tetap 401: berhenti agar tidak berputar terus.
        if (response.priorResponse() != null) {
            return null;
        }

        String currentToken = tokenStorage.getAccessToken();
        if (currentToken != null && !currentToken.equals(failedToken)) {
            // Thread lain baru saja me-refresh selagi kita menunggu: cukup ulangi dengan token terbaru.
            return withToken(response.request(), currentToken);
        }

        String refreshToken = tokenStorage.getRefreshToken();
        if (refreshToken == null) {
            endSession();
            return null;
        }
        try {
            retrofit2.Response<AuthResponseDto> refreshed =
                    refreshApi.refresh(new RefreshTokenRequestDto(refreshToken)).execute();
            AuthResponseDto body = refreshed.body();
            if (refreshed.isSuccessful() && body != null) {
                tokenStorage.saveTokens(body.accessToken, body.refreshToken);
                sessionStore.saveUser(body.user);
                return withToken(response.request(), body.accessToken);
            }
            // 401/400 = refresh token ditolak (kedaluwarsa, dicabut, atau dipakai ulang): sesi selesai.
            if (refreshed.code() == 401 || refreshed.code() == 400) {
                endSession();
            }
            return null;
        } catch (IOException e) {
            // Offline: biarkan token tersimpan; request ini gagal sebagai error jaringan biasa.
            Log.w(TAG, "Refresh token gagal karena jaringan");
            return null;
        }
    }

    private void endSession() {
        tokenStorage.clear();
        sessionStore.clearUser();
        sessionStore.notifySessionExpired();
    }

    private static String bearerToken(Request request) {
        String header = request.header(AuthInterceptor.HEADER);
        return header != null && header.startsWith(AuthInterceptor.BEARER)
                ? header.substring(AuthInterceptor.BEARER.length()) : null;
    }

    private static Request withToken(Request request, String token) {
        return request.newBuilder().header(AuthInterceptor.HEADER, AuthInterceptor.BEARER + token).build();
    }
}
