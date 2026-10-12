package com.aris.templateapp.template;

import com.aris.templateapp.common.persistence.PersistableEnum;
import com.aris.templateapp.common.persistence.PersistableEnumConverter;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Converter;

/** Status satu kali pengecekan otomatis template. */
public enum CheckStatus implements PersistableEnum {

    RUNNING("running"),
    PASSED("passed"),
    FAILED("failed");

    private final String value;

    CheckStatus(String value) {
        this.value = value;
    }

    // Nilai teks yang sama dipakai di database dan di JSON.
    @Override
    @JsonValue
    public String value() {
        return value;
    }

    @Converter(autoApply = true)
    public static class JpaConverter extends PersistableEnumConverter<CheckStatus> {
        public JpaConverter() {
            super(CheckStatus.class);
        }
    }
}
