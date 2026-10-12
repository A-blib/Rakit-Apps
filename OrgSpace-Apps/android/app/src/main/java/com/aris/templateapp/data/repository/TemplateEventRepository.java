package com.aris.templateapp.data.repository;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.aris.templateapp.core.storage.SessionStore;
import com.aris.templateapp.core.template.SendEventsWorker;
import com.aris.templateapp.core.util.AppExecutors;
import com.aris.templateapp.data.local.PendingEventDao;
import com.aris.templateapp.data.local.PendingEventEntity;
import com.aris.templateapp.data.remote.api.GalleryApi;
import com.aris.templateapp.data.remote.dto.TemplateEventDto;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Response;

/**
 * Event statistik provider "Dilihat" dan "Didownload" (alur-buat-website-via-template.md bagian 10). Setiap event
 * masuk antrean {@code pending_events} lebih dulu, lalu dikirim WorkManager begitu ada internet. Dengan begitu
 * export saat offline tetap terhitung setelah HP online, dan membuka editor tidak pernah menunggu jaringan.
 */
@Singleton
public class TemplateEventRepository {

    public static final String VIEW = TemplateEventDto.VIEW;
    public static final String DOWNLOAD = TemplateEventDto.DOWNLOAD;

    private static final String WORK_NAME = "kirim-event-template";
    private static final int BATCH = 20;
    /** Event yang terus gagal (bukan karena offline) dibuang setelah sekian kali agar antrean tidak macet. */
    static final int MAX_ATTEMPTS = 5;

    private final Context context;
    private final PendingEventDao dao;
    private final GalleryApi api;
    private final SessionStore sessionStore;
    private final AppExecutors executors;

    @Inject
    public TemplateEventRepository(@ApplicationContext Context context, PendingEventDao dao, GalleryApi api,
                                   SessionStore sessionStore, AppExecutors executors) {
        this.context = context;
        this.dao = dao;
        this.api = api;
        this.sessionStore = sessionStore;
        this.executors = executors;
    }

    /** @param projectId wajib untuk {@link #DOWNLOAD} */
    public void record(String templateId, String type, @Nullable String projectId) {
        executors.diskIO().execute(() -> {
            PendingEventEntity event = new PendingEventEntity(UUID.randomUUID().toString(), templateId, type,
                    Instant.now().toString());
            event.projectId = projectId;
            dao.insert(event);
            scheduleSend();
        });
    }

    /** Menjadwalkan pengiriman saat ada koneksi. APPEND: event baru tidak membatalkan pengiriman yang sedang jalan. */
    public void scheduleSend() {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(SendEventsWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request);
    }

    /**
     * Mengirim semua event di antrean. Server menjawab 202 juga untuk event yang diabaikan (ganda, dari pemilik
     * template), jadi app tidak perlu tahu aturan itu.
     *
     * @return false jika ada event yang perlu dicoba lagi nanti (offline/server error)
     */
    @WorkerThread
    public boolean sendPending() {
        String installId = sessionStore.getInstallId();
        while (true) {
            List<PendingEventEntity> batch = dao.oldest(BATCH);
            if (batch.isEmpty()) {
                return true;
            }
            for (PendingEventEntity event : batch) {
                try {
                    Response<Void> response = api.recordEvent(event.templateId,
                            new TemplateEventDto(event.type, event.projectId, installId, event.occurredAt)).execute();
                    if (response.isSuccessful() || response.code() < 500) {
                        // 4xx (template dihapus, data tidak valid) tidak akan berhasil jika diulang.
                        dao.delete(event.id);
                    } else if (!retryLater(event)) {
                        return false;
                    }
                } catch (IOException e) {
                    return false;
                }
            }
        }
    }

    private boolean retryLater(PendingEventEntity event) {
        if (event.attempts + 1 >= MAX_ATTEMPTS) {
            dao.delete(event.id);
            return true;
        }
        dao.incrementAttempts(event.id);
        return false;
    }
}
