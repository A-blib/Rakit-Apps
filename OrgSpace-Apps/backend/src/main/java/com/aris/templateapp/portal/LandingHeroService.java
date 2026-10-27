package com.aris.templateapp.portal;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.common.image.SquareImages;
import com.aris.templateapp.portal.dto.LandingHeroRequest;
import com.aris.templateapp.portal.dto.LandingHeroResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** Hero landing page portal web: dibaca publik, diubah tim internal dari dashboard (satu baris di {@code landing_hero}). */
@Service
public class LandingHeroService {

    /** Sama dengan batas multipart (spring.servlet.multipart.max-file-size). */
    static final long IMAGE_MAX_BYTES = 2L * 1024 * 1024;
    static final int IMAGE_MAX_SIDE_PX = 1600;

    private final JdbcClient jdbc;
    private final Clock clock;
    private final Path image;

    public LandingHeroService(JdbcClient jdbc, Clock clock, @Value("${app.landing.image-dir:./data/landing}") String imageDir) {
        this.jdbc = jdbc;
        this.clock = clock;
        this.image = Path.of(imageDir).toAbsolutePath().normalize().resolve("hero.png");
    }

    @Transactional(readOnly = true)
    public LandingHeroResponse get() {
        return jdbc.sql("SELECT * FROM landing_hero WHERE id = 1")
                .query((rs, row) -> new LandingHeroResponse(rs.getString("headline"), rs.getString("headline_highlight"),
                        rs.getString("subheadline"), rs.getString("primary_cta_label"),
                        rs.getString("secondary_cta_label"), rs.getString("secondary_cta_url"),
                        lines(rs.getString("highlights")), rs.getString("image_alt"),
                        instant(rs.getTimestamp("image_updated_at")), instant(rs.getTimestamp("updated_at"))))
                .single();
    }

    @Transactional
    public LandingHeroResponse update(UUID adminId, LandingHeroRequest request) {
        jdbc.sql("""
                        UPDATE landing_hero SET headline = :headline, headline_highlight = :highlight,
                               subheadline = :subheadline, primary_cta_label = :primary, secondary_cta_label = :secondary,
                               secondary_cta_url = :secondaryUrl, highlights = :highlights, image_alt = :imageAlt,
                               updated_by = :admin, updated_at = :now
                         WHERE id = 1""")
                .param("headline", request.headline().trim())
                .param("highlight", request.headlineHighlight().trim())
                .param("subheadline", request.subheadline().trim())
                .param("primary", request.primaryCtaLabel().trim())
                .param("secondary", request.secondaryCtaLabel().trim())
                .param("secondaryUrl", request.secondaryCtaUrl().trim())
                .param("highlights", String.join("\n", request.highlights().stream().map(String::trim).toList()))
                .param("imageAlt", request.imageAlt().trim())
                .param("admin", adminId).param("now", Timestamp.from(clock.instant()))
                .update();
        return get();
    }

    /** Gambar ditulis ulang sebagai PNG (isi tersembunyi ikut terbuang, lihat {@link SquareImages}), maks 1600 px. */
    @Transactional
    public LandingHeroResponse uploadImage(UUID adminId, byte[] bytes) {
        var source = SquareImages.decode(bytes, IMAGE_MAX_BYTES, ErrorCode.LANDING_IMAGE_INVALID,
                "Gambar hero maksimal " + (IMAGE_MAX_BYTES / 1024 / 1024) + " MB. Pilih gambar yang lebih kecil.");
        SquareImages.writePng(SquareImages.shrink(source, IMAGE_MAX_SIDE_PX), image);
        touchImage(adminId, clock.instant());
        return get();
    }

    /** Kembali ke gambar bawaan web. */
    @Transactional
    public LandingHeroResponse deleteImage(UUID adminId) {
        try {
            Files.deleteIfExists(image);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        touchImage(adminId, null);
        return get();
    }

    public Path imageFile() {
        if (!Files.isRegularFile(image)) {
            throw new ApiException(ErrorCode.NOT_FOUND);
        }
        return image;
    }

    private void touchImage(UUID adminId, Instant imageUpdatedAt) {
        jdbc.sql("UPDATE landing_hero SET image_updated_at = :image, updated_by = :admin, updated_at = :now WHERE id = 1")
                .param("image", imageUpdatedAt == null ? null : Timestamp.from(imageUpdatedAt))
                .param("admin", adminId).param("now", Timestamp.from(clock.instant()))
                .update();
    }

    private static List<String> lines(String value) {
        return value.isEmpty() ? List.of() : Arrays.asList(value.split("\n"));
    }

    private static Instant instant(Timestamp value) {
        return value == null ? null : value.toInstant();
    }
}
