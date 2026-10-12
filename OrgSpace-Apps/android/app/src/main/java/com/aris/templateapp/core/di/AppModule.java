package com.aris.templateapp.core.di;

import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/**
 * Module Hilt untuk objek umum app. {@code @Provides} dipakai untuk objek dari library yang
 * tidak bisa kita beri {@code @Inject} pada konstruktornya (mis. Gson).
 * {@code SingletonComponent}: objek hidup selama app berjalan (satu untuk semua).
 */
@Module
@InstallIn(SingletonComponent.class)
public final class AppModule {

    private AppModule() {
    }

    @Provides
    @Singleton
    static Gson provideGson() {
        return new Gson();
    }
}
