package com.aris.templateapp.monitoring;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Locale;

/**
 * Asal request untuk statistik pengguna aktif per platform. App Android & portal web mengirim header
 * {@value #HEADER}; request lama dari app (sebelum header ini ada) dikenali dari User-Agent OkHttp.
 */
public enum ClientPlatform {
    ANDROID("android"),
    WEB("web"),
    OTHER("other");

    public static final String HEADER = "X-OrgSpace-Client";

    private final String value;

    ClientPlatform(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static ClientPlatform of(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (header != null) {
            for (ClientPlatform platform : values()) {
                if (platform.value.equalsIgnoreCase(header.trim())) {
                    return platform;
                }
            }
        }
        String agent = request.getHeader("User-Agent");
        return agent != null && agent.toLowerCase(Locale.ROOT).startsWith("okhttp") ? ANDROID : OTHER;
    }
}
