package com.aris.templateapp.site;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.organization.ActivityService;
import com.aris.templateapp.organization.Organization;
import com.aris.templateapp.organization.OrganizationRepository;
import com.aris.templateapp.project.Project;
import com.aris.templateapp.project.ProjectAssetStorage;
import com.aris.templateapp.project.ProjectRepository;
import com.aris.templateapp.template.Template;
import com.aris.templateapp.template.TemplateRepository;
import com.aris.templateapp.upload.UploadStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Membangun website dari data project di cloud menjadi halaman statis utuh (alur-publish-website.md 7.7).
 * Terdiri dari 6 tahap:
 * 1. Kumpulkan data project & aset
 * 2. Susun HTML, injeksi nilai isian & modul dinamis (berita, form)
 * 3. Periksa keamanan & kelengkapan (index.html, batas ukuran)
 * 4. Tulis folder rilis
 * 5. Update symlink host
 * 6. Catat rilis ke database
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SiteBuildService {

    private final ProjectRepository projectRepository;
    private final SiteRepository siteRepository;
    private final SiteReleaseRepository siteReleaseRepository;
    private final SiteBuildRepository siteBuildRepository;
    private final SiteNewsRepository siteNewsRepository;
    private final TemplateRepository templateRepository;
    private final OrganizationRepository organizationRepository;
    private final UploadStorage uploadStorage;
    private final SiteStorage siteStorage;
    private final ProjectAssetStorage assetStorage;
    private final AppProperties properties;
    private final ActivityService activityService;

    /** Menjalankan proses kompilasi & build website secara utuh. */
    @Transactional
    public SiteRelease build(UUID buildId) {
        SiteBuild build = siteBuildRepository.findById(buildId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Build tidak ditemukan"));
        build.setStatus("running");
        build.setStartedAt(Instant.now());
        siteBuildRepository.saveAndFlush(build);

        try {
            // Tahap 1: Kumpulkan data
            Project project = projectRepository.findById(build.getProjectId())
                    .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND));
            Site site = siteRepository.findById(build.getSiteId())
                    .orElseThrow(() -> new ApiException(ErrorCode.SITE_NOT_PUBLISHED));
            Organization org = organizationRepository.findById(project.getOrganizationId())
                    .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Organisasi tidak ditemukan"));

            Template template = null;
            if (project.getSourceTemplateId() != null) {
                template = templateRepository.findById(project.getSourceTemplateId()).orElse(null);
            }

            Map<String, Object> snapshot = project.getContent() != null ? new HashMap<>(project.getContent()) : new HashMap<>();
            int nextVersion = siteReleaseRepository.findFirstBySiteIdOrderByVersionDesc(site.getId())
                    .map(r -> r.getVersion() + 1).orElse(1);

            // Tahap 2: Susun file
            Path tmpDir = siteStorage.prepareTmpBuild(buildId);

            // Ekstrak paket template jika ada
            if (template != null) {
                Path pkgPath = uploadStorage.packageZipPath(template.getId());
                if (Files.exists(pkgPath)) {
                    extractZip(pkgPath, tmpDir);
                }
            }

            // Jika belum ada index.html (mis. template belum diekstrak atau project kosong), buat template default
            Path indexPath = tmpDir.resolve("index.html");
            if (!Files.exists(indexPath)) {
                createDefaultIndexHtml(indexPath, org, site);
            }

            // Proses semua file HTML di direktori kerja
            processHtmlFiles(tmpDir, snapshot, org, site);

            // Tahap 3: Periksa hasil
            if (!Files.exists(indexPath)) {
                throw new ApiException(ErrorCode.SITE_CONTENT_INVALID, "index.html tidak ditemukan dalam hasil build");
            }

            AtomicLong totalBytes = new AtomicLong(0);
            AtomicInteger fileCount = new AtomicInteger(0);
            try (Stream<Path> stream = Files.walk(tmpDir)) {
                stream.filter(Files::isRegularFile).forEach(p -> {
                    try {
                        totalBytes.addAndGet(Files.size(p));
                        fileCount.incrementAndGet();
                    } catch (IOException ignored) {
                    }
                });
            }

            long maxBytes = (long) properties.sites().outputMaxMb() * 1024 * 1024;
            if (totalBytes.get() > maxBytes) {
                throw new ApiException(ErrorCode.OUTPUT_TOO_LARGE, "Ukuran website melebihi batas " + properties.sites().outputMaxMb() + " MB");
            }

            // Tahap 4: Tulis rilis
            siteStorage.promoteTmpToRelease(buildId, site.getId(), nextVersion);

            // Tahap 5: Ganti Symlink / Update status host
            updateHostSymlink(site, nextVersion);

            // Tahap 6: Catat ke database
            SiteRelease release = new SiteRelease(site.getId(), nextVersion, snapshot, build.getKind(), build.getRequestedBy());
            release.setSizeBytes(totalBytes.get());
            release.setFileCount(fileCount.get());
            release = siteReleaseRepository.save(release);

            site.setCurrentReleaseId(release.getId());
            site.setStatus("online");
            site.setPublishedAt(Instant.now());
            site.setUpdatedAt(Instant.now());
            siteRepository.save(site);

            build.setStatus("succeeded");
            build.setReleaseId(release.getId());
            build.setFinishedAt(Instant.now());
            siteBuildRepository.save(build);

            activityService.log(org.getId(), build.getRequestedBy(), "site.published", "site", site.getId(),
                    Map.of("version", nextVersion, "slug", site.getSlug()));

            log.info("Website {} berhasil dibangun versi {} ({} file, {} bytes)", site.getSlug(), nextVersion, fileCount.get(), totalBytes.get());
            return release;

        } catch (Exception e) {
            log.error("Gagal membangun website untuk build {}", buildId, e);
            siteStorage.cleanTmp(buildId);
            build.setStatus("failed");
            build.setErrorCode(e instanceof ApiException ae ? ae.getErrorCode().name() : "BUILD_FAILED");
            build.setErrorDetail(Map.of("message", e.getMessage() != null ? e.getMessage() : "Terjadi kesalahan build"));
            build.setFinishedAt(Instant.now());
            siteBuildRepository.save(build);
            throw (e instanceof RuntimeException re ? re : new RuntimeException(e));
        }
    }

    private void extractZip(Path zipPath, Path destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().contains("..") || entry.getName().equals("manifest.json")) {
                    continue; // Skip manifest atau potensi zip slip
                }
                Path out = destDir.resolve(entry.getName()).normalize();
                if (!out.startsWith(destDir)) {
                    continue;
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(out);
                } else {
                    Files.createDirectories(out.getParent());
                    Files.copy(zis, out, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private void processHtmlFiles(Path tmpDir, Map<String, Object> snapshot, Organization org, Site site) throws IOException {
        try (Stream<Path> paths = Files.walk(tmpDir)) {
            List<Path> htmlFiles = paths.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".html")).toList();
            for (Path htmlFile : htmlFiles) {
                Document doc = Jsoup.parse(htmlFile.toFile(), "UTF-8");
                applySeoAndMeta(doc, org, site);
                applyFieldValues(doc, snapshot);
                injectDynamicModules(doc, org, site);
                stripMarkingAttributes(doc);
                Files.writeString(htmlFile, doc.outerHtml(), StandardCharsets.UTF_8);
            }
        }
    }

    private void applySeoAndMeta(Document doc, Organization org, Site site) {
        String title = site.getSeoTitle() != null && !site.getSeoTitle().isBlank()
                ? site.getSeoTitle() : (org.getName() + " — Website Resmi");
        if (doc.title() == null || doc.title().isBlank() || doc.selectFirst("title") == null) {
            if (doc.head() != null) {
                doc.head().appendElement("title").text(title);
            }
        } else {
            doc.title(title);
        }

        if (site.getSeoDescription() != null && !site.getSeoDescription().isBlank() && doc.head() != null) {
            Element metaDesc = doc.head().selectFirst("meta[name=description]");
            if (metaDesc == null) {
                doc.head().appendElement("meta").attr("name", "description").attr("content", site.getSeoDescription());
            } else {
                metaDesc.attr("content", site.getSeoDescription());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void applyFieldValues(Document doc, Map<String, Object> snapshot) {
        Map<String, Object> fields = (Map<String, Object>) snapshot.get("fields");
        if (fields != null) {
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                String key = entry.getKey();
                if (!(entry.getValue() instanceof Map<?, ?> valMap)) {
                    continue;
                }
                Map<String, Object> val = (Map<String, Object>) valMap;
                Elements elements = doc.select("[data-key=" + key + "], [data-edit=" + key + "]");
                for (Element el : elements) {
                    if (val.containsKey("text")) {
                        el.text(String.valueOf(val.get("text")));
                    }
                    if (val.containsKey("href")) {
                        el.attr("href", String.valueOf(val.get("href")));
                    }
                    if (val.containsKey("image")) {
                        String imgVal = String.valueOf(val.get("image"));
                        if (imgVal.startsWith("asset:")) {
                            imgVal = "/api/assets/" + imgVal.substring("asset:".length());
                        }
                        if ("img".equalsIgnoreCase(el.tagName())) {
                            el.attr("src", imgVal);
                        } else {
                            el.attr("style", appendOrUpdateStyle(el.attr("style"), "background-image", "url('" + imgVal + "')"));
                        }
                    }
                }
            }
        }

        // Terapkan kustomisasi gaya per-isian (color, background-color, font-size, border-radius/sudut)
        Map<String, Object> styles = (Map<String, Object>) snapshot.get("styles");
        if (styles != null) {
            for (Map.Entry<String, Object> entry : styles.entrySet()) {
                String key = entry.getKey();
                if (!(entry.getValue() instanceof Map<?, ?> valMap)) {
                    continue;
                }
                Map<String, Object> styleVals = (Map<String, Object>) valMap;
                Elements elements = doc.select("[data-key=" + key + "], [data-edit=" + key + "]");
                for (Element el : elements) {
                    String currentStyle = el.attr("style");
                    if (styleVals.containsKey("color")) {
                        currentStyle = appendOrUpdateStyle(currentStyle, "color", String.valueOf(styleVals.get("color")));
                    }
                    if (styleVals.containsKey("background-color")) {
                        currentStyle = appendOrUpdateStyle(currentStyle, "background-color", String.valueOf(styleVals.get("background-color")));
                    }
                    if (styleVals.containsKey("font-size")) {
                        currentStyle = appendOrUpdateStyle(currentStyle, "font-size", styleVals.get("font-size") + "px");
                    }
                    if (styleVals.containsKey("border-radius")) {
                        currentStyle = appendOrUpdateStyle(currentStyle, "border-radius", styleVals.get("border-radius") + "px");
                    }
                    el.attr("style", currentStyle);
                }
            }
        }

        // Terapkan variabel tema :root
        Map<String, Object> theme = (Map<String, Object>) snapshot.get("theme");
        if (theme != null && !theme.isEmpty()) {
            StringBuilder css = new StringBuilder(":root {");
            for (Map.Entry<String, Object> entry : theme.entrySet()) {
                css.append(" ").append(entry.getKey()).append(": ").append(entry.getValue()).append(";");
            }
            css.append(" }");
            if (doc.head() != null) {
                Element existingTheme = doc.head().selectFirst("style#orgspace-theme");
                if (existingTheme != null) {
                    existingTheme.text(css.toString());
                } else {
                    doc.head().appendElement("style").attr("id", "orgspace-theme").text(css.toString());
                }
            }
        }
    }

    private String appendOrUpdateStyle(String existingStyle, String prop, String value) {
        if (existingStyle == null || existingStyle.isBlank()) {
            return prop + ": " + value + ";";
        }
        String pattern = "(?i)" + Pattern.quote(prop) + "\\s*:\\s*[^;]+;?";
        if (existingStyle.matches(".*" + pattern + ".*")) {
            return existingStyle.replaceAll(pattern, prop + ": " + value + ";");
        }
        String trimmed = existingStyle.trim();
        if (!trimmed.endsWith(";")) {
            trimmed += ";";
        }
        return trimmed + " " + prop + ": " + value + ";";
    }

    private void injectDynamicModules(Document doc, Organization org, Site site) {
        // 1. Modul Berita (data-ws-list="berita")
        Elements newsLists = doc.select("[data-ws-list=berita]");
        for (Element listEl : newsLists) {
            Element itemTemplate = listEl.selectFirst("[data-ws-item]");
            Element emptyEl = listEl.selectFirst("[data-ws-empty]");
            if (itemTemplate == null) {
                continue;
            }

            List<SiteNews> news = siteNewsRepository.findByOrganizationIdOrderByPublishedAtDesc(org.getId());
            if (news.isEmpty()) {
                // Berikan data berita contoh bawaan bila belum ada
                news = List.of(
                        new SiteNews(org.getId(), "Pelantikan Pengurus " + org.getName(),
                                "Selamat bertugas bagi jajaran pengurus yang baru dilantik untuk masa bakti ini.",
                                "Dokumentasi dan sambutan lengkap...", null, null),
                        new SiteNews(org.getId(), "Rencana Kerja & Agenda Terdekat",
                                "Simak agenda kegiatan dan program unggulan yang akan segera diselenggarakan.",
                                "Program kerja terlaksana...", null, null)
                );
            }

            if (emptyEl != null) {
                emptyEl.remove();
            }

            // Hapus kartu template sebelum mengisi data baru
            itemTemplate.remove();

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("id", "ID"));
            for (SiteNews item : news) {
                Element clone = itemTemplate.clone();
                Element titleEl = clone.selectFirst("[data-ws-field=judul]");
                if (titleEl != null) titleEl.text(item.getTitle());

                Element dateEl = clone.selectFirst("[data-ws-field=tanggal]");
                if (dateEl != null) dateEl.text(LocalDate.now().format(fmt));

                Element summaryEl = clone.selectFirst("[data-ws-field=ringkasan]");
                if (summaryEl != null && item.getSummary() != null) summaryEl.text(item.getSummary());

                Element linkEl = clone.selectFirst("[data-ws-link=detail]");
                if (linkEl != null) linkEl.attr("href", "#");

                listEl.appendChild(clone);
            }
        }

        // 2. Modul Formulir Dinamis (data-ws-form)
        Elements formElements = doc.select("[data-ws-form]");
        for (Element formPlaceholder : formElements) {
            String formName = formPlaceholder.attr("data-ws-form");
            String formTitle = "pendaftaran".equalsIgnoreCase(formName)
                    ? "Formulir Pendaftaran " + org.getName()
                    : "Kotak Aspirasi & Pesan";

            String formHtml = String.format("""
                    <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 24px; max-width: 600px; margin: 24px auto; font-family: inherit;">
                      <h3 style="margin-top: 0; font-size: 1.25rem; color: #1e293b;">%s</h3>
                      <div id="orgspace-form-alert-%s" style="display: none; padding: 12px; border-radius: 6px; margin-bottom: 16px; font-size: 0.9rem;"></div>
                      <form id="orgspace-dynamic-form-%s">
                        <div style="margin-bottom: 14px;">
                          <label style="display: block; font-size: 0.875rem; font-weight: 600; color: #475569; margin-bottom: 4px;">Nama Lengkap *</label>
                          <input type="text" name="nama" required placeholder="Masukkan nama lengkap Anda" style="width: 100%%; padding: 10px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.95rem; box-sizing: border-box;">
                        </div>
                        <div style="margin-bottom: 14px;">
                          <label style="display: block; font-size: 0.875rem; font-weight: 600; color: #475569; margin-bottom: 4px;">Kelas / Departemen *</label>
                          <input type="text" name="kelas" required placeholder="Contoh: XII MIPA 1 / Divisi Humas" style="width: 100%%; padding: 10px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.95rem; box-sizing: border-box;">
                        </div>
                        <div style="margin-bottom: 14px;">
                          <label style="display: block; font-size: 0.875rem; font-weight: 600; color: #475569; margin-bottom: 4px;">WhatsApp / Nomor HP *</label>
                          <input type="tel" name="whatsapp" required placeholder="08xxxxxxxxxx" style="width: 100%%; padding: 10px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.95rem; box-sizing: border-box;">
                        </div>
                        <div style="margin-bottom: 18px;">
                          <label style="display: block; font-size: 0.875rem; font-weight: 600; color: #475569; margin-bottom: 4px;">Alasan / Catatan *</label>
                          <textarea name="pesan" required rows="3" placeholder="Tuliskan motivasi atau pesan Anda..." style="width: 100%%; padding: 10px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.95rem; box-sizing: border-box;"></textarea>
                        </div>
                        <button type="submit" style="background: #2563eb; color: #ffffff; padding: 12px 24px; border: none; border-radius: 6px; font-size: 1rem; font-weight: 600; cursor: pointer; width: 100%%;">Kirim Data</button>
                      </form>
                      <script>
                        (function() {
                          const form = document.getElementById('orgspace-dynamic-form-%s');
                          if (!form) return;
                          form.addEventListener('submit', async function(e) {
                            e.preventDefault();
                            const btn = form.querySelector('button[type="submit"]');
                            const alertBox = document.getElementById('orgspace-form-alert-%s');
                            btn.disabled = true;
                            btn.innerText = 'Mengirim...';
                            const formData = new FormData(form);
                            const payload = {};
                            formData.forEach((val, key) => payload[key] = val);
                            try {
                              const res = await fetch('/api/public/sites/%s/forms/%s', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify(payload)
                              });
                              const json = await res.json();
                              alertBox.style.display = 'block';
                              alertBox.style.background = '#dcfce7';
                              alertBox.style.color = '#166534';
                              alertBox.innerText = json.message || 'Pendaftaran berhasil dikirim!';
                              form.reset();
                            } catch(err) {
                              alertBox.style.display = 'block';
                              alertBox.style.background = '#fee2e2';
                              alertBox.style.color = '#991b1b';
                              alertBox.innerText = 'Gagal mengirim data. Silakan coba lagi.';
                            } finally {
                              btn.disabled = false;
                              btn.innerText = 'Kirim Data';
                            }
                          });
                        })();
                      </script>
                    </div>
                    """, formTitle, formName, formName, formName, formName, site.getSlug(), formName);

            formPlaceholder.html(formHtml);
        }
    }

    private void stripMarkingAttributes(Document doc) {
        for (Element el : doc.getAllElements()) {
            el.removeAttr("data-key");
            el.removeAttr("data-edit");
            el.removeAttr("data-label");
            el.removeAttr("data-tpl-id");
            el.removeAttr("data-ws-list");
            el.removeAttr("data-ws-item");
            el.removeAttr("data-ws-field");
            el.removeAttr("data-ws-link");
            el.removeAttr("data-ws-empty");
            el.removeAttr("data-ws-limit");
            el.removeAttr("data-ws-form");
        }
    }

    private void updateHostSymlink(Site site, int version) {
        Path hostsDir = Path.of(properties.sites().storageRootDir()).resolve("hosts");
        try {
            Files.createDirectories(hostsDir);
            Path symlink = hostsDir.resolve(site.getSlug() + "." + properties.sites().domain());
            Path target = siteStorage.releasePath(site.getId(), version);
            Files.deleteIfExists(symlink);
            try {
                Files.createSymbolicLink(symlink, target);
            } catch (UnsupportedOperationException | IOException ignored) {
                // Jika sistem tidak mendukung symlink di direktori ini, lewati
            }
        } catch (IOException ignored) {
        }
    }

    private void createDefaultIndexHtml(Path indexPath, Organization org, Site site) throws IOException {
        String html = String.format("""
                <!DOCTYPE html>
                <html lang="id">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>%s</title>
                  <style>
                    body { font-family: system-ui, -apple-system, sans-serif; margin: 0; padding: 20px; background: #f8fafc; color: #1e293b; line-height: 1.6; }
                    .container { max-width: 800px; margin: 0 auto; background: white; padding: 40px; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }
                    header { border-bottom: 2px solid #e2e8f0; padding-bottom: 24px; margin-bottom: 32px; text-align: center; }
                    h1 { color: #0f172a; margin: 0 0 8px 0; font-size: 2rem; }
                    .badge { display: inline-block; background: #e0f2fe; color: #0369a1; padding: 4px 12px; border-radius: 9999px; font-size: 0.875rem; font-weight: 600; }
                    .hero { text-align: center; padding: 20px 0 40px 0; }
                    .hero p { font-size: 1.15rem; color: #64748b; margin-bottom: 24px; }
                    .card { background: #f1f5f9; padding: 20px; border-radius: 12px; margin-bottom: 16px; }
                    footer { text-align: center; margin-top: 48px; color: #94a3b8; font-size: 0.875rem; }
                  </style>
                </head>
                <body>
                  <div class="container">
                    <header>
                      <span class="badge">Website Resmi</span>
                      <h1>%s</h1>
                      <p style="margin: 4px 0 0 0; color: #64748b;">Organisasi Pelajar & Mahasiswa</p>
                    </header>
                    <main>
                      <section class="hero">
                        <h2>Selamat Datang</h2>
                        <p>Wadah komunikasi, informasi program kerja, dan pendaftaran anggota resmi organisasi kami.</p>
                      </section>
                      <section data-ws-list="berita">
                        <article data-ws-item class="card">
                          <h3 data-ws-field="judul">Judul Berita</h3>
                          <span data-ws-field="tanggal" style="font-size: 0.85rem; color: #64748b;">Tanggal</span>
                          <p data-ws-field="ringkasan">Ringkasan berita atau kegiatan organisasi...</p>
                        </article>
                      </section>
                      <div data-ws-form="pendaftaran"></div>
                    </main>
                    <footer>
                      Dibuat dan diterbitkan melalui platform OrgSpace.
                    </footer>
                  </div>
                </body>
                </html>
                """, org.getName(), org.getName());
        Files.writeString(indexPath, html, StandardCharsets.UTF_8);
    }
}
