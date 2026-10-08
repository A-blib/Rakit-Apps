package com.aris.templateapp.core.template;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Potong tengah & perkecil foto (alur-buat-website-via-template.md bagian 6.6). */
public class ImageProcessorTest {

    @Test
    public void centerCropToRatio() {
        // 4000x3000 → 16:9 = 4000x2250, digeser ke tengah.
        assertArrayEquals(new int[] {0, 375, 4000, 2250}, ImageProcessor.cropRect(4000, 3000, "16:9"));
        // Foto tegak ke 1:1.
        assertArrayEquals(new int[] {0, 500, 3000, 3000}, ImageProcessor.cropRect(3000, 4000, "1:1"));
        // Foto sangat lebar ke 4:3: sisi kiri-kanan dipotong.
        assertArrayEquals(new int[] {200, 0, 400, 300}, ImageProcessor.cropRect(800, 300, "4:3"));
        // Tanpa rasio / rasio rusak: utuh.
        assertArrayEquals(new int[] {0, 0, 800, 600}, ImageProcessor.cropRect(800, 600, null));
        assertArrayEquals(new int[] {0, 0, 800, 600}, ImageProcessor.cropRect(800, 600, "x"));
    }

    @Test
    public void sampleSizeKeepsAtLeastMaxSide() {
        assertEquals(4, ImageProcessor.sampleSize(8000, 6000, 1920));
        assertEquals(1, ImageProcessor.sampleSize(1600, 1200, 1920));
    }
}
