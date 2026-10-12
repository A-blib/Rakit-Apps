package com.aris.templateapp.core.template;

import androidx.annotation.Nullable;

import com.aris.templateapp.data.model.ProjectValues;
import com.aris.templateapp.data.model.TemplateManifest;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Membuat isi {@code custom.css} dari gaya dan tema project (alur-buat-website-via-template.md bagian 11.4).
 * <p>
 * Input user tidak pernah ditulis mentah ke CSS: hanya properti yang diizinkan manifest, warna hex yang valid, dan
 * angka di dalam rentang manifest. Selain itu dilewati diam-diam (nilai lama yang rusak tidak merusak website).
 */
public final class CustomCss {

    /** Preview memakai {@code [data-key]}; export memakai class unik karena atribut data-* dihapus (bagian 8.1). */
    public enum Target { PREVIEW, EXPORT }

    public static final String EXPORT_CLASS_PREFIX = "ws-";

    private static final Pattern KEY = Pattern.compile("^[A-Za-z0-9_-]+$");
    private static final Pattern VAR = Pattern.compile("^--[A-Za-z0-9_-]+$");
    private static final Pattern HEX = Pattern.compile("^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$");
    private static final Pattern INTEGER = Pattern.compile("^-?[0-9]{1,4}(px)?$");
    /** Batas aman variabel tema jenis ukuran (manifest tidak memberi rentang untuk tema). */
    public static final int THEME_SIZE_MAX = 200;

    private CustomCss() {
    }

    public static String build(TemplateManifest manifest, ProjectValues values, Target target) {
        StringBuilder css = new StringBuilder();
        StringBuilder root = new StringBuilder();
        for (TemplateManifest.ThemeVar variable : manifest.theme) {
            String raw = values.theme.get(variable.var);
            String value = raw == null || !VAR.matcher(variable.var).matches() ? null
                    : "color".equals(variable.type) ? color(raw) : size(raw, 0, THEME_SIZE_MAX);
            if (value != null) {
                root.append(' ').append(variable.var).append(": ").append(value).append(';');
            }
        }
        if (root.length() > 0) {
            css.append(":root {").append(root).append(" }\n");
        }
        for (TemplateManifest.Field field : manifest.fields) {
            Map<String, String> chosen = values.styles.get(field.key);
            if (chosen == null || chosen.isEmpty() || !KEY.matcher(field.key).matches()) {
                continue;
            }
            StringBuilder rules = new StringBuilder();
            for (TemplateManifest.Style style : field.styles) {
                String value = value(style, chosen.get(style.prop));
                if (value != null) {
                    rules.append(' ').append(style.prop).append(": ").append(value).append(" !important;");
                }
            }
            if (rules.length() > 0) {
                css.append(selector(field.key, target)).append(" {").append(rules).append(" }\n");
            }
        }
        return css.toString();
    }

    public static String selector(String key, Target target) {
        return target == Target.PREVIEW ? "[data-key=\"" + key + "\"]" : "." + EXPORT_CLASS_PREFIX + key;
    }

    /** true jika isian ini punya minimal satu gaya yang benar-benar ditulis ke CSS. */
    public static boolean hasStyle(TemplateManifest.Field field, ProjectValues values) {
        Map<String, String> chosen = values.styles.get(field.key);
        if (chosen == null) {
            return false;
        }
        for (TemplateManifest.Style style : field.styles) {
            if (value(style, chosen.get(style.prop)) != null) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    static String value(TemplateManifest.Style style, @Nullable String raw) {
        if (raw == null || style.prop == null) {
            return null;
        }
        switch (style.prop) {
            case "color":
            case "background-color":
                return color(raw);
            case "font-size":
                return size(raw, style.min == null ? 8 : style.min, style.max == null ? 96 : style.max);
            case "border-radius":
                return size(raw, style.min == null ? 0 : style.min, style.max == null ? 64 : style.max);
            default:
                return null;
        }
    }

    /** "#ABC" → "#aabbcc"; selain hex → null. */
    @Nullable
    public static String color(@Nullable String raw) {
        if (raw == null || !HEX.matcher(raw.trim()).matches()) {
            return null;
        }
        String hex = raw.trim().toLowerCase(Locale.ROOT);
        if (hex.length() == 4) {
            hex = "#" + hex.charAt(1) + hex.charAt(1) + hex.charAt(2) + hex.charAt(2) + hex.charAt(3) + hex.charAt(3);
        }
        return hex;
    }

    /** "40" atau "40px" di dalam [min, max] → "40px"; selain itu null. */
    @Nullable
    static String size(String raw, int min, int max) {
        String clean = raw.trim().toLowerCase(Locale.ROOT);
        if (!INTEGER.matcher(clean).matches()) {
            return null;
        }
        int number = Integer.parseInt(clean.replace("px", ""));
        return number < min || number > max ? null : number + "px";
    }
}
