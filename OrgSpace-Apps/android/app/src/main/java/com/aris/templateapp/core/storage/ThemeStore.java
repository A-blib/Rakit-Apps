package com.aris.templateapp.core.storage;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Pilihan tema app: ikuti sistem (bawaan), selalu terang, atau selalu gelap. Disimpan di HP dan dipasang
 * lewat {@link AppCompatDelegate#setDefaultNightMode}, yang membuat semua warna {@code values-night/} dipakai
 * atau tidak, terlepas dari mode HP.
 */
@Singleton
public class ThemeStore {

    public enum Theme {
        SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
        LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
        DARK(AppCompatDelegate.MODE_NIGHT_YES);

        public final int nightMode;

        Theme(int nightMode) {
            this.nightMode = nightMode;
        }
    }

    private static final String PREFS = "app_settings";
    private static final String KEY_THEME = "theme";

    private final SharedPreferences prefs;

    @Inject
    public ThemeStore(@ApplicationContext Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public Theme getTheme() {
        try {
            return Theme.valueOf(prefs.getString(KEY_THEME, Theme.SYSTEM.name()));
        } catch (IllegalArgumentException e) {
            return Theme.SYSTEM;
        }
    }

    /** Menyimpan lalu langsung menerapkan; Activity yang terbuka dibuat ulang otomatis dengan warna baru. */
    public void setTheme(Theme theme) {
        prefs.edit().putString(KEY_THEME, theme.name()).apply();
        AppCompatDelegate.setDefaultNightMode(theme.nightMode);
    }

    /** Dipanggil sekali saat app dibuka, sebelum Activity pertama dibuat. */
    public void applySaved() {
        AppCompatDelegate.setDefaultNightMode(getTheme().nightMode);
    }
}
