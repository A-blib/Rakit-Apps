package com.aris.templateapp.data.model;

/** Hasil masuk/daftar yang berhasil. Token sudah disimpan oleh AuthRepository; UI cukup tahu user-nya. */
public class AuthResult {

    private final User user;
    private final boolean newUser;

    public AuthResult(User user, boolean newUser) {
        this.user = user;
        this.newUser = newUser;
    }

    public User getUser() {
        return user;
    }

    /** true jika akun baru saja dibuat (app lalu membuka onboarding). */
    public boolean isNewUser() {
        return newUser;
    }
}
