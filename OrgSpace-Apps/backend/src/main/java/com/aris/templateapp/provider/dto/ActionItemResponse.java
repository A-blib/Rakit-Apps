package com.aris.templateapp.provider.dto;

import java.util.UUID;

/**
 * Satu kartu "Perlu tindakan". Kartu hilang sendiri saat masalahnya beres, karena daftar ini dihitung
 * dari kondisi template/profil saat ini, bukan disimpan.
 *
 * @param kind TEMPLATE_CHECK_FAILED · TEMPLATE_WARNING · DRAFT · PROFILE_INCOMPLETE
 */
public record ActionItemResponse(String kind, UUID templateId, String templateName, int errorCount, int warningCount) {

    public static final String CHECK_FAILED = "TEMPLATE_CHECK_FAILED";
    public static final String WARNING = "TEMPLATE_WARNING";
    public static final String DRAFT = "DRAFT";
    public static final String PROFILE_INCOMPLETE = "PROFILE_INCOMPLETE";
}
