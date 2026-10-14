package com.aris.templateapp.gallery.dto;

import com.aris.templateapp.organization.OrgCategory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Detail template di layar Unduh (alur-buat-website-via-template.md bagian 5 & 12).
 *
 * @param contentTags      "Isi template" (kode, mis. profil, struktur, kontak) untuk label "Punya: ..."
 * @param pages            nama halaman, mis. ["Beranda", "Tentang"]
 * @param version          versi paket; HP melewati unduhan jika paket versi ini sudah tersimpan
 * @param packageSizeBytes ukuran paket ZIP; null jika template belum punya paket (template demo lama)
 * @param dynamicModules   modul dinamis yang didukung template (data-ws-*), mis. ["berita", "struktur"]
 */
public record TemplateDetailResponse(
        UUID id,
        String name,
        OrgCategory category,
        String description,
        List<String> keywords,
        List<String> contentTags,
        String creatorName,
        String thumbnailUrl,
        long downloads,
        Instant publishedAt,
        List<String> pages,
        List<String> libraries,
        boolean responsive,
        int version,
        Long packageSizeBytes,
        List<String> dynamicModules) {

    public TemplateDetailResponse(
            UUID id, String name, OrgCategory category, String description, List<String> keywords,
            List<String> contentTags, String creatorName, String thumbnailUrl, long downloads,
            Instant publishedAt, List<String> pages, List<String> libraries, boolean responsive,
            int version, Long packageSizeBytes) {
        this(id, name, category, description, keywords, contentTags, creatorName, thumbnailUrl,
                downloads, publishedAt, pages, libraries, responsive, version, packageSizeBytes, List.of());
    }
}
