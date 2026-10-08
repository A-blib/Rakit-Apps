package com.aris.templateapp.core.template;

import androidx.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rasio kontras dua warna menurut rumus WCAG (alur-buat-website-via-template.md bagian 6.5). Murni Java (tanpa
 * {@code android.graphics.Color}) agar bisa diuji di laptop. Menerima "#rgb", "#rrggbb", dan "rgb()/rgba()" dari
 * {@code getComputedStyle}.
 */
public final class ColorContrast {

    /** Di bawah rasio ini teks biasa sulit dibaca (WCAG AA). */
    public static final double MIN_RATIO = 4.5;

    private static final Pattern RGB = Pattern.compile("rgba?\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*(?:,\\s*([0-9.]+)\\s*)?\\)");

    private ColorContrast() {
    }

    /** @return rasio 1–21, atau null jika salah satu warna tidak terbaca / transparan */
    @Nullable
    public static Double ratio(@Nullable String a, @Nullable String b) {
        int[] ca = parse(a);
        int[] cb = parse(b);
        if (ca == null || cb == null) {
            return null;
        }
        double la = luminance(ca);
        double lb = luminance(cb);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    public static boolean isLow(@Nullable String foreground, @Nullable String background) {
        Double ratio = ratio(foreground, background);
        return ratio != null && ratio < MIN_RATIO;
    }

    @Nullable
    static int[] parse(@Nullable String color) {
        if (color == null) {
            return null;
        }
        String hex = CustomCss.color(color);
        if (hex != null) {
            int value = Integer.parseInt(hex.substring(1), 16);
            return new int[] {(value >> 16) & 0xff, (value >> 8) & 0xff, value & 0xff};
        }
        Matcher m = RGB.matcher(color.trim());
        if (!m.matches()) {
            return null;
        }
        if (m.group(4) != null && Double.parseDouble(m.group(4)) < 0.5) {
            return null; // hampir transparan: warna sebenarnya tidak diketahui
        }
        return new int[] {clamp(m.group(1)), clamp(m.group(2)), clamp(m.group(3))};
    }

    /** "rgb(30, 58, 138)" → "#1e3a8a" (untuk mengisi kolom hex dengan warna asli elemen). */
    @Nullable
    public static String toHex(@Nullable String color) {
        int[] c = parse(color);
        return c == null ? null : String.format("#%02x%02x%02x", c[0], c[1], c[2]);
    }

    private static int clamp(String value) {
        return Math.max(0, Math.min(255, Integer.parseInt(value)));
    }

    private static double luminance(int[] c) {
        return 0.2126 * channel(c[0]) + 0.7152 * channel(c[1]) + 0.0722 * channel(c[2]);
    }

    private static double channel(int value) {
        double v = value / 255.0;
        return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }
}
