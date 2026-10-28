package com.aris.templateapp.gallery;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.gallery.dto.TemplatePreviewResponse;
import com.aris.templateapp.site.PortalConfig;
import com.aris.templateapp.site.PublicSiteController;
import com.aris.templateapp.site.dto.SiteRenderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Preview template untuk pengunjung landing (belum login): file paket template + data contoh untuk modul data-ws-*.
 * Hanya template yang tayang di galeri ({@link TemplatePackageService#packageFile}), dan paketnya memang sudah publik.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplatePreviewService {

    /** Banyak item contoh per modul: cukup untuk melihat susunan kartu, tetap ringan. */
    static final int SAMPLE_ITEMS = 3;

    /** Format tanggal berita sama dengan website terbit (PublicSiteController). */
    private static final DateTimeFormatter NEWS_DATE = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.of("id", "ID"));

    private final TemplatePackageService packageService;

    public TemplatePreviewResponse preview(UUID id) {
        TemplatePackageService.Package pkg = packageService.packageFile(id);
        String html = allPagesHtml(pkg);
        Map<String, Map<String, String>> modules = PublicSiteController.templateModules(html);

        Map<String, List<Map<String, Object>>> collections = new LinkedHashMap<>();
        modules.forEach((name, fields) -> collections.put(name, sampleItems(fields)));

        List<SiteRenderResponse.NewsItem> news = new ArrayList<>();
        for (int i = 1; i <= SAMPLE_ITEMS; i++) {
            news.add(new SiteRenderResponse.NewsItem(
                    UUID.nameUUIDFromBytes(("berita-contoh-" + i).getBytes(StandardCharsets.UTF_8)),
                    "Kegiatan contoh " + i,
                    "Ringkasan kegiatan contoh. Isi asli diisi pengurus lewat Portal Pengurus.",
                    "Ini isi berita contoh untuk melihat tampilan template. Website asli menampilkan berita organisasi.",
                    LocalDate.now().minusDays(7L * i).format(NEWS_DATE),
                    null));
        }

        Map<String, Long> stats = new LinkedHashMap<>();
        collections.forEach((name, items) -> stats.put(name, (long) items.size()));
        stats.put("pengurus", 12L);
        stats.put("berita", (long) news.size());
        for (var form : Jsoup.parse(html).select("[data-ws-form]")) {
            stats.put("form." + form.attr("data-ws-form").strip().toLowerCase(Locale.ROOT), 25L);
        }

        return new TemplatePreviewResponse(id, packageService.visible(id).getName(), news, collections, modules,
                stats, sampleOrg(), portalConfig(pkg, modules));
    }

    /** Satu file dari paket (CSS, JS, gambar, HTML). Path dicari sebagai nama entri ZIP, bukan path di disk. */
    public byte[] file(UUID id, String path) {
        TemplatePackageService.Package pkg = packageService.packageFile(id);
        try (ZipFile zip = new ZipFile(pkg.file().toFile())) {
            ZipEntry entry = zip.getEntry(path);
            if (entry == null || entry.isDirectory()) {
                throw new ApiException(ErrorCode.NOT_FOUND);
            }
            try (InputStream in = zip.getInputStream(entry)) {
                return in.readAllBytes();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Gagal membaca paket template " + id, e);
        }
    }

    /** Semua halaman HTML paket digabung, agar modul di halaman selain beranda ikut terbaca. */
    private static String allPagesHtml(TemplatePackageService.Package pkg) {
        StringBuilder html = new StringBuilder();
        try (ZipFile zip = new ZipFile(pkg.file().toFile())) {
            for (ZipEntry entry : Collections.list(zip.entries())) {
                if (!entry.isDirectory() && entry.getName().toLowerCase(Locale.ROOT).endsWith(".html")) {
                    try (InputStream in = zip.getInputStream(entry)) {
                        html.append(new String(in.readAllBytes(), StandardCharsets.UTF_8));
                    }
                }
            }
        } catch (IOException e) {
            log.warn("Gagal membaca halaman paket {}", pkg.file(), e);
        }
        return html.toString();
    }

    /** portal.json paket (opsional), agar Portal demo tampil sama dengan Portal website asli. */
    private static Map<String, Object> portalConfig(TemplatePackageService.Package pkg,
                                                    Map<String, Map<String, String>> modules) {
        try (ZipFile zip = new ZipFile(pkg.file().toFile())) {
            ZipEntry entry = zip.getEntry(PortalConfig.FILE_NAME);
            if (entry == null) {
                return Map.of();
            }
            try (InputStream in = zip.getInputStream(entry)) {
                return PortalConfig.read(new String(in.readAllBytes(), StandardCharsets.UTF_8), modules,
                        path -> zip.getEntry(path) != null).config();
            }
        } catch (IOException e) {
            log.warn("Gagal membaca portal.json paket {}", pkg.file(), e);
            return Map.of();
        }
    }

    /** Field gambar sengaja kosong: runtime Hosting lalu memakai foto contoh bawaan template. */
    static List<Map<String, Object>> sampleItems(Map<String, String> fields) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 1; i <= SAMPLE_ITEMS; i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", "contoh-" + i);
            for (Map.Entry<String, String> field : fields.entrySet()) {
                String label = field.getKey().replace('_', ' ').replace('-', ' ');
                switch (field.getValue()) {
                    case "paragraf" -> item.put(field.getKey(), "Contoh " + label + ". Teks ini hanya data contoh untuk melihat tampilan template.");
                    case "angka" -> item.put(field.getKey(), String.valueOf(i * 10));
                    case "tanggal" -> item.put(field.getKey(), LocalDate.now().minusDays(7L * i).toString());
                    case "link" -> item.put(field.getKey(), "#");
                    case "gambar" -> { }
                    default -> item.put(field.getKey(), capitalize(label) + " contoh " + i);
                }
            }
            items.add(item);
        }
        return items;
    }

    /** Profil organisasi buatan untuk data-ws-org. Email memakai domain contoh (example.com) yang tidak bisa dikirimi. */
    private static Map<String, String> sampleOrg() {
        Map<String, String> org = new LinkedHashMap<>();
        org.put("nama", "Organisasi Contoh");
        org.put("singkatan", "ORCO");
        org.put("kategori", "Organisasi");
        org.put("institusi", "Sekolah Contoh");
        org.put("kota", "Bandung");
        org.put("alamat", "Jl. Contoh No. 1, Bandung");
        org.put("email", "halo@example.com");
        org.put("whatsapp", "081234567890");
        org.put("instagram", "@organisasicontoh");
        return org;
    }

    private static String capitalize(String text) {
        return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
