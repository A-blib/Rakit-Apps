package com.aris.templateapp.core.upload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Cek kilat ZIP di HP (alur-fitur-upload.md bagian 4). */
public class ZipQuickCheckTest {

    private static final long MAX = 20L * 1024 * 1024;

    @Test
    public void zipWithIndexAtRootOrInWrapperFolderPasses() throws IOException {
        assertTrue(check("toko.zip", zip("index.html", "css/style.css")).passed());
        assertTrue(check("toko.zip", zip("toko-kue/index.html", "toko-kue/css/style.css", "__MACOSX/._index.html")).passed());
    }

    @Test
    public void missingIndexReportsDeeperFolder() throws IOException {
        ZipQuickCheck.Result result = check("toko.zip", zip("project/dist/index.html", "project/package.json", "README.md"));
        assertEquals(ZipQuickCheck.Problem.NO_INDEX, result.problem);
        assertEquals("project/dist/", result.deeperIndexFolder);
    }

    @Test
    public void rarAndNonZipAreRecognized() throws IOException {
        assertEquals(ZipQuickCheck.Problem.RAR, check("toko.rar", "Rar!xxxx".getBytes(StandardCharsets.UTF_8)).problem);
        assertEquals(ZipQuickCheck.Problem.RAR, check("toko.zip", "Rar!xxxx".getBytes(StandardCharsets.UTF_8)).problem);
        assertEquals(ZipQuickCheck.Problem.NOT_ZIP, check("toko.zip", "bukan zip".getBytes(StandardCharsets.UTF_8)).problem);
    }

    @Test
    public void corruptZipIsRejected() throws IOException {
        byte[] good = zip("index.html", "css/style.css");
        byte[] broken = Arrays.copyOf(good, 40);
        assertEquals(ZipQuickCheck.Problem.CORRUPT, check("toko.zip", broken).problem);
    }

    @Test
    public void tooLargeListsLargestFiles() throws IOException {
        byte[] data = zip("index.html", "img/hero.jpg");
        ZipQuickCheck.Result result = ZipQuickCheck.check("toko.zip", MAX + 1, MAX, () -> new ByteArrayInputStream(data));
        assertEquals(ZipQuickCheck.Problem.TOO_LARGE, result.problem);
        assertEquals(2, result.largestFiles.size());
        assertNull(result.deeperIndexFolder);
    }

    @Test
    public void rootDetectionMatchesServerRules() {
        assertEquals("", ZipPaths.findRoot(List.of("index.html", "a/b.css")));
        assertEquals("site/", ZipPaths.findRoot(List.of("site/index.html", "site/a.css")));
        assertNull(ZipPaths.findRoot(List.of("site/index.html", "other.txt")));
        assertTrue(ZipPaths.isUnsafe("../x.txt"));
        assertTrue(ZipPaths.isUnsafe("C:/x.txt"));
        assertTrue(ZipPaths.isJunk("a/.DS_Store"));
    }

    private static ZipQuickCheck.Result check(String name, byte[] data) throws IOException {
        return ZipQuickCheck.check(name, data.length, MAX, () -> new ByteArrayInputStream(data));
    }

    private static byte[] zip(String... names) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (String name : names) {
                zip.putNextEntry(new ZipEntry(name));
                zip.write(("isi " + name).getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return out.toByteArray();
    }
}
