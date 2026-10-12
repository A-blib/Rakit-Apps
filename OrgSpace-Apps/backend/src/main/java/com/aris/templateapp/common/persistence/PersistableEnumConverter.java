package com.aris.templateapp.common.persistence;

import jakarta.persistence.AttributeConverter;

/**
 * Dasar converter JPA untuk {@link PersistableEnum}: menyimpan {@code value()} ke kolom teks.
 * Dipakai alih-alih {@code @Enumerated(STRING)} yang akan menyimpan nama konstanta huruf besar.
 */
public abstract class PersistableEnumConverter<E extends Enum<E> & PersistableEnum>
        implements AttributeConverter<E, String> {

    private final Class<E> type;

    protected PersistableEnumConverter(Class<E> type) {
        this.type = type;
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        return dbData == null ? null : PersistableEnum.fromValue(type, dbData);
    }
}
