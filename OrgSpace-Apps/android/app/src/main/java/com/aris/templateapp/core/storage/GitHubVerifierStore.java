package com.aris.templateapp.core.storage;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Rahasia sekali pakai untuk login/sambungkan GitHub (pola PKCE). Saat alur dimulai, hanya hash-nya
 * ({@code codeChallenge}) yang dikirim ke backend; verifier-nya tetap di HP dan baru dikirim saat menukar tiket dari
 * deep link. App lain yang menangkap deep link {@code templateapp://} tidak punya verifier ini, jadi tiketnya tidak
 * berguna.
 * <p>
 * Disimpan di SharedPreferences (bukan hanya di memori) karena sistem bisa menutup app selama user login di Custom Tab.
 */
@Singleton
public class GitHubVerifierStore {

    private static final String PREFS_NAME = "github_pkce";
    private static final String KEY_VERIFIER = "code_verifier";

    private final SharedPreferences prefs;
    private final SecureRandom random = new SecureRandom();

    @Inject
    public GitHubVerifierStore(@ApplicationContext Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Membuat verifier baru untuk satu alur GitHub dan mengembalikan challenge yang dikirim ke backend. */
    public String startFlow() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        prefs.edit().putString(KEY_VERIFIER, verifier).apply();
        return challengeOf(verifier);
    }

    /** Verifier alur yang sedang berjalan; dihapus setelah diambil karena tiket hanya bisa ditukar sekali. */
    @Nullable
    public String takeVerifier() {
        String verifier = prefs.getString(KEY_VERIFIER, null);
        prefs.edit().remove(KEY_VERIFIER).apply();
        return verifier;
    }

    /** PKCE S256: BASE64URL(SHA-256(verifier)) tanpa padding; harus sama persis dengan hitungan backend. */
    public static String challengeOf(String verifier) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
