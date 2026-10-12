package com.aris.templateapp.core.upload;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Cadangan otomatis tandaan yang belum disimpan (alur-fitur-upload.md bagian 7.12). Jika app tertutup atau HP mati,
 * saat editor dibuka lagi provider ditanya "Ada perubahan yang belum disimpan. Pulihkan?".
 */
@Singleton
public class MarkingBackupStore {

    private static final String PREFS = "marking_backup";

    private final SharedPreferences prefs;

    @Inject
    public MarkingBackupStore(@ApplicationContext Context context) {
        this.prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** @param json data tandaan dalam bentuk JSON */
    public void save(String templateId, String json) {
        prefs.edit().putString(templateId, json).apply();
    }

    @Nullable
    public String load(String templateId) {
        return prefs.getString(templateId, null);
    }

    public void clear(String templateId) {
        prefs.edit().remove(templateId).apply();
    }
}
