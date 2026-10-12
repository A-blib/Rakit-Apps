package com.aris.templateapp.ui.upload;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Isian percobaan di langkah Coba (alur-fitur-upload.md bagian 7.12): tetap ada selama provider bolak-balik antara
 * Tandai dan Coba, dan hilang saat app ditutup. Tidak pernah dikirim ke server; isi contoh template tidak berubah.
 */
@Singleton
public class TrySession {

    /** Nilai percobaan satu isian. Null = belum diubah (pakai isi asli). */
    public static class Value {
        public String text;
        public String href;
        /** Gambar pilihan sebagai data URL (base64), agar bisa langsung dipasang di WebView. */
        public String image;
        /** Gaya: color, background-color, font-size (mis. "40px"), border-radius. */
        public final Map<String, String> styles = new LinkedHashMap<>();
    }

    private final Map<String, Map<String, Value>> values = new HashMap<>();
    private final Map<String, Map<String, String>> themes = new HashMap<>();

    @Inject
    public TrySession() {
    }

    public Value value(String templateId, String key) {
        return values.computeIfAbsent(templateId, k -> new HashMap<>()).computeIfAbsent(key, k -> new Value());
    }

    public Map<String, Value> values(String templateId) {
        return values.computeIfAbsent(templateId, k -> new HashMap<>());
    }

    public Map<String, String> theme(String templateId) {
        return themes.computeIfAbsent(templateId, k -> new LinkedHashMap<>());
    }

    /** Tombol "Isi asli": semua percobaan dibuang. */
    public void reset(String templateId) {
        values.remove(templateId);
        themes.remove(templateId);
    }
}
