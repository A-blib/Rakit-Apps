package com.aris.templateapp.debug;

import com.aris.templateapp.data.local.ProjectEntity;
import com.aris.templateapp.data.model.ProjectMode;
import com.aris.templateapp.data.model.ProjectStatus;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Project contoh untuk tombol debug "Isi project contoh" (alur-pembuatan-website.md bagian 6.1): status, mode, dan
 * waktu yang beragam, termasuk project yang diedit setelah diexport. Hanya ada di build debug.
 */
public final class SampleProjects {

    private SampleProjects() {
    }

    /** @param now waktu sekarang (epoch ms); dijadikan parameter agar mudah diuji */
    public static List<ProjectEntity> create(long now) {
        List<ProjectEntity> projects = new ArrayList<>();
        projects.add(project("Toko Kue Bu Ani", ProjectMode.TEMPLATE, ProjectStatus.DRAFT, 3, now,
                ago(3, 0), ago(0, 2), null));
        projects.add(project("SMK Negeri 1 Contoh", ProjectMode.CUSTOM, ProjectStatus.EXPORTED, 0, now,
                ago(12, 0), ago(1, 0), ago(3, 0)));
        projects.add(project("Portofolio Rina", ProjectMode.TEMPLATE, ProjectStatus.READY, 0, now,
                ago(5, 0), ago(0, 5), null));
        projects.add(project("Karang Taruna RW 05", ProjectMode.CUSTOM, ProjectStatus.DRAFT, 5, now,
                ago(9, 0), ago(2, 0), null));
        projects.add(project("Kantor Desa Sukamaju", ProjectMode.TEMPLATE, ProjectStatus.EXPORTED, 0, now,
                ago(20, 0), ago(6, 1), ago(6, 0)));
        projects.add(project("Undangan Pernikahan", ProjectMode.CUSTOM, ProjectStatus.READY, 0, now,
                ago(15, 0), ago(10, 0), null));
        projects.add(project("Project tanpa nama", ProjectMode.CUSTOM, ProjectStatus.DRAFT, 8, now,
                ago(0, 1), ago(0, 0), null));
        return projects;
    }

    private static Duration ago(int days, int hours) {
        return Duration.ofDays(days).plusHours(hours).plusMinutes(days == 0 && hours == 0 ? 20 : 0);
    }

    private static ProjectEntity project(String name, ProjectMode mode, ProjectStatus status, int missing, long now,
                                         Duration createdAgo, Duration updatedAgo, Duration exportedAgo) {
        ProjectEntity project = new ProjectEntity(UUID.randomUUID().toString(), name, mode, status,
                now - createdAgo.toMillis(), now - updatedAgo.toMillis());
        project.missingCount = missing;
        project.lastExportedAt = exportedAgo == null ? null : now - exportedAgo.toMillis();
        project.sample = true;
        return project;
    }
}
