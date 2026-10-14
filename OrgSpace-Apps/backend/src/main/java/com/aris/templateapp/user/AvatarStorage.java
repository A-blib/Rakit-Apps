package com.aris.templateapp.user;

import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.common.image.SquareImages;
import com.aris.templateapp.config.AppProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Menyimpan foto profil user: dipotong persegi di tengah, diperkecil maks 512 px, ditulis ulang sebagai PNG
 * ({@link SquareImages}, aturan sama dengan logo organisasi). Satu file per user, menimpa foto lama.
 */
@Component
public class AvatarStorage {

    static final int MAX_SIZE_PX = 512;

    private final Path dir;
    private final long maxBytes;

    public AvatarStorage(AppProperties properties) {
        this.dir = Path.of(properties.users().avatarDir()).toAbsolutePath().normalize();
        this.maxBytes = properties.users().avatarMaxKb() * 1024L;
    }

    public void save(UUID userId, byte[] bytes) {
        var source = SquareImages.decode(bytes, maxBytes, ErrorCode.AVATAR_INVALID,
                "Foto profil maksimal " + (maxBytes / 1024) + " KB. Pilih gambar yang lebih kecil.");
        SquareImages.writePng(SquareImages.squareAndShrink(source, MAX_SIZE_PX), path(userId));
    }

    /** File foto jika ada; null jika user belum mengunggah foto (mis. masih memakai foto Google atau inisial). */
    public Path file(UUID userId) {
        Path file = path(userId);
        return Files.isRegularFile(file) ? file : null;
    }

    public void delete(UUID userId) {
        try {
            Files.deleteIfExists(path(userId));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path path(UUID userId) {
        return dir.resolve(userId + ".png");
    }
}
