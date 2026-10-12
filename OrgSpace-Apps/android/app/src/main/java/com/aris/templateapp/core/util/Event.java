package com.aris.templateapp.core.util;

/**
 * Pembungkus data yang hanya boleh "dipakai" sekali, mis. perintah pindah layar atau pesan snackbar.
 * <p>
 * Masalah yang diselesaikan: LiveData mengirim ulang nilai terakhir setiap kali layar dibuat ulang
 * (mis. HP diputar). Tanpa Event, snackbar "Sesi berakhir" akan muncul lagi setiap kali layar diputar.
 */
public class Event<T> {

    private final T content;
    private boolean handled = false;

    public Event(T content) {
        this.content = content;
    }

    /** Mengembalikan isi hanya pada pemanggilan pertama; selanjutnya null. */
    public synchronized T getContentIfNotHandled() {
        if (handled) {
            return null;
        }
        handled = true;
        return content;
    }

    /** Melihat isi tanpa menandainya terpakai. */
    public T peekContent() {
        return content;
    }
}
