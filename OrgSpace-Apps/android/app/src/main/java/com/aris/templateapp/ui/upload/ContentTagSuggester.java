package com.aris.templateapp.ui.upload;

import androidx.annotation.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Saran "Isi template" dari nama halaman & nama file template (alur-fitur-upload.md 6.1), mis. halaman
 * {@code struktur.html} → Struktur kepengurusan. Saran hanya ditandai di chip; kreator tetap memilih sendiri.
 */
public final class ContentTagSuggester {

    /** Kode Isi template → kata yang biasa muncul di nama halaman/file template. */
    private static final Map<String, String[]> WORDS = new LinkedHashMap<>();

    static {
        WORDS.put("profil", new String[] {"profil", "profile", "tentang", "about", "visi", "misi"});
        WORDS.put("struktur", new String[] {"struktur", "pengurus", "kepengurusan", "anggota", "team", "tim"});
        WORDS.put("kontak", new String[] {"kontak", "contact", "hubungi"});
        WORDS.put("proker", new String[] {"proker", "program"});
        WORDS.put("berita", new String[] {"berita", "news", "artikel", "blog", "kabar"});
        WORDS.put("galeri", new String[] {"galeri", "gallery", "foto", "dokumentasi"});
        WORDS.put("agenda", new String[] {"agenda", "kegiatan", "event", "jadwal", "acara"});
        WORDS.put("oprec", new String[] {"oprec", "rekrut", "recruit", "pendaftaran", "daftar"});
        WORDS.put("aspirasi", new String[] {"aspirasi", "saran", "masukan"});
    }

    private ContentTagSuggester() {
    }

    /**
     * @param pages    nama file halaman dari info teknis, mis. ["index.html", "struktur.html"]
     * @param fileName nama file ZIP asal, mis. "aksara-osis-berita.zip"
     */
    public static Set<String> suggest(@Nullable List<String> pages, @Nullable String fileName) {
        StringBuilder text = new StringBuilder();
        if (pages != null) {
            for (String page : pages) {
                text.append(' ').append(page);
            }
        }
        if (fileName != null) {
            text.append(' ').append(fileName);
        }
        String haystack = text.toString().toLowerCase(Locale.ROOT);
        Set<String> result = new LinkedHashSet<>();
        for (Map.Entry<String, String[]> entry : WORDS.entrySet()) {
            for (String word : entry.getValue()) {
                if (haystack.contains(word)) {
                    result.add(entry.getKey());
                    break;
                }
            }
        }
        return result;
    }
}
