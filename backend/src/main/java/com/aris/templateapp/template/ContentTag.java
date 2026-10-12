package com.aris.templateapp.template;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

/**
 * "Isi template": konten khas yang dimiliki template (alur-fitur-upload.md 6.1, K9). Tampil sebagai label
 * "Punya: ..." di galeri. Tiga konten pertama adalah konten Inti organisasi (Peringatan ORG_CORE_CONTENT).
 */
public enum ContentTag {

    PROFIL("profil", "Profil / visi-misi", true),
    STRUKTUR("struktur", "Struktur kepengurusan", true),
    KONTAK("kontak", "Kontak + media sosial", true),
    PROKER("proker", "Program kerja", false),
    BERITA("berita", "Berita kegiatan", false),
    GALERI("galeri", "Galeri", false),
    AGENDA("agenda", "Agenda", false),
    OPREC("oprec", "Open recruitment", false),
    ASPIRASI("aspirasi", "Kotak aspirasi", false);

    private final String value;
    private final String label;
    private final boolean core;

    ContentTag(String value, String label, boolean core) {
        this.value = value;
        this.label = label;
        this.core = core;
    }

    @JsonValue
    public String value() {
        return value;
    }

    public String label() {
        return label;
    }

    public boolean isCore() {
        return core;
    }

    /** null jika kode tidak dikenal. */
    public static ContentTag fromValue(String value) {
        for (ContentTag tag : values()) {
            if (tag.value.equals(value)) {
                return tag;
            }
        }
        return null;
    }

    /** Konten Inti yang belum dicantumkan; kosong = semua konten Inti ada. */
    public static Set<ContentTag> missingCore(Collection<ContentTag> tags) {
        Set<ContentTag> missing = EnumSet.noneOf(ContentTag.class);
        for (ContentTag tag : values()) {
            if (tag.core && !tags.contains(tag)) {
                missing.add(tag);
            }
        }
        return missing;
    }
}
