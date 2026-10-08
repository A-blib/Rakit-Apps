package com.aris.templateapp.core.storage;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/** Tur singkat editor Tandai hanya tampil pertama kali (alur-fitur-upload.md bagian 7.10 A6). */
@Singleton
public class EditorTourStore {

    private static final String PREFS = "editor_tour";
    private static final String KEY_MARK = "mark_seen";

    private final SharedPreferences prefs;

    @Inject
    public EditorTourStore(@ApplicationContext Context context) {
        this.prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean seen() {
        return prefs.getBoolean(KEY_MARK, false);
    }

    public void markSeen() {
        prefs.edit().putBoolean(KEY_MARK, true).apply();
    }
}
