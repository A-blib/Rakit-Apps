package com.aris.templateapp.seed;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ZIP situs contoh untuk template demo (hanya seeder dev), agar draft dan upload demo bisa dibuka di wizard Upload
 * seperti upload sungguhan. Versi "rusak" memakai {@code <base href>} sehingga gagal pengecekan.
 */
final class DemoSite {

    private DemoSite() {
    }

    static byte[] zip(String title, boolean broken) {
        Map<String, String> files = new LinkedHashMap<>();
        files.put("index.html", """
                <!DOCTYPE html>
                <html lang="id">
                <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>%s</title>
                %s<link rel="stylesheet" href="css/style.css">
                </head>
                <body>
                <header><nav><a href="index.html">Beranda</a> <a href="kontak.html">Kontak</a></nav></header>
                <main>
                <section id="hero"><h1>%s</h1><p>Website contoh untuk mencoba fitur Upload di Rakit.</p>
                <a class="btn" href="kontak.html">Hubungi kami</a></section>
                <section id="layanan"><h2>Layanan</h2><p>Tulis layanan utama di sini.</p></section>
                </main>
                <footer><p>%s</p></footer>
                </body>
                </html>
                """.formatted(title, broken ? "<base href=\"https://contoh.example.com/\">\n" : "", title, title));
        files.put("kontak.html", """
                <!DOCTYPE html>
                <html lang="id">
                <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Kontak</title>
                <link rel="stylesheet" href="css/style.css">
                </head>
                <body>
                <header><nav><a href="index.html">Beranda</a> <a href="kontak.html">Kontak</a></nav></header>
                <main><section id="kontak"><h1>Kontak</h1><p>Hubungi kami lewat WhatsApp setiap hari kerja.</p></section></main>
                <footer><p>%s</p></footer>
                </body>
                </html>
                """.formatted(title));
        files.put("css/style.css", """
                :root { --primary: #1e3a8a; --radius: 8px; }
                body { margin: 0; font-family: sans-serif; color: #222; }
                section { padding: 48px 24px; }
                .btn { background: var(--primary); color: #fff; padding: 8px 16px; border-radius: var(--radius); }
                @media (max-width: 768px) { section { padding: 24px 16px; } }
                """);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (Map.Entry<String, String> file : files.entrySet()) {
                zip.putNextEntry(new ZipEntry(file.getKey()));
                zip.write(file.getValue().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }
}
