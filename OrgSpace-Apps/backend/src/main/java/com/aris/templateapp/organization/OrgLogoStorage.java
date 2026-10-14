package com.aris.templateapp.organization;

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
 * Mengolah dan menyimpan logo organisasi: dipotong persegi di tengah, diperkecil maks 512 px, ditulis ulang sebagai
 * PNG ({@link SquareImages}). App Android mengirim PNG/JPG yang sudah diperkecil.
 */
@Component
public class OrgLogoStorage {

    static final int MAX_SIZE_PX = 512;

    private final Path dir;
    private final long maxBytes;

    public OrgLogoStorage(AppProperties properties) {
        this.dir = Path.of(properties.org().logoDir()).toAbsolutePath().normalize();
        this.maxBytes = properties.org().logoMaxKb() * 1024L;
    }

    /** Menyimpan logo baru, menimpa yang lama. */
    public void save(UUID organizationId, byte[] bytes) {
        var source = SquareImages.decode(bytes, maxBytes, ErrorCode.LOGO_INVALID,
                "Logo maksimal " + (maxBytes / 1024) + " KB. Pilih gambar yang lebih kecil.");
        SquareImages.writePng(SquareImages.squareAndShrink(source, MAX_SIZE_PX), path(organizationId));
    }

    /** File logo jika ada; null jika organisasi belum punya logo. */
    public Path file(UUID organizationId) {
        Path file = path(organizationId);
        return Files.isRegularFile(file) ? file : null;
    }

    public void delete(UUID organizationId) {
        try {
            Files.deleteIfExists(path(organizationId));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path path(UUID organizationId) {
        return dir.resolve(organizationId + ".png");
    }
}
