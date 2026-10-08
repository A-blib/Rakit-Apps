package com.aris.templateapp.core.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Peringatan kontras di tab Gaya (alur-buat-website-via-template.md bagian 6.5). */
public class ColorContrastTest {

    @Test
    public void blackOnWhiteIsMaximum() {
        assertEquals(21.0, ColorContrast.ratio("#000000", "#ffffff"), 0.01);
    }

    @Test
    public void similarColorsAreLow() {
        assertTrue(ColorContrast.isLow("#777777", "#888888"));
        assertFalse(ColorContrast.isLow("#1e3a8a", "rgb(255, 255, 255)"));
    }

    @Test
    public void transparentOrUnknownIsNotJudged() {
        assertNull(ColorContrast.ratio("rgba(0, 0, 0, 0)", "#ffffff"));
        assertFalse(ColorContrast.isLow("biru", "#ffffff"));
        assertEquals("#1e3a8a", ColorContrast.toHex("rgb(30, 58, 138)"));
    }
}
