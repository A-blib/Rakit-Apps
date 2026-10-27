package com.aris.templateapp.common.image;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;

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

/**
 * Pengolah gambar bersama (logo organisasi, foto profil, gambar hero landing page). File asli tidak pernah disimpan
 * atau disajikan: gambar dibaca ulang, diperkecil (logo & foto profil juga dipotong persegi di tengah), lalu ditulis
 * ulang sebagai PNG. Dengan begitu isi tersembunyi di file (mis. script dalam SVG atau data setelah akhir gambar)
 * ikut terbuang.
 * <p>
 * PNG dipakai, bukan WebP, karena Java bawaan belum bisa menulis WebP dan library tambahan belum disetujui.
 */
public final class SquareImages {

    private SquareImages() {
    }

    /**
     * Memeriksa ukuran & jenis file (dari isinya/magic bytes, bukan nama atau Content-Type kiriman HP), lalu
     * membaca gambarnya.
     *
     * @param invalid kode error untuk file yang bukan PNG/JPG atau terlalu besar
     */
    public static BufferedImage decode(byte[] bytes, long maxBytes, ErrorCode invalid, String tooLargeMessage) {
        if (bytes == null || bytes.length == 0 || bytes.length > maxBytes) {
            throw new ApiException(invalid, tooLargeMessage);
        }
        if (!isPng(bytes) && !isJpeg(bytes)) {
            throw new ApiException(invalid);
        }
        BufferedImage source;
        try {
            source = ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            source = null;
        }
        if (source == null) {
            throw new ApiException(invalid);
        }
        return source;
    }

    public static BufferedImage squareAndShrink(BufferedImage source, int maxSidePx) {
        int side = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - side) / 2;
        int y = (source.getHeight() - side) / 2;
        int target = Math.min(side, maxSidePx);
        // ARGB agar latar transparan tetap transparan.
        BufferedImage result = new BufferedImage(target, target, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(source, 0, 0, target, target, x, y, x + side, y + side, null);
        g.dispose();
        return result;
    }

    /** Memperkecil agar sisi terpanjang maks {@code maxSidePx}, tanpa dipotong (mis. gambar hero landing page). */
    public static BufferedImage shrink(BufferedImage source, int maxSidePx) {
        double scale = Math.min(1.0, (double) maxSidePx / Math.max(source.getWidth(), source.getHeight()));
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(source, 0, 0, width, height, null);
        g.dispose();
        return result;
    }

    /** Menulis PNG ke {@code target}, menimpa yang lama. */
    public static void writePng(BufferedImage image, Path target) {
        try {
            Path dir = target.getParent();
            Files.createDirectories(dir);
            // Tulis ke file sementara lalu pindahkan, agar pembaca tidak pernah melihat file setengah jadi.
            Path temp = Files.createTempFile(dir, target.getFileName().toString(), ".tmp");
            ImageIO.write(image, "png", temp.toFile());
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean isPng(byte[] b) {
        return b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
    }

    private static boolean isJpeg(byte[] b) {
        return b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
    }
}
