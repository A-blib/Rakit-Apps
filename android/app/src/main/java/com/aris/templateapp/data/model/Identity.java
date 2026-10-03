package com.aris.templateapp.data.model;

/** Metode login yang tersambung ke akun (untuk layar Pengaturan → Metode login terhubung). */
public class Identity {

    private final LoginMethod method;
    private final String email;

    public Identity(LoginMethod method, String email) {
        this.method = method;
        this.email = email;
    }

    public LoginMethod getMethod() {
        return method;
    }

    public String getEmail() {
        return email;
    }
}
