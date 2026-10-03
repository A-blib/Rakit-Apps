package com.aris.templateapp.data.model;

/** Peran/mode user. Nilai teksnya sama dengan JSON backend ("creator", "provider"). */
public enum UserRole {
    CREATOR("creator"),
    PROVIDER("provider");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    /** Nilai tidak dikenal dianggap CREATOR, mode yang selalu boleh dibuka. */
    public static UserRole fromValue(String value) {
        return PROVIDER.value.equals(value) ? PROVIDER : CREATOR;
    }
}
