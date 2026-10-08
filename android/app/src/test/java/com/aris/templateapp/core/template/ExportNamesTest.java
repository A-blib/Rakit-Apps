package com.aris.templateapp.core.template;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Nama file export (alur-buat-website-via-template.md bagian 8.4). */
public class ExportNamesTest {

    @Test
    public void fileNameFromProjectName() {
        assertEquals("dapur-mama-rina.zip", ExportNames.fileName("Dapur Mama Rina"));
        assertEquals("toko-kue-2.zip", ExportNames.fileName("Toko Kue (2)"));
        assertEquals("cafe-ole.zip", ExportNames.fileName("  Café  Olé! "));
        assertEquals("website.zip", ExportNames.fileName("!!!"));
    }

    @Test
    public void typedNameIsCleanedAndKeepsOneZipExtension() {
        assertEquals("sekolah-kita.zip", ExportNames.clean("Sekolah Kita.ZIP"));
        // Garis miring dibuang (hanya a–z, 0–9, dan tanda hubung), jadi tidak bisa membuat folder.
        assertEquals("ab.zip", ExportNames.clean("a/b"));
    }
}
