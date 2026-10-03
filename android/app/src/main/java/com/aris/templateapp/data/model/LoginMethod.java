package com.aris.templateapp.data.model;

/** Metode login. Nilai teksnya sama dengan backend ("local", "google", "github"). */
public enum LoginMethod {
    LOCAL("local"),
    GOOGLE("google"),
    GITHUB("github");

    private final String value;

    LoginMethod(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static LoginMethod fromValue(String value) {
        for (LoginMethod method : values()) {
            if (method.value.equals(value)) {
                return method;
            }
        }
        return null;
    }
}
