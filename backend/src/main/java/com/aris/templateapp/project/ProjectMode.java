package com.aris.templateapp.project;

import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.common.persistence.PersistableEnumConverter;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;

/**
 * Cara website dibuat (K19–K20): dari template kreator, atau dirakit sendiri dari section bawaan OrgSpace.
 * Mode tidak bisa dipindah setelah website dibuat.
 */
public enum ProjectMode implements PersistableEnum {

    TEMPLATE("template"),
    ASSEMBLED("assembled");

    private final String value;

    ProjectMode(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String value() {
        return value;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends PersistableEnumConverter<ProjectMode> {
        public JpaConverter() {
            super(ProjectMode.class);
        }
    }
}
