package com.aris.templateapp.core.di;

import com.aris.templateapp.BuildConfig;
import com.aris.templateapp.core.network.AuthInterceptor;
import com.aris.templateapp.core.network.TokenAuthenticator;
import com.aris.templateapp.data.remote.api.AuthApi;
import com.aris.templateapp.data.remote.api.ProviderApi;
import com.aris.templateapp.data.remote.api.UserApi;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Menyiapkan OkHttp (pengirim HTTP) dan Retrofit (pengubah interface Java menjadi request HTTP).
 * <pre>
 * Request app ─► AuthInterceptor (tempel token) ─► backend
 *                       401? ─► TokenAuthenticator (refresh lewat client terpisah) ─► ulangi request
 * </pre>
 */
@Module
@InstallIn(SingletonComponent.class)
public final class NetworkModule {

    private static final long TIMEOUT_SECONDS = 15;

    private NetworkModule() {
    }

    @Provides
    @Singleton
    static OkHttpClient provideOkHttpClient(AuthInterceptor authInterceptor, TokenAuthenticator tokenAuthenticator) {
        return baseClient()
                .addInterceptor(authInterceptor)
                .authenticator(tokenAuthenticator)
                .build();
    }

    @Provides
    @Singleton
    static Retrofit provideRetrofit(OkHttpClient client, Gson gson) {
        return retrofit(client, gson);
    }

    @Provides
    @Singleton
    static AuthApi provideAuthApi(Retrofit retrofit) {
        return retrofit.create(AuthApi.class);
    }

    @Provides
    @Singleton
    static UserApi provideUserApi(Retrofit retrofit) {
        return retrofit.create(UserApi.class);
    }

    @Provides
    @Singleton
    static ProviderApi provideProviderApi(Retrofit retrofit) {
        return retrofit.create(ProviderApi.class);
    }

    /** AuthApi khusus TokenAuthenticator: tanpa interceptor & authenticator agar tidak memanggil dirinya sendiri. */
    @Provides
    @Singleton
    @RefreshClient
    static AuthApi provideRefreshAuthApi(Gson gson) {
        return retrofit(baseClient().build(), gson).create(AuthApi.class);
    }

    private static OkHttpClient.Builder baseClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private static Retrofit retrofit(OkHttpClient client, Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
}
