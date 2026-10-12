package com.aris.templateapp.core.storage;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aris.templateapp.core.util.Event;
import com.aris.templateapp.data.mapper.UserMapper;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.data.model.UserRole;
import com.aris.templateapp.data.remote.dto.CategoriesDto;
import com.aris.templateapp.data.remote.dto.OrganizationSummaryDto;
import com.aris.templateapp.data.remote.dto.UserDto;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Data sesi yang tidak rahasia: salinan user terakhir (agar app tetap bisa dibuka saat offline),
 * mode terakhir, daftar ruang kerja + ruang kerja terakhir, dan penanda intro sudah dilihat. Token disimpan terpisah di {@link TokenStorage}.
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
    private static final String KEY_INSTALL_ID = "install_id";
    private static final String KEY_WORKSPACES = "workspaces";
    private static final String KEY_ACTIVE_WORKSPACE = "active_workspace";
    private static final String KEY_CATEGORIES = "org_categories";

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

    /** Daftar ruang kerja terakhir dari backend (bentuk DTO, sama alasannya dengan {@link #saveUser}). */
    public synchronized void saveWorkspaces(List<OrganizationSummaryDto> workspaces) {
        prefs.edit().putString(KEY_WORKSPACES, gson.toJson(workspaces)).apply();
    }

    /** Daftar ruang kerja tersimpan; kosong jika belum pernah dimuat atau data rusak. */
    public synchronized List<OrganizationSummaryDto> getWorkspaces() {
        String json = prefs.getString(KEY_WORKSPACES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        try {
            List<OrganizationSummaryDto> list = gson.fromJson(json, new TypeToken<List<OrganizationSummaryDto>>() {
            }.getType());
            return list == null ? new ArrayList<>() : list;
        } catch (JsonParseException e) {
            return new ArrayList<>();
        }
    }

    /** Daftar kategori & Isi template terakhir dari server, untuk label saat offline. Tidak terhapus saat keluar. */
    public synchronized void saveCategories(CategoriesDto categories) {
        prefs.edit().putString(KEY_CATEGORIES, gson.toJson(categories)).apply();
    }

    @Nullable
    public synchronized CategoriesDto getCategories() {
        String json = prefs.getString(KEY_CATEGORIES, null);
        try {
            return json == null ? null : gson.fromJson(json, CategoriesDto.class);
        } catch (JsonParseException e) {
            return null;
        }
    }

    @Nullable
    public String getActiveWorkspaceId() {
        return prefs.getString(KEY_ACTIVE_WORKSPACE, null);
    }

    public void saveActiveWorkspaceId(String id) {
        prefs.edit().putString(KEY_ACTIVE_WORKSPACE, id).apply();
    }

    /**
     * ID acak per instalasi app, untuk menghitung "Dilihat"/"Didownload" dari tamu tanpa mengenali siapa
     * orangnya (alur-provider.md 3.5). Dibuat sekali, tidak ikut terhapus saat keluar akun.
     */
    public synchronized String getInstallId() {
        String id = prefs.getString(KEY_INSTALL_ID, null);
        if (id == null) {
            id = UUID.randomUUID().toString();
            prefs.edit().putString(KEY_INSTALL_ID, id).apply();
        }
        return id;
    }

    /** Menghapus data user dan ruang kerja (saat keluar / sesi berakhir). Penanda intro & ID instalasi tetap disimpan. */
    public synchronized void clearUser() {
        prefs.edit().remove(KEY_USER).remove(KEY_LAST_MODE).remove(KEY_WORKSPACES).remove(KEY_ACTIVE_WORKSPACE).apply();
    }

    public LiveData<Event<Boolean>> sessionExpiredEvents() {
        return sessionExpired;
    }

    /** Boleh dipanggil dari thread mana pun (postValue meneruskannya ke thread utama). */
    public void notifySessionExpired() {
        sessionExpired.postValue(new Event<>(true));
    }
}
