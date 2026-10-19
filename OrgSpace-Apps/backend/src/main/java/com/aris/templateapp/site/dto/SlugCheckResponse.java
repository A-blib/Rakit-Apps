package com.aris.templateapp.site.dto;

/**
 * Hasil cek nama website untuk dialog terbitkan.
 *
 * @param locked    true jika website sudah pernah terbit; slug tidak bisa diganti lagi
 * @param available false jika slug tidak valid/dipakai; alasannya di {@code message}
 * @param url       pratinjau link publik, mis. {@code https://orgspace.app/osis-smansa}
 */
public record SlugCheckResponse(String slug, boolean locked, boolean available, String message, String url) {
}
