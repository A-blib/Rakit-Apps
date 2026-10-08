package com.aris.templateapp.core.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.aris.templateapp.data.model.ProjectValues;
import com.aris.templateapp.data.model.TemplateManifest;

import org.junit.Test;

/** Pembuatan custom.css (alur-buat-website-via-template.md bagian 11.4). */
public class CustomCssTest {

    @Test
    public void previewAndExportSelectors() throws Exception {
        TemplateManifest manifest = TestPackages.manifest("toko-kue");
        ProjectValues values = new ProjectValues();
        values.theme.put("--primary", "#16A34A");
        values.style("nama_toko").put("color", "#1e3a8a");
        values.style("nama_toko").put("font-size", "40");

        assertEquals(":root { --primary: #16a34a; }\n"
                        + "[data-key=\"nama_toko\"] { color: #1e3a8a !important; font-size: 40px !important; }\n",
                CustomCss.build(manifest, values, CustomCss.Target.PREVIEW));
        assertEquals(":root { --primary: #16a34a; }\n"
                        + ".ws-nama_toko { color: #1e3a8a !important; font-size: 40px !important; }\n",
                CustomCss.build(manifest, values, CustomCss.Target.EXPORT));
    }

    @Test
    public void rawInputIsNeverWrittenToCss() throws Exception {
        TemplateManifest manifest = TestPackages.manifest("toko-kue");
        ProjectValues values = new ProjectValues();
        values.theme.put("--primary", "red;}body{display:none");
        values.style("nama_toko").put("color", "url(javascript:x)");
        values.style("nama_toko").put("font-size", "999");
        // Properti yang tidak diizinkan manifest diabaikan.
        values.style("deskripsi").put("color", "#000000");

        assertEquals("", CustomCss.build(manifest, values, CustomCss.Target.PREVIEW));
    }

    @Test
    public void colorAndSizeNormalisation() {
        assertEquals("#aabbcc", CustomCss.color("#ABC"));
        assertNull(CustomCss.color("blue"));
        assertEquals("40px", CustomCss.size("40px", 20, 48));
        assertNull(CustomCss.size("12", 20, 48));
        assertNull(CustomCss.size("4e1", 20, 48));
    }
}
