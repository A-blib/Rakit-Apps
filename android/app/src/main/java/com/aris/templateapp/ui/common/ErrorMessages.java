package com.aris.templateapp.ui.common;

import android.content.Context;

import com.aris.templateapp.R;
import com.aris.templateapp.data.model.ApiError;
import com.aris.templateapp.data.model.LoginMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Memilih teks error untuk user berdasarkan {@link ApiError#getCode()} (tabel bagian 6.2).
 * Teks selalu diambil dari strings.xml agar seragam dan bisa diterjemahkan.
 */
public final class ErrorMessages {

    private ErrorMessages() {
    }

    public static String forError(Context context, ApiError error) {
        if (error == null) {
            return context.getString(R.string.error_unknown);
        }
        switch (error.getCode()) {
            case ApiError.NETWORK_ERROR:
                return context.getString(R.string.error_no_connection);
            case "INVALID_CREDENTIALS":
                return context.getString(R.string.error_invalid_credentials);
            case "EMAIL_ALREADY_USED":
                return context.getString(R.string.error_email_already_used);
            case "USE_SOCIAL_LOGIN":
                return context.getString(R.string.error_use_social_login, methodLabels(context, error.getExistingMethods()));
            case "REFRESH_TOKEN_INVALID":
            case "UNAUTHORIZED":
                return context.getString(R.string.error_session_expired);
            default:
                // Kode lain yang belum punya teks khusus: pakai pesan dari server bila ada.
                return error.getMessage() != null ? error.getMessage() : context.getString(R.string.error_unknown);
        }
    }

    /** ["google","github"] → "Google atau GitHub". */
    public static String methodLabels(Context context, List<String> methods) {
        List<String> labels = new ArrayList<>();
        for (String value : methods) {
            LoginMethod method = LoginMethod.fromValue(value);
            if (method != null) {
                labels.add(methodLabel(context, method));
            }
        }
        return String.join(" " + context.getString(R.string.word_or) + " ", labels);
    }

    public static String methodLabel(Context context, LoginMethod method) {
        switch (method) {
            case GOOGLE:
                return context.getString(R.string.method_google);
            case GITHUB:
                return context.getString(R.string.method_github);
            case LOCAL:
            default:
                return context.getString(R.string.method_email);
        }
    }
}
