package com.aris.templateapp.core.template;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aris.templateapp.data.repository.TemplateEventRepository;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;

/**
 * Pekerjaan WorkManager: kirim antrean event statistik. WorkManager menjalankannya saat HP online, bahkan setelah
 * app ditutup, dan mengulanginya nanti jika masih gagal.
 * <p>
 * Worker dibuat WorkManager (bukan Hilt), jadi repository diambil lewat {@link EntryPoint}. Cara ini tidak butuh
 * library tambahan {@code androidx.hilt:hilt-work}.
 */
public class SendEventsWorker extends Worker {

    @EntryPoint
    @InstallIn(SingletonComponent.class)
    public interface Dependencies {
        TemplateEventRepository templateEventRepository();
    }

    public SendEventsWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        TemplateEventRepository repository = EntryPointAccessors
                .fromApplication(getApplicationContext(), Dependencies.class)
                .templateEventRepository();
        return repository.sendPending() ? Result.success() : Result.retry();
    }
}
