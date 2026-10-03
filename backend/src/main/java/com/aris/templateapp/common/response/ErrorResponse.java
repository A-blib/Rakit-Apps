package com.aris.templateapp.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Bentuk JSON semua error dari API, contoh:
 * {@code { "code": "VALIDATION_ERROR", "message": "Data tidak valid.", "fieldErrors": { "email": "..." } }}
 *
 * @param existingMethods metode login yang sudah dimiliki akun, untuk {@code USE_SOCIAL_LOGIN} dan
 *                        {@code ACCOUNT_LINK_REQUIRED} (mis. {@code ["google"]}), agar app bisa menyebut namanya
 * @param linkToken       hanya untuk {@code ACCOUNT_LINK_REQUIRED}: dikirim balik saat user masuk dengan metode lama
 */
// Field yang null tidak ditulis, agar error biasa tetap ringkas.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String code, String message, Map<String, String> fieldErrors,
                            List<String> existingMethods, String linkToken) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, null, null, null);
    }
}
