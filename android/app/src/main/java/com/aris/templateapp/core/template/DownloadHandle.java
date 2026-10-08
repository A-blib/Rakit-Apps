package com.aris.templateapp.core.template;

import androidx.annotation.Nullable;

import retrofit2.Call;

/**
 * Pegangan unduhan yang bisa dibatalkan dari thread UI. Membatalkan juga memutus request yang sedang menunggu data,
 * sehingga tombol Batal langsung berhenti tanpa menunggu batas waktu jaringan.
 */
public class DownloadHandle {

    private volatile boolean cancelled;
    @Nullable
    private volatile Call<?> call;

    public void attach(Call<?> call) {
        this.call = call;
        if (cancelled) {
            call.cancel();
        }
    }

    public void cancel() {
        cancelled = true;
        Call<?> current = call;
        if (current != null) {
            current.cancel();
        }
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
