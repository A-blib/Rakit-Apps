package com.aris.templateapp;

import android.app.Application;

import com.aris.templateapp.core.storage.ThemeStore;
import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.data.repository.TemplateEventRepository;
import com.aris.templateapp.data.repository.TemplatePackageRepository;

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
    @Inject
    AppExecutors executors;
    @Inject
    TemplatePackageRepository templatePackages;
    @Inject
    TemplateEventRepository templateEvents;

    @Override
    public void onCreate() {
        // Hilt mengisi field @Inject di dalam super.onCreate().
        super.onCreate();
        // Tema pilihan user (Pengaturan → Tema) dipasang sebelum layar pertama digambar, agar tidak berkedip.
        themeStore.applySaved();
        // Paket template yang tidak dibuka 30 hari dan tidak dipakai project dihapus agar HP tidak penuh
        // (alur-buat-website-via-template.md bagian 11.1). Dikerjakan di latar, tidak memperlambat app dibuka.
        executors.diskIO().execute(templatePackages::deleteUnusedPackages);
        // Event statistik yang tertunda (mis. app ditutup saat offline) dijadwalkan ulang.
        templateEvents.scheduleSend();
    }
}
