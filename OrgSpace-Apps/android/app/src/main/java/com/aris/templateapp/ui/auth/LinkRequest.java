package com.aris.templateapp.ui.auth;

import com.aris.templateapp.data.model.LoginMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Permintaan penyambungan akun dari backend (ACCOUNT_LINK_REQUIRED): email akun Google/GitHub ini sudah dipakai
 * akun lain. User harus masuk dengan salah satu {@link #existingMethods} sambil membawa {@link #linkToken}.
 */
public final class LinkRequest {

    private final String linkToken;
    private final List<LoginMethod> existingMethods;
    private final LoginMethod newMethod;

    public LinkRequest(String linkToken, List<String> existingMethods, LoginMethod newMethod) {
        this.linkToken = linkToken;
        this.existingMethods = new ArrayList<>();
        for (String value : existingMethods) {
            LoginMethod method = LoginMethod.fromValue(value);
            if (method != null) {
                this.existingMethods.add(method);
            }
        }
        this.newMethod = newMethod;
    }

    public String getLinkToken() {
        return linkToken;
    }

    public List<LoginMethod> getExistingMethods() {
        return existingMethods;
    }

    /** Metode yang akan disambungkan (yang barusan dicoba user). */
    public LoginMethod getNewMethod() {
        return newMethod;
    }
}
