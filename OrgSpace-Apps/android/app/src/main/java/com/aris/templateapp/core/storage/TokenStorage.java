package com.aris.templateapp.core.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Menyimpan access token & refresh token secara terenkripsi.
 * <p>
 * Cara kerja: kunci AES dibuat dan disimpan di <b>Android Keystore</b> (area aman milik sistem; kuncinya tidak
 * bisa dibaca keluar, bahkan oleh app kita sendiri). Token dienkripsi AES-GCM dengan kunci itu, lalu hasilnya
 * (IV + ciphertext, dalam Base64) disimpan di SharedPreferences. Tanpa kunci di HP ini, isi file tidak berguna.
 * <p>
 * Semua method {@code synchronized} karena dipanggil dari beberapa thread (UI, OkHttp, executor).
 */
@Singleton
public class TokenStorage {

    private static final String TAG = "TokenStorage";
    private static final String PREFS_NAME = "secure_tokens";
    private static final String KEY_ACCESS = "access_token";
    private static final String KEY_REFRESH = "refresh_token";
    private static final String KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "templateapp_token_key";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final String SEPARATOR = ":";

    private final SharedPreferences prefs;

    @Inject
    public TokenStorage(@ApplicationContext Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public synchronized void saveTokens(String accessToken, String refreshToken) {
        try {
            prefs.edit()
                    .putString(KEY_ACCESS, encrypt(accessToken))
                    .putString(KEY_REFRESH, encrypt(refreshToken))
                    .apply();
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "Gagal mengenkripsi token", e);
            clear();
        }
    }

    public synchronized String getAccessToken() {
        return read(KEY_ACCESS);
    }

    public synchronized String getRefreshToken() {
        return read(KEY_REFRESH);
    }

    public synchronized boolean hasTokens() {
        return prefs.contains(KEY_REFRESH);
    }

    public synchronized void clear() {
        prefs.edit().clear().apply();
    }

    private String read(String key) {
        String stored = prefs.getString(key, null);
        if (stored == null) {
            return null;
        }
        try {
            return decrypt(stored);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            // Kunci hilang/berganti (mis. data app dipulihkan ke HP lain): token tidak bisa dibuka, anggap belum login.
            Log.w(TAG, "Token tidak bisa didekripsi; sesi dihapus");
            clear();
            return null;
        }
    }

    private String encrypt(String plain) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        // IV (angka acak sekali pakai) dibuat otomatis oleh Keystore dan wajib disimpan untuk dekripsi.
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
        byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP)
                + SEPARATOR + Base64.encodeToString(cipherText, Base64.NO_WRAP);
    }

    private String decrypt(String stored) throws GeneralSecurityException {
        String[] parts = stored.split(SEPARATOR, 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Format token tersimpan tidak dikenal");
        }
        byte[] iv = Base64.decode(parts[0], Base64.NO_WRAP);
        byte[] cipherText = Base64.decode(parts[1], Base64.NO_WRAP);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
        return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
    }

    private SecretKey getOrCreateKey() throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE);
            keyStore.load(null);
            if (keyStore.containsAlias(KEY_ALIAS)) {
                return ((KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null)).getSecretKey();
            }
            KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE);
            generator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build());
            return generator.generateKey();
        } catch (java.io.IOException e) {
            throw new GeneralSecurityException("Android Keystore tidak bisa dibuka", e);
        }
    }
}
