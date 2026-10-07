package com.aris.templateapp.upload;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.config.AppProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

/**
 * Penyimpanan file upload di disk server (folder {@code app.upload.storage-dir}, bawaan {@code backend/uploads/}).
 * <pre>
 * uploads/sessions/{sessionId}.part         ← ZIP yang sedang dikirim per potongan
 * uploads/templates/{templateId}/source.zip ← ZIP terakhir yang lolos upload (diganti saat "Upload file perbaikan")
 * </pre>
 * Nama file selalu dari UUID buatan server, bukan dari nama file provider, sehingga tidak bisa dipakai untuk
 * menulis ke luar folder ini.
 */
@Component
public class UploadStorage {

    private static final String[] THUMBNAIL_EXTENSIONS = {"jpg", "png", "webp"};

    private final Path root;

    public UploadStorage(AppProperties properties) {
        this.root = Path.of(properties.upload().storageDir()).toAbsolutePath().normalize();
    }

    public void createSessionFile(UUID sessionId) {
        try {
            Files.createDirectories(sessionFile(sessionId).getParent());
            Files.write(sessionFile(sessionId), new byte[0]);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Menulis satu potongan di posisi {@code offset}. Jika koneksi putus di tengah potongan, posisi di database
     * belum maju, sehingga app cukup mengirim ulang potongan yang sama dan isinya tertimpa.
     *
     * @return jumlah byte yang ditulis
     */
    public long writeChunk(UUID sessionId, long offset, InputStream body, long maxBytes) {
        try (FileChannel channel = FileChannel.open(sessionFile(sessionId), StandardOpenOption.WRITE)) {
            channel.position(offset);
            byte[] buffer = new byte[64 * 1024];
            long written = 0;
            int n;
            while ((n = body.read(buffer)) > 0) {
                written += n;
                if (written > maxBytes) {
                    throw new ApiException(ErrorCode.VALIDATION_ERROR, "Potongan upload lebih besar dari yang diizinkan.");
                }
                channel.write(ByteBuffer.wrap(buffer, 0, n));
            }
            return written;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Memindahkan ZIP yang sudah lengkap menjadi ZIP template (menggantikan ZIP lama jika ada). */
    public void promote(UUID sessionId, UUID templateId) {
        try {
            Path target = sourceZip(templateId);
            Files.createDirectories(target.getParent());
            Files.move(sessionFile(sessionId), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public byte[] readSource(UUID templateId) {
        try {
            return Files.readAllBytes(sourceZip(templateId));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void deleteSession(UUID sessionId) {
        try {
            Files.deleteIfExists(sessionFile(sessionId));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void deleteTemplate(UUID templateId) {
        try {
            FileSystemUtils.deleteRecursively(root.resolve("templates").resolve(templateId.toString()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Menyimpan thumbnail (menggantikan yang lama, apa pun formatnya). */
    public void writeThumbnail(UUID templateId, byte[] image, String extension) {
        try {
            Path folder = root.resolve("templates").resolve(templateId.toString());
            Files.createDirectories(folder);
            for (String ext : THUMBNAIL_EXTENSIONS) {
                Files.deleteIfExists(folder.resolve("thumbnail." + ext));
            }
            Files.write(folder.resolve("thumbnail." + extension), image);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** File thumbnail template, atau null jika belum ada. */
    public Path thumbnail(UUID templateId) {
        Path folder = root.resolve("templates").resolve(templateId.toString());
        for (String ext : THUMBNAIL_EXTENSIONS) {
            Path file = folder.resolve("thumbnail." + ext);
            if (Files.exists(file)) {
                return file;
            }
        }
        return null;
    }

    /** Salinan bernomor untuk mode tandai (dibuat ulang jika ZIP sumber lebih baru). */
    public Path workZipPath(UUID templateId) {
        return root.resolve("templates").resolve(templateId.toString()).resolve("work.zip");
    }

    /** Paket template yang tayang (hasil Kirim). */
    public Path packageZipPath(UUID templateId) {
        return root.resolve("templates").resolve(templateId.toString()).resolve("package.zip");
    }

    public void writePackage(UUID templateId, byte[] zip) {
        try {
            Path target = packageZipPath(templateId);
            Files.createDirectories(target.getParent());
            Path temp = target.resolveSibling("package.zip.tmp");
            Files.write(temp, zip);
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Path sourceZipPath(UUID templateId) {
        return sourceZip(templateId);
    }

    private Path sessionFile(UUID sessionId) {
        return root.resolve("sessions").resolve(sessionId + ".part");
    }

    private Path sourceZip(UUID templateId) {
        return root.resolve("templates").resolve(templateId.toString()).resolve("source.zip");
    }
}
