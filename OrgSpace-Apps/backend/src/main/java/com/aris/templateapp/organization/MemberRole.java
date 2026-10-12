package com.aris.templateapp.organization;

import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.common.persistence.PersistableEnumConverter;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;

/**
 * Peran anggota organisasi (K15). Urutan konstanta = urutan hak: Pemilik paling tinggi.
 * Hak per aksi dijaga {@link OrganizationAccess}.
 */
public enum MemberRole implements PersistableEnum {

    OWNER("owner"),
    ADMIN("admin"),
    EDITOR("editor");

    private final String value;

    MemberRole(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String value() {
        return value;
    }

    /** true jika peran ini sama atau lebih tinggi dari {@code minimum} (Pemilik ≥ Admin ≥ Editor). */
    public boolean atLeast(MemberRole minimum) {
        return ordinal() <= minimum.ordinal();
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends PersistableEnumConverter<MemberRole> {
        public JpaConverter() {
            super(MemberRole.class);
        }
    }
}
