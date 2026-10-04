package com.aris.templateapp;

import android.app.Application;

import com.aris.templateapp.core.storage.ThemeStore;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

/**
 * Class Application: dibuat pertama kali saat app dijalankan, sebelum Activity mana pun.
 * {@code @HiltAndroidApp} menyalakan Hilt sehingga dependency (Retrofit, TokenStorage, dst.)
 * bisa disuntikkan ke Activity, Fragment, dan ViewModel.
 */
@HiltAndroidApp
public class TemplateApp extends Application {

    @Inject
    ThemeStore themeStore;

    @Override
    public void onCreate() {
        // Hilt mengisi field @Inject di dalam super.onCreate().
        super.onCreate();
        // Tema pilihan user (Pengaturan → Tema) dipasang sebelum layar pertama digambar, agar tidak berkedip.
        themeStore.applySaved();
    }
}
