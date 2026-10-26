package com.aris.templateapp.data.local;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 * Database SQLite di HP (Room). Naikkan {@code version} dan tulis migrasi setiap kali struktur tabel berubah;
 * riwayat strukturnya tersimpan di {@code app/schemas/}.
 */
@Database(entities = {ProjectEntity.class, TemplatePackageEntity.class, PendingEventEntity.class,
        WebsiteEntity.class}, version = 4,
        // Versi 2 (editor template mode), 3 (cache tab Website), dan 4 (projects.server_project_id) hanya menambah
        // tabel dan kolom yang boleh kosong, jadi Room bisa membuat migrasinya sendiri dari riwayat di app/schemas/.
        // Data project lama tetap utuh.
        autoMigrations = {@AutoMigration(from = 1, to = 2), @AutoMigration(from = 2, to = 3),
                @AutoMigration(from = 3, to = 4)})
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    public static final String NAME = "rakit.db";

    public abstract ProjectDao projectDao();

    public abstract TemplatePackageDao templatePackageDao();

    public abstract PendingEventDao pendingEventDao();

    public abstract WebsiteDao websiteDao();
}
