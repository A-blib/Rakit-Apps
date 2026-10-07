package com.aris.templateapp.core.upload;

import java.util.Locale;

/** Ukuran file yang mudah dibaca, format Indonesia: "12,4 MB", "350 KB". */
public final class FileSizes {

    private static final Locale INDONESIA = Locale.forLanguageTag("id");

    private FileSizes() {
    }

    public static String format(long bytes) {
        if (bytes >= 1024L * 1024L) {
            return String.format(INDONESIA, "%.1f MB", bytes / (1024.0 * 1024.0));
        }
        if (bytes >= 1024L) {
            return Math.round(bytes / 1024.0) + " KB";
        }
        return bytes + " byte";
    }

    /** Hanya angkanya dalam MB, mis. "12,4" (untuk "12,4 dari 18 MB"). */
    public static String megabytes(long bytes) {
        return String.format(INDONESIA, "%.1f", bytes / (1024.0 * 1024.0));
    }
}
