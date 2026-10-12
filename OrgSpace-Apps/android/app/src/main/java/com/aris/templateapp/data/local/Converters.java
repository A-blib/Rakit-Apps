package com.aris.templateapp.data.local;

import androidx.room.TypeConverter;

import com.aris.templateapp.data.model.ProjectMode;
import com.aris.templateapp.data.model.ProjectStatus;

/** Mengubah enum ↔ teks saat menulis/membaca database (SQLite hanya kenal angka, teks, dan blob). */
public final class Converters {

    private Converters() {
    }

    @TypeConverter
    public static String fromMode(ProjectMode mode) {
        return mode == null ? null : mode.value();
    }

    @TypeConverter
    public static ProjectMode toMode(String value) {
        return value == null ? null : ProjectMode.fromValue(value);
    }

    @TypeConverter
    public static String fromStatus(ProjectStatus status) {
        return status == null ? null : status.value();
    }

    @TypeConverter
    public static ProjectStatus toStatus(String value) {
        return value == null ? null : ProjectStatus.fromValue(value);
    }
}
