package com.aris.templateapp.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Bentuk JSON semua error dari API, contoh:
 * {@code { "code": "VALIDATION_ERROR", "message": "Data tidak valid.", "fieldErrors": { "email": "..." } }}
 * <p>
 * {@code existingMethods} hanya diisi untuk error yang menyuruh user masuk dengan metode lain
 * (mis. {@code USE_SOCIAL_LOGIN}: {@code ["google"]}), agar app bisa menyebut nama metodenya.
 */
// Field yang null tidak ditulis, agar error biasa tetap ringkas.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String code, String message, Map<String, String> fieldErrors,
                            List<String> existingMethods) {
}
