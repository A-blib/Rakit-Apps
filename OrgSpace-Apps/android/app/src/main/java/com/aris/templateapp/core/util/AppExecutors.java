package com.aris.templateapp.core.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Kumpulan thread untuk pekerjaan di luar thread utama (UI).
 * <p>
 * Aturan Android: thread utama hanya untuk menggambar layar. Request jaringan atau baca/tulis penyimpanan
 * di thread utama membuat app macet ("Application Not Responding"), dan request jaringan bahkan langsung ditolak.
 */
@Singleton
public class AppExecutors {

    private final ExecutorService diskIO;
    private final ExecutorService networkIO;
    private final Executor mainThread;

    @Inject
    public AppExecutors() {
        this.diskIO = Executors.newSingleThreadExecutor();
        this.networkIO = Executors.newFixedThreadPool(3);
        Handler mainHandler = new Handler(Looper.getMainLooper());
        this.mainThread = mainHandler::post;
    }

    /** Untuk baca/tulis SharedPreferences atau file. Satu thread agar urutan tulis terjaga. */
    public ExecutorService diskIO() {
        return diskIO;
    }

    /** Untuk request ke backend. */
    public ExecutorService networkIO() {
        return networkIO;
    }

    /** Untuk kembali ke thread utama, mis. memperbarui tampilan. */
    public Executor mainThread() {
        return mainThread;
    }
}
