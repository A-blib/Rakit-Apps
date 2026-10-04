package com.aris.templateapp.core.di;

import android.content.Context;

import androidx.room.Room;

import com.aris.templateapp.data.local.AppDatabase;
import com.aris.templateapp.data.local.ProjectDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

/** Satu objek database untuk seluruh app (membuka database itu mahal). */
@Module
@InstallIn(SingletonComponent.class)
public final class DatabaseModule {

    private DatabaseModule() {
    }

    @Provides
    @Singleton
    static AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, AppDatabase.NAME).build();
    }

    @Provides
    static ProjectDao provideProjectDao(AppDatabase database) {
        return database.projectDao();
    }
}
