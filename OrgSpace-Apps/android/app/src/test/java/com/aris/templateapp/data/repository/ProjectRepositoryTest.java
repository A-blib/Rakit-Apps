package com.aris.templateapp.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import com.aris.templateapp.data.local.ProjectEntity;
import com.aris.templateapp.data.model.ProjectMode;
import com.aris.templateapp.data.model.ProjectStatus;

import org.junit.Test;

/** Bagian ProjectRepository yang tidak butuh database: pola pencarian dan aturan duplikat. */
public class ProjectRepositoryTest {

    @Test
    public void likePatternEscapesWildcardsAndIgnoresBlank() {
        assertNull(ProjectRepository.likePattern(null));
        assertNull(ProjectRepository.likePattern("   "));
        assertEquals("%toko kue%", ProjectRepository.likePattern("  Toko Kue "));
        assertEquals("%diskon 50\\%%", ProjectRepository.likePattern("Diskon 50%"));
        assertEquals("%a\\_b%", ProjectRepository.likePattern("a_b"));
    }

    @Test
    public void duplicateOfExportedProjectIsReadyAndNeverExported() {
        ProjectEntity source = new ProjectEntity("asal", "SMK 1", ProjectMode.CUSTOM, ProjectStatus.EXPORTED, 10, 20);
        source.lastExportedAt = 15L;
        source.sourceTemplateId = "tpl";

        ProjectEntity copy = ProjectRepository.copyOf(source, "SMK 1 (salinan)", "baru", 100);

        assertEquals("baru", copy.id);
        assertEquals("SMK 1 (salinan)", copy.name);
        assertEquals(ProjectStatus.READY, copy.status);
        assertNull(copy.lastExportedAt);
        assertEquals(100, copy.createdAt);
        assertEquals(100, copy.updatedAt);
        assertEquals("tpl", copy.sourceTemplateId);
        assertNotEquals(source.id, copy.id);
    }

    @Test
    public void duplicateOfDraftKeepsDraftAndMissingCount() {
        ProjectEntity source = new ProjectEntity("asal", "Toko", ProjectMode.TEMPLATE, ProjectStatus.DRAFT, 10, 20);
        source.missingCount = 3;

        ProjectEntity copy = ProjectRepository.copyOf(source, "Toko (salinan)", "baru", 100);

        assertEquals(ProjectStatus.DRAFT, copy.status);
        assertEquals(3, copy.missingCount);
    }

    @Test
    public void nextNameAddsNumberWhenTaken() {
        assertEquals("Toko Kue", ProjectRepository.nextName("Toko Kue", new java.util.HashSet<>()));
        assertEquals("Toko Kue (2)", ProjectRepository.nextName("Toko Kue",
                new java.util.HashSet<>(java.util.Arrays.asList("Toko Kue"))));
        assertEquals("Toko Kue (3)", ProjectRepository.nextName("Toko Kue",
                new java.util.HashSet<>(java.util.Arrays.asList("Toko Kue", "Toko Kue (2)"))));
    }
}
