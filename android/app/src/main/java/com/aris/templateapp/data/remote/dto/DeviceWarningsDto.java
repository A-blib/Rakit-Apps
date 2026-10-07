package com.aris.templateapp.data.remote.dto;

import java.util.List;

/** Peringatan tahap C dari WebView HP (JS_RUNTIME_ERROR, HORIZONTAL_OVERFLOW). */
public class DeviceWarningsDto {
    public final List<Warning> warnings;

    public DeviceWarningsDto(List<Warning> warnings) {
        this.warnings = warnings;
    }

    public static class Warning {
        public final String code;
        public final String message;
        public final String file;
        public final Integer line;

        public Warning(String code, String message, String file, Integer line) {
            this.code = code;
            this.message = message;
            this.file = file;
            this.line = line;
        }
    }
}
