package com.aris.templateapp.core.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.aris.templateapp.data.model.ProjectStatus;
import com.aris.templateapp.data.model.ProjectValues;
import com.aris.templateapp.data.model.TemplateManifest;

import org.junit.Before;
import org.junit.Test;

/** Semua cabang aturan kelengkapan (alur-buat-website-via-template.md bagian 7.1). */
public class CompletenessCheckerTest {

    private TemplateManifest manifest;
    private ProjectValues values;

    @Before
    public void setUp() throws Exception {
        manifest = TestPackages.manifest("toko-kue");
        values = new ProjectValues();
    }

    @Test
    public void untouchedTemplateIsDraftWithAllRequiredStillSample() {
        CompletenessChecker.Result result = CompletenessChecker.check(manifest, values);

        // nama_toko, deskripsi, foto_hero, tombol_pesan (link WhatsApp contoh) masih contoh.
        assertEquals(4, result.missingCount());
        assertEquals(0, result.completeRequired);
        assertEquals(4, result.totalRequired);
        assertEquals(CompletenessChecker.Reason.SAMPLE, result.reasonFor("nama_toko"));
        assertEquals(CompletenessChecker.Reason.SAMPLE, result.reasonFor("foto_hero"));
        // Isian opsional yang masih contoh hanya saran.
        assertTrue(result.isSuggestion("link_ig"));
        assertEquals(Integer.valueOf(1), result.missingBySection.get("s1"));
        assertEquals(Integer.valueOf(3), result.missingBySection.get("s2"));
        assertEquals(ProjectStatus.DRAFT, CompletenessChecker.status(result.missingCount(), null));
    }

    @Test
    public void requiredEmptyIsMissing() {
        values.field("nama_toko").text = "   ";
        assertEquals(CompletenessChecker.Reason.EMPTY, CompletenessChecker.check(manifest, values).reasonFor("nama_toko"));
    }

    @Test
    public void typingTheSampleTextBackCountsAsSample() {
        values.field("nama_toko").text = " Toko Kue Bu Ani ";
        assertEquals(CompletenessChecker.Reason.SAMPLE, CompletenessChecker.check(manifest, values).reasonFor("nama_toko"));
    }

    @Test
    public void invalidLinkIsMissingEvenWhenOptional() {
        values.field("link_ig").href = "javascript:alert(1)";
        CompletenessChecker.Result result = CompletenessChecker.check(manifest, values);
        assertEquals(CompletenessChecker.Reason.INVALID_LINK, result.reasonFor("link_ig"));
        assertFalse(result.isSuggestion("link_ig"));
    }

    @Test
    public void buttonWithSampleWhatsappNumberIsSampleUntilLinkChanges() {
        values.field("tombol_pesan").text = "Pesan sekarang";
        assertEquals(CompletenessChecker.Reason.SAMPLE, CompletenessChecker.check(manifest, values).reasonFor("tombol_pesan"));

        values.field("tombol_pesan").href = "https://wa.me/6289876543210";
        assertNull(CompletenessChecker.check(manifest, values).reasonFor("tombol_pesan"));
    }

    @Test
    public void buttonWithEmptyTextIsMissing() {
        values.field("tombol_pesan").text = "";
        values.field("tombol_pesan").href = "https://wa.me/6289876543210";
        assertEquals(CompletenessChecker.Reason.EMPTY, CompletenessChecker.check(manifest, values).reasonFor("tombol_pesan"));
    }

    @Test
    public void allRequiredFilledIsReadyAndExportedStaysExported() {
        values.field("nama_toko").text = "Dapur Mama Rina";
        values.field("deskripsi").text = "Kue lezat untuk keluarga.";
        values.field("foto_hero").image = "images/foto_hero-1.webp";
        values.field("tombol_pesan").href = "https://wa.me/6289876543210";

        CompletenessChecker.Result result = CompletenessChecker.check(manifest, values);
        assertEquals(0, result.missingCount());
        assertEquals(4, result.completeRequired);
        assertEquals(ProjectStatus.READY, CompletenessChecker.status(0, null));
        assertEquals(ProjectStatus.EXPORTED, CompletenessChecker.status(0, 1000L));
        assertEquals(ProjectStatus.EXPORTED, CompletenessChecker.status(3, 1000L));
    }

    @Test
    public void internalSampleLinkIsAlreadyFine() {
        TemplateManifest.Field link = manifest.field("link_ig");
        link.sample = "kontak.html";
        link.required = true;
        assertNull(CompletenessChecker.check(manifest, values).reasonFor("link_ig"));
    }
}
