package com.aris.templateapp.organization;

import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.common.persistence.PersistableEnumConverter;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;

/**
 * Kategori organisasi (revisi-fondasi-orgspace.md bagian 6). Satu-satunya sumber daftar ini: Android, galeri
 * template, dan upload kreator mengambilnya lewat {@code GET /api/meta/categories}, sehingga kategori bisa
 * berubah tanpa update app. Daftar masih sementara, menunggu hasil observasi lapangan Aris.
 */
public enum OrgCategory implements PersistableEnum {

    OSIS_MPK("osis_mpk", "OSIS / MPK", true, false),
    BEM_DEMA_DPM("bem_dema_dpm", "BEM / DEMA / DPM", false, true),
    HIMPUNAN("himpunan", "Himpunan / HMJ / HMPS", false, true),
    UKM("ukm", "UKM", false, true),
    PRAMUKA("pramuka", "Pramuka", true, true),
    JURNALISTIK_MEDIA("jurnalistik_media", "Jurnalistik / Media", true, true),
    PMR_KSR("pmr_ksr", "PMR / KSR", true, true),
    PASKIBRA("paskibra", "Paskibra", true, false),
    KEROHANIAN("kerohanian", "Kerohanian", true, true),
    SENI_OLAHRAGA("seni_olahraga", "Seni & Olahraga", true, true),
    LAINNYA("lainnya", "Ekskul / Komunitas lain", true, true);

    private final String value;
    private final String label;
    private final boolean forSchool;
    private final boolean forCampus;

    OrgCategory(String value, String label, boolean forSchool, boolean forCampus) {
        this.value = value;
        this.label = label;
        this.forSchool = forSchool;
        this.forCampus = forCampus;
    }

    @Override
    @JsonValue
    public String value() {
        return value;
    }

    public String label() {
        return label;
    }

    /** Kombinasi kategori–jenjang yang sah; selain itu ditolak {@code CATEGORY_LEVEL_MISMATCH}. */
    public boolean allows(OrgLevel level) {
        return level.isCampus() ? forCampus : forSchool;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends PersistableEnumConverter<OrgCategory> {
        public JpaConverter() {
            super(OrgCategory.class);
        }
    }
}
