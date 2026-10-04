package com.aris.templateapp.debug;

import static org.junit.Assert.assertTrue;

import com.aris.templateapp.data.local.ProjectEntity;
import com.aris.templateapp.data.model.ProjectMode;
import com.aris.templateapp.data.model.ProjectStatus;

import org.junit.Test;

import java.util.List;

/** Project contoh harus mencakup semua status, kedua mode, dan project yang diedit setelah diexport (6.1). */
public class SampleProjectsTest {

    @Test
    public void samplesCoverEveryCaseNeededForDemo() {
        List<ProjectEntity> samples = SampleProjects.create(1_000_000_000_000L);

        assertTrue(samples.size() >= 6 && samples.size() <= 8);
        for (ProjectStatus status : ProjectStatus.values()) {
            assertTrue(status.name(), samples.stream().anyMatch(p -> p.status == status));
        }
        for (ProjectMode mode : ProjectMode.values()) {
            assertTrue(mode.name(), samples.stream().anyMatch(p -> p.mode == mode));
        }
        assertTrue(samples.stream().anyMatch(ProjectEntity::hasChangesSinceExport));
        assertTrue(samples.stream().anyMatch(p -> p.status == ProjectStatus.EXPORTED && !p.hasChangesSinceExport()));
        assertTrue(samples.stream().allMatch(p -> p.sample));
    }
}
