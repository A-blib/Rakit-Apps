package com.aris.templateapp.organization.dto;

import java.util.List;

/**
 * Daftar jenjang dan kategori organisasi untuk form onboarding, galeri template, dan upload kreator.
 *
 * @param categories setiap kategori membawa daftar jenjang yang boleh memilihnya
 */
public record CategoriesResponse(List<Level> levels, List<Category> categories) {

    public record Level(String value, String label) {
    }

    public record Category(String value, String label, List<String> levels) {
    }
}
