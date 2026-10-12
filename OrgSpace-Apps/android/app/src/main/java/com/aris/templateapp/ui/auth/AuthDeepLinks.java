package com.aris.templateapp.ui.auth;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aris.templateapp.core.util.Event;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Menampung deep link {@code templateapp://auth/callback?...} yang diterima MainActivity setelah login GitHub,
 * lalu meneruskannya ke layar yang sedang menunggu (Masuk/Daftar, atau Pengaturan saat menyambungkan GitHub).
 * <p>
 * LiveData menyimpan nilai terakhir, jadi layar yang baru tampil setelah deep link datang tetap menerimanya;
 * {@link Event} memastikan deep link hanya diproses sekali.
 */
@Singleton
public class AuthDeepLinks {

    public static final String SCHEME = "templateapp";
    public static final String HOST = "auth";
    public static final String PATH = "/callback";

    private final MutableLiveData<Event<Uri>> callbacks = new MutableLiveData<>();

    @Inject
    public AuthDeepLinks() {
    }

    public static boolean isAuthCallback(Uri uri) {
        return uri != null && SCHEME.equals(uri.getScheme()) && HOST.equals(uri.getHost())
                && PATH.equals(uri.getPath());
    }

    public void publish(Uri uri) {
        callbacks.setValue(new Event<>(uri));
    }

    public LiveData<Event<Uri>> getCallbacks() {
        return callbacks;
    }
}
