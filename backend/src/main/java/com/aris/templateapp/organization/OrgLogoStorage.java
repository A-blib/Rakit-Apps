package com.aris.templateapp.organization;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.config.AppProperties;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Mengolah dan menyimpan logo organisasi. File asli tidak pernah disimpan atau disajikan: gambar dibaca ulang,
 * dipotong persegi di tengah, diperkecil maks 512 px, lalu ditulis ulang. Dengan begitu isi tersembunyi di file
 * (mis. script dalam SVG atau data setelah akhir gambar) ikut terbuang.
 * <p>
 * Hasil disimpan sebagai PNG, bukan WebP seperti di rancangan, karena Java bawaan belum bisa menulis WebP dan
 * library tambahan belum disetujui. App Android mengirim PNG/JPG yang sudah diperkecil.
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
        if (bytes == null || bytes.length == 0 || bytes.length > maxBytes) {
            throw new ApiException(ErrorCode.LOGO_INVALID,
                    "Logo maksimal " + (maxBytes / 1024) + " KB. Pilih gambar yang lebih kecil.");
        }
        // Jenis file dicek dari isinya (magic bytes), bukan dari nama atau Content-Type kiriman HP.
        if (!isPng(bytes) && !isJpeg(bytes)) {
            throw new ApiException(ErrorCode.LOGO_INVALID);
        }
        BufferedImage source;
        try {
            source = ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            source = null;
        }
        if (source == null) {
            throw new ApiException(ErrorCode.LOGO_INVALID);
        }
        write(organizationId, squareAndShrink(source));
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

    static BufferedImage squareAndShrink(BufferedImage source) {
        int side = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - side) / 2;
        int y = (source.getHeight() - side) / 2;
        int target = Math.min(side, MAX_SIZE_PX);
        // ARGB agar latar transparan logo tetap transparan.
        BufferedImage result = new BufferedImage(target, target, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(source, 0, 0, target, target, x, y, x + side, y + side, null);
        g.dispose();
        return result;
    }

    private void write(UUID organizationId, BufferedImage image) {
        try {
            Files.createDirectories(dir);
            // Tulis ke file sementara lalu pindahkan, agar pembaca tidak pernah melihat file setengah jadi.
            Path temp = Files.createTempFile(dir, organizationId.toString(), ".tmp");
            ImageIO.write(image, "png", temp.toFile());
            Files.move(temp, path(organizationId), StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path path(UUID organizationId) {
        return dir.resolve(organizationId + ".png");
    }

    private static boolean isPng(byte[] b) {
        return b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
    }

    private static boolean isJpeg(byte[] b) {
        return b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
    }
}
