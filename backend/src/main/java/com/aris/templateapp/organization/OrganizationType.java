package com.aris.templateapp.organization;

import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.common.persistence.PersistableEnumConverter;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;

/** Jenis ruang kerja: organisasi (banyak anggota) atau ruang pribadi (satu orang). */
public enum OrganizationType implements PersistableEnum {

    ORGANIZATION("organization"),
    PERSONAL("personal");

    private final String value;

    OrganizationType(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String value() {
        return value;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends PersistableEnumConverter<OrganizationType> {
        public JpaConverter() {
            super(OrganizationType.class);
        }
    }
}
