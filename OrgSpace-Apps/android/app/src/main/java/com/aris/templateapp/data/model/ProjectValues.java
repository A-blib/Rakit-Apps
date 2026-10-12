package com.aris.templateapp.data.model;

import androidx.annotation.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Isi {@code values.json} milik satu project (alur-buat-website-via-template.md bagian 11.3). Isian yang tidak ada di
 * sini masih memakai isi template. Angka gaya disimpan sebagai teks (mis. "40") agar satu peta bisa menampung warna
 * dan ukuran sekaligus.
 */
public class ProjectValues {

    public Map<String, FieldValue> fields = new LinkedHashMap<>();
    /** kunci isian → (properti CSS → nilai), mis. "nama_toko" → {"color": "#1e3a8a", "font-size": "40"}. */
    public Map<String, Map<String, String>> styles = new LinkedHashMap<>();
    /** variabel CSS tema → nilai, mis. "--primary" → "#16a34a". */
    public Map<String, String> theme = new LinkedHashMap<>();

    public static class FieldValue {
        @Nullable
        public String text;
        @Nullable
        public String href;
        /** Path gambar milik project, relatif terhadap folder project, mis. "images/foto_hero-1728370000.webp". */
        @Nullable
        public String image;

        public FieldValue copy() {
            FieldValue c = new FieldValue();
            c.text = text;
            c.href = href;
            c.image = image;
            return c;
        }

        boolean isEmpty() {
            return text == null && href == null && image == null;
        }
    }

    public FieldValue field(String key) {
        return fields.computeIfAbsent(key, k -> new FieldValue());
    }

    @Nullable
    public FieldValue peek(String key) {
        return fields.get(key);
    }

    public Map<String, String> style(String key) {
        return styles.computeIfAbsent(key, k -> new LinkedHashMap<>());
    }

    /** Membuang isian/gaya kosong agar values.json tetap ringkas dan "masih isi template" mudah dikenali. */
    public void compact() {
        fields.values().removeIf(v -> v == null || v.isEmpty());
        styles.values().removeIf(m -> m == null || m.isEmpty());
    }

    public ProjectValues copy() {
        ProjectValues c = new ProjectValues();
        fields.forEach((k, v) -> c.fields.put(k, v.copy()));
        styles.forEach((k, v) -> c.styles.put(k, new LinkedHashMap<>(v)));
        c.theme.putAll(theme);
        return c;
    }

    public boolean isEmpty() {
        compact();
        return fields.isEmpty() && styles.isEmpty() && theme.isEmpty();
    }
}
