package com.aris.templateapp.project;

import com.aris.templateapp.config.AppProperties;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

/**
 * File gambar website di disk ({@code PROJECT_ASSETS_DIR/{orgId}/{sha256}.{ext}}). Nama file = hash isinya, jadi
 * gambar yang sama (unggahan ulang, website hasil duplikat) memakai satu file.
 * <p>
 * Server tidak bisa menulis WebP (Java bawaan hanya membaca PNG/JPEG), jadi gambar disimpan apa adanya setelah jenisnya
 * diperiksa dari isi file (bukan dari nama/header yang dikirim HP). HP sudah memperkecil dan mengompres ke WebP 80.
 */
@Component
public class ProjectAssetStorage {

    /** Jenis gambar yang dikenali dari isi file beserta ukurannya. */
    public record Image(String mimeType, String extension, int width, int height) {
    }

    private final Path root;

    public ProjectAssetStorage(AppProperties properties) {
        this.root = Path.of(properties.projects().assetsDir()).toAbsolutePath().normalize();
    }

    /** null jika bukan WebP/JPEG/PNG yang bisa dibaca. */
    public static Image inspect(byte[] bytes) {
        if (isWebp(bytes)) {
            int[] size = webpSize(bytes);
            return size == null ? null : new Image("image/webp", "webp", size[0], size[1]);
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                return null;
            }
            boolean png = bytes.length > 4 && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G';
            boolean jpeg = bytes.length > 3 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8;
            if (png) {
                return new Image("image/png", "png", image.getWidth(), image.getHeight());
            }
            return jpeg ? new Image("image/jpeg", "jpg", image.getWidth(), image.getHeight()) : null;
        } catch (IOException e) {
            return null;
        }
    }

    public static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Menulis file (jika belum ada) dan mengembalikan path relatif terhadap folder aset. */
    public String write(UUID organizationId, String sha256, String extension, byte[] bytes) {
        Path relative = Path.of(organizationId.toString(), sha256 + "." + extension);
        Path target = root.resolve(relative);
        try {
            if (!Files.exists(target)) {
                Files.createDirectories(target.getParent());
                Path temp = Files.createTempFile(target.getParent(), "upload", ".tmp");
                Files.write(temp, bytes);
                Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Gambar website gagal disimpan", e);
        }
        return relative.toString();
    }

    public Path file(String storagePath) {
        Path file = root.resolve(storagePath).normalize();
        if (!file.startsWith(root)) {
            throw new IllegalArgumentException("Path aset di luar folder aset");
        }
        return file;
    }

    private static boolean isWebp(byte[] b) {
        return b.length > 30 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
    }

    /** Lebar & tinggi dari header VP8 / VP8L / VP8X; null jika header tidak dikenal. */
    static int[] webpSize(byte[] b) {
        String chunk = new String(b, 12, 4, StandardCharsets.US_ASCII);
        switch (chunk) {
            case "VP8 " -> {
                // Bingkai kunci: 3 byte tag, 3 byte kode awal (9d 01 2a), lalu lebar & tinggi 14 bit.
                int width = ((b[27] & 0xFF) << 8 | (b[26] & 0xFF)) & 0x3FFF;
                int height = ((b[29] & 0xFF) << 8 | (b[28] & 0xFF)) & 0x3FFF;
                return new int[] {width, height};
            }
            case "VP8L" -> {
                int bits = (b[21] & 0xFF) | (b[22] & 0xFF) << 8 | (b[23] & 0xFF) << 16 | (b[24] & 0xFF) << 24;
                return new int[] {(bits & 0x3FFF) + 1, ((bits >> 14) & 0x3FFF) + 1};
            }
            case "VP8X" -> {
                int width = ((b[24] & 0xFF) | (b[25] & 0xFF) << 8 | (b[26] & 0xFF) << 16) + 1;
                int height = ((b[27] & 0xFF) | (b[28] & 0xFF) << 8 | (b[29] & 0xFF) << 16) + 1;
                return new int[] {width, height};
            }
            default -> {
                return null;
            }
        }
    }
}
