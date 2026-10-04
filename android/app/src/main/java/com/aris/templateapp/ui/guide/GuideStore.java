package com.aris.templateapp.ui.guide;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Mengingat panduan mana yang sudah pernah dibuka (penanda titik "baru", bagian 3.9) di HP ini.
 * Dipakai juga oleh checklist provider baru: langkah "Baca panduan" selesai jika ada panduan yang dibuka.
 */
@Singleton
public class GuideStore {

    private static final String PREFS = "provider_guides";
    private static final String KEY_PREFIX = "opened_";

    private final SharedPreferences prefs;

    @Inject
    public GuideStore(@ApplicationContext Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isOpened(Guide guide) {
        return prefs.getBoolean(KEY_PREFIX + guide.name(), false);
    }

    /**
     * Ada panduan untuk dashboard itu yang sudah dibuka. Hanya panduan yang sudah berisi yang dihitung
     * (membuka panduan "Segera hadir" tidak menyelesaikan checklist provider).
     */
    public boolean anyOpened(Guide.Audience audience) {
        for (Guide guide : Guide.forAudience(audience)) {
            if (guide.available && isOpened(guide)) {
                return true;
            }
        }
        return false;
    }

    public void markOpened(Guide guide) {
        prefs.edit().putBoolean(KEY_PREFIX + guide.name(), true).apply();
    }
}
