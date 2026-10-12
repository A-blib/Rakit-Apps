package com.aris.templateapp.core.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Ekstrak paket template dari server (alur-buat-website-via-template.md bagian 5: keamanan ekstrak). */
public class PackageExtractorTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void validPackageIsExtracted() throws Exception {
        File target = temp.newFolder("paket");
        PackageExtractor.extract(zip(entries("manifest.json", "{}", "index.html", "<h1>Hai</h1>",
                "css/style.css", "body{}")), target, 1024);

        assertEquals("<h1>Hai</h1>", read(new File(target, "index.html")));
        assertTrue(new File(target, "css/style.css").isFile());
    }

    @Test
    public void zipSlipPathFailsTheWholePackage() throws Exception {
        File target = temp.newFolder("paket");
        assertThrows(PackageExtractor.InvalidPackageException.class, () -> PackageExtractor.extract(
                zip(entries("manifest.json", "{}", "index.html", "x", "../luar.txt", "jahat")), target, 1024));
        assertTrue(!new File(target.getParentFile(), "luar.txt").exists());
    }

    @Test
    public void absolutePathFails() throws Exception {
        File target = temp.newFolder("paket");
        assertThrows(PackageExtractor.InvalidPackageException.class, () -> PackageExtractor.extract(
                zip(entries("/etc/passwd", "x")), target, 1024));
    }

    @Test
    public void contentLargerThanLimitFails() throws Exception {
        File target = temp.newFolder("paket");
        assertThrows(PackageExtractor.InvalidPackageException.class, () -> PackageExtractor.extract(
                zip(entries("manifest.json", "{}", "index.html", "x".repeat(2000))), target, 1024));
    }

    @Test
    public void packageWithoutManifestFails() throws Exception {
        File target = temp.newFolder("paket");
        assertThrows(PackageExtractor.InvalidPackageException.class, () -> PackageExtractor.extract(
                zip(entries("index.html", "x")), target, 1024));
    }

    private static Map<String, String> entries(String... nameAndContent) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < nameAndContent.length; i += 2) {
            map.put(nameAndContent[i], nameAndContent[i + 1]);
        }
        return map;
    }

    private static ByteArrayInputStream zip(Map<String, String> files) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (Map.Entry<String, String> file : files.entrySet()) {
                zip.putNextEntry(new ZipEntry(file.getKey()));
                zip.write(file.getValue().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    private static String read(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }
}
