package com.aris.templateapp.data.local;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.aris.templateapp.data.model.ProjectMode;
import com.aris.templateapp.data.model.ProjectStatus;

import org.junit.Test;

/** "Ada perubahan sejak export terakhir" = sudah diexport dan updated_at lebih baru dari last_exported_at (4.2). */
public class ProjectEntityTest {

    @Test
    public void changedAfterExport() {
        ProjectEntity project = project(ProjectStatus.EXPORTED, 200, 100L);
        assertTrue(project.hasChangesSinceExport());
    }

    @Test
    public void notChangedWhenEditedBeforeOrAtExport() {
        assertFalse(project(ProjectStatus.EXPORTED, 100, 100L).hasChangesSinceExport());
        assertFalse(project(ProjectStatus.EXPORTED, 90, 100L).hasChangesSinceExport());
    }

    @Test
    public void onlyExportedProjectsCanBeOutdated() {
        assertFalse(project(ProjectStatus.READY, 200, 100L).hasChangesSinceExport());
        assertFalse(project(ProjectStatus.EXPORTED, 200, null).hasChangesSinceExport());
    }

    private static ProjectEntity project(ProjectStatus status, long updatedAt, Long exportedAt) {
        ProjectEntity project = new ProjectEntity("id", "Nama", ProjectMode.TEMPLATE, status, 0, updatedAt);
        project.lastExportedAt = exportedAt;
        return project;
    }
}
