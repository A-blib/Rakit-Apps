package com.aris.templateapp;

import android.app.Application;

import dagger.hilt.android.HiltAndroidApp;

/**
 * Class Application: dibuat pertama kali saat app dijalankan, sebelum Activity mana pun.
 * {@code @HiltAndroidApp} menyalakan Hilt sehingga dependency (Retrofit, TokenStorage, dst.)
 * bisa disuntikkan ke Activity, Fragment, dan ViewModel.
 */
@HiltAndroidApp
public class TemplateApp extends Application {
}
