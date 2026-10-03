package com.aris.templateapp.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Bentuk JSON semua error dari API, contoh:
 * {@code { "code": "VALIDATION_ERROR", "message": "Data tidak valid.", "fieldErrors": { "email": "..." } }}
 */
// fieldErrors hanya ditulis jika ada, agar error biasa tetap ringkas.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String code, String message, Map<String, String> fieldErrors) {
}
