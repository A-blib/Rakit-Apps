package com.aris.templateapp.site;

import com.aris.templateapp.config.AppProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Penyimpanan file website statis & rilis hasil bangun di disk server (folder {@code app.sites.root-dir}).
 * Struktur:
 * <pre>
 * data/{siteId}/releases/{version}/ (index.html, css/, img/, dsb.)
 * tmp/{buildId}/ (tempat kerja sementara proses bangun)
 * </pre>
 */
@Component
public class SiteStorage {

    private final Path root;

    public SiteStorage(AppProperties properties) {
        String rootDir = properties.sites() != null ? properties.sites().storageRootDir() : "./data/sites";
        this.root = Path.of(rootDir).toAbsolutePath().normalize();
    }

    public Path prepareTmpBuild(UUID buildId) {
        Path tmp = root.resolve("tmp").resolve(buildId.toString());
        try {
            FileSystemUtils.deleteRecursively(tmp);
            Files.createDirectories(tmp);
            return tmp;
        } catch (IOException e) {
            throw new UncheckedIOException("Gagal membuat direktori kerja bangun", e);
        }
    }

    public Path promoteTmpToRelease(UUID buildId, UUID siteId, int version) {
        Path tmp = root.resolve("tmp").resolve(buildId.toString());
        Path target = releasePath(siteId, version);
        try {
            FileSystemUtils.deleteRecursively(target);
            Files.createDirectories(target.getParent());
            Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE);
            return target;
        } catch (IOException e) {
            // Jika atomic move antar partisi tidak didukung, gunakan copy + delete
            try {
                FileSystemUtils.copyRecursively(tmp, target);
                FileSystemUtils.deleteRecursively(tmp);
                return target;
            } catch (IOException ex) {
                throw new UncheckedIOException("Gagal memindahkan hasil bangun ke folder rilis", ex);
            }
        }
    }

    public Path releasePath(UUID siteId, int version) {
        return root.resolve("data").resolve(siteId.toString()).resolve("releases").resolve(String.valueOf(version));
    }

    public void cleanTmp(UUID buildId) {
        try {
            FileSystemUtils.deleteRecursively(root.resolve("tmp").resolve(buildId.toString()));
        } catch (IOException ignored) {
        }
    }
}
