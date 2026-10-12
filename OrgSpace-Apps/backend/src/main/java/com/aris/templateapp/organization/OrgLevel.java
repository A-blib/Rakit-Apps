package com.aris.templateapp.organization;

import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.common.persistence.PersistableEnumConverter;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;

/** Jenjang instansi pendidikan organisasi (revisi-fondasi-orgspace.md bagian 6). */
public enum OrgLevel implements PersistableEnum {

    SMP("smp", "SMP/MTs", false),
    SMA("sma", "SMA/MA", false),
    SMK("smk", "SMK", false),
    PERGURUAN_TINGGI("perguruan_tinggi", "Perguruan tinggi", true);

    private final String value;
    private final String label;
    private final boolean campus;

    OrgLevel(String value, String label, boolean campus) {
        this.value = value;
        this.label = label;
        this.campus = campus;
    }

    public String label() {
        return label;
    }

    /** Kampus vs sekolah menentukan kategori yang boleh dipilih (lihat {@link OrgCategory}). */
    public boolean isCampus() {
        return campus;
    }

    @Override
    @JsonValue
    public String value() {
        return value;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends PersistableEnumConverter<OrgLevel> {
        public JpaConverter() {
            super(OrgLevel.class);
        }
    }
}
