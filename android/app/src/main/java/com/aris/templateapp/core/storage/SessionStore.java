package com.aris.templateapp.core.storage;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.data.mapper.UserMapper;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.data.model.UserRole;
import com.aris.templateapp.data.remote.dto.UserDto;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Data sesi yang tidak rahasia: salinan user terakhir (agar app tetap bisa dibuka saat offline),
 * mode terakhir, dan penanda intro sudah dilihat. Token disimpan terpisah di {@link TokenStorage}.
 * <p>
 * Juga menjadi "pengeras suara" event sesi berakhir: TokenAuthenticator memanggil
 * {@link #notifySessionExpired()}, MainActivity mendengarkan {@link #sessionExpiredEvents()}.
 */
@Singleton
public class SessionStore {

    private static final String PREFS_NAME = "session";
    private static final String KEY_USER = "cached_user";
    private static final String KEY_LAST_MODE = "last_mode";
    private static final String KEY_INTRO_SEEN = "intro_seen";

    private final SharedPreferences prefs;
    private final Gson gson;
    private final MutableLiveData<Event<Boolean>> sessionExpired = new MutableLiveData<>();

    @Inject
    public SessionStore(@ApplicationContext Context context, Gson gson) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = gson;
    }

    public boolean isIntroSeen() {
        return prefs.getBoolean(KEY_INTRO_SEEN, false);
    }

    public void setIntroSeen() {
        prefs.edit().putBoolean(KEY_INTRO_SEEN, true).apply();
    }

    /** Disimpan dalam bentuk DTO (JSON) karena nama field DTO dijaga tetap sama oleh aturan R8. */
    public synchronized void saveUser(UserDto user) {
        prefs.edit()
                .putString(KEY_USER, gson.toJson(user))
                .putString(KEY_LAST_MODE, user.activeMode)
                .apply();
    }

    /** User terakhir yang tersimpan, atau null jika belum ada / data rusak. */
    public synchronized User getCachedUser() {
        String json = prefs.getString(KEY_USER, null);
        if (json == null) {
            return null;
        }
        try {
            return UserMapper.toModel(gson.fromJson(json, UserDto.class));
        } catch (JsonParseException e) {
            return null;
        }
    }

    public UserRole getLastMode() {
        return UserRole.fromValue(prefs.getString(KEY_LAST_MODE, null));
    }

    public void saveLastMode(UserRole mode) {
        prefs.edit().putString(KEY_LAST_MODE, mode.value()).apply();
    }

    /** Menghapus data user (saat keluar / sesi berakhir). Penanda intro tetap disimpan. */
    public synchronized void clearUser() {
        prefs.edit().remove(KEY_USER).remove(KEY_LAST_MODE).apply();
    }

    public LiveData<Event<Boolean>> sessionExpiredEvents() {
        return sessionExpired;
    }

    /** Boleh dipanggil dari thread mana pun (postValue meneruskannya ke thread utama). */
    public void notifySessionExpired() {
        sessionExpired.postValue(new Event<>(true));
    }
}
