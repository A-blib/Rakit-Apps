package com.aris.templateapp.common.persistence;

/**
 * Enum yang punya nilai teks sendiri untuk database dan JSON (mis. {@code CREATOR} ↔ {@code "creator"}).
 * Nama konstanta Java tetap huruf besar, sedangkan nilai di database mengikuti CHECK constraint migrasi.
 */
public interface PersistableEnum {

    String value();

    /** Nilai null menghasilkan null (mis. kategori template yang belum diisi saat upload). */
    static <E extends Enum<E> & PersistableEnum> E fromValue(Class<E> type, String value) {
        if (value == null) {
            return null;
        }
        for (E constant : type.getEnumConstants()) {
            if (constant.value().equals(value)) {
                return constant;
            }
        }
        throw new IllegalArgumentException("Nilai " + type.getSimpleName() + " tidak dikenal: " + value);
    }
}
