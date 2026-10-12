package com.aris.templateapp.upload.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Hasil tahap C pengecekan (alur-fitur-upload.md bagian 5.1 & 5.6): WebView di HP menjalankan halaman lalu melaporkan
 * error JavaScript dan tampilan yang melebar. Hanya menjadi Peringatan, tidak pernah menggagalkan template.
 */
public record DeviceWarningsRequest(@NotNull @Size(max = 20) List<@Valid Warning> warnings) {

    /** @param code JS_RUNTIME_ERROR atau HORIZONTAL_OVERFLOW */
    public record Warning(@NotBlank String code, @NotBlank @Size(max = 300) String message,
                          @Size(max = 255) String file, Integer line) {
    }
}
