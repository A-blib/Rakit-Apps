package com.aris.templateapp.site;

import com.aris.templateapp.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Penyimpanan file website statis & rilis hasil bangun di disk server (folder {@code app.sites.root-dir}).
 * Struktur:
 * <pre>
 * data/{siteId}/releases/{version}/ (index.html, css/, img/, dsb.)
 * tmp/{buildId}/ (tempat kerja sementara proses bangun)
 * </pre>
 */
@Slf4j
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

    /**
     * Publish ulang menyalin seluruh website, padahal biasanya hanya HTML yang berubah. File hasil build yang isinya
     * sama persis dengan file di {@code previousRelease} diganti hardlink ke file lama: dua folder rilis tetap utuh,
     * tetapi isi file itu hanya tersimpan sekali di disk. Aman karena folder rilis tidak pernah diubah setelah dibuat,
     * dan menghapus satu rilis tidak menghapus isi file yang masih ditautkan rilis lain.
     *
     * @return jumlah byte yang tidak perlu disimpan ulang
     */
    public long linkUnchangedFiles(UUID buildId, Path previousRelease) {
        Path tmp = root.resolve("tmp").resolve(buildId.toString());
        if (!Files.isDirectory(previousRelease)) {
            return 0;
        }
        List<Path> files;
        try (Stream<Path> stream = Files.walk(tmp)) {
            files = stream.filter(p -> Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS)).toList();
        } catch (IOException e) {
            log.warn("Rilis {} tidak bisa dibandingkan; file disimpan utuh", previousRelease, e);
            return 0;
        }
        long saved = 0;
        for (Path file : files) {
            Path previous = previousRelease.resolve(tmp.relativize(file).toString());
            Path link = file.resolveSibling(file.getFileName() + ".link");
            try {
                if (!Files.isRegularFile(previous, LinkOption.NOFOLLOW_LINKS) || Files.mismatch(previous, file) != -1) {
                    continue;
                }
                long size = Files.size(file);
                // Tautan dibuat di nama sementara lalu ditukar, agar file tidak pernah hilang jika langkah ini gagal.
                Files.createLink(link, previous);
                Files.move(link, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                saved += size;
            } catch (IOException | UnsupportedOperationException e) {
                // Sistem file tanpa hardlink (mis. FAT): file salinan tetap dipakai, hanya tidak hemat ruang.
                try {
                    Files.deleteIfExists(link);
                } catch (IOException ignored) {
                }
            }
        }
        return saved;
    }

    /** Menghapus folder satu rilis. Isi file yang masih ditautkan rilis lain tetap ada (lihat linkUnchangedFiles). */
    public void deleteRelease(UUID siteId, int version) {
        try {
            FileSystemUtils.deleteRecursively(releasePath(siteId, version));
        } catch (IOException e) {
            log.warn("Folder rilis {} versi {} gagal dihapus", siteId, version, e);
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
