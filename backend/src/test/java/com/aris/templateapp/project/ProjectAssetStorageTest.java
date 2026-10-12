package com.aris.templateapp.project;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectAssetStorageTest {

    // WebP lossless 1×1 piksel (VP8L) dan lossy 1×1 (VP8), masing-masing dibuat dengan cwebp.
    private static final String LOSSLESS = "UklGRhoAAABXRUJQVlA4TA0AAAAvAAAAEAcQERGIiP4HAA==";
    private static final String LOSSY = "UklGRiIAAABXRUJQVlA4IBYAAAAwAQCdASoBAAEADsD+JaQAA3AAAAAA";

    @Test
    void readsWebpSizeFromHeader() {
        ProjectAssetStorage.Image lossless = ProjectAssetStorage.inspect(Base64.getDecoder().decode(LOSSLESS));
        assertThat(lossless.mimeType()).isEqualTo("image/webp");
        assertThat(lossless.width()).isEqualTo(1);
        assertThat(lossless.height()).isEqualTo(1);
        ProjectAssetStorage.Image lossy = ProjectAssetStorage.inspect(Base64.getDecoder().decode(LOSSY));
        assertThat(lossy.width()).isEqualTo(1);
        assertThat(lossy.height()).isEqualTo(1);
    }

    @Test
    void rejectsNonImages() {
        assertThat(ProjectAssetStorage.inspect("<svg onload=alert(1)>".getBytes())).isNull();
    }
}
