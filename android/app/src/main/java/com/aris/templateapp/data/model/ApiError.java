package com.aris.templateapp.data.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Error yang sudah dirapikan untuk UI. Dibuat oleh {@code ApiErrorParser} dari ErrorResponse backend,
 * atau dari kegagalan jaringan ({@link #NETWORK_ERROR}).
 * Layar memilih teks dari strings.xml berdasarkan {@link #getCode()}, bukan menampilkan pesan server mentah.
 */
public class ApiError {

    /** Tidak ada koneksi / backend tidak terjangkau (mis. lupa adb reverse). */
    public static final String NETWORK_ERROR = "NETWORK_ERROR";
    /** Respons tidak dikenali (bukan format ErrorResponse). */
    public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";

    private final String code;
    private final String message;
    private final Map<String, String> fieldErrors;
    private final List<String> existingMethods;
    private final String linkToken;

    public ApiError(String code, String message, Map<String, String> fieldErrors,
                    List<String> existingMethods, String linkToken) {
        this.code = code;
        this.message = message;
        this.fieldErrors = fieldErrors == null ? Collections.emptyMap() : fieldErrors;
        this.existingMethods = existingMethods == null ? Collections.emptyList() : existingMethods;
        this.linkToken = linkToken;
    }

    public static ApiError of(String code) {
        return new ApiError(code, null, null, null, null);
    }

    public String getCode() {
        return code;
    }

    /** Pesan cadangan dari server (Bahasa Indonesia); bisa null. */
    public String getMessage() {
        return message;
    }

    /** Pesan error per field form, mis. "email" → "Format email tidak valid". */
    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    /** Metode login yang sudah dimiliki akun, untuk USE_SOCIAL_LOGIN dan ACCOUNT_LINK_REQUIRED. */
    public List<String> getExistingMethods() {
        return existingMethods;
    }

    /** Hanya untuk ACCOUNT_LINK_REQUIRED. */
    public String getLinkToken() {
        return linkToken;
    }

    public boolean isNetworkError() {
        return NETWORK_ERROR.equals(code);
    }
}
