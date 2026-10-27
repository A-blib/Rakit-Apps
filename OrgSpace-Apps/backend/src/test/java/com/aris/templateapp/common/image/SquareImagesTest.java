package com.aris.templateapp.common.image;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SquareImagesTest {

    @Test
    void shrinkKeepsAspectRatioWithoutCropping() {
        BufferedImage wide = SquareImages.shrink(new BufferedImage(2000, 1000, BufferedImage.TYPE_INT_RGB), 1600);
        assertThat(wide.getWidth()).isEqualTo(1600);
        assertThat(wide.getHeight()).isEqualTo(800);

        BufferedImage tall = SquareImages.shrink(new BufferedImage(900, 3000, BufferedImage.TYPE_INT_RGB), 1600);
        assertThat(tall.getWidth()).isEqualTo(480);
        assertThat(tall.getHeight()).isEqualTo(1600);
    }

    @Test
    void shrinkNeverEnlargesSmallImages() {
        BufferedImage small = SquareImages.shrink(new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB), 1600);
        assertThat(small.getWidth()).isEqualTo(300);
        assertThat(small.getHeight()).isEqualTo(200);
    }

    @Test
    void decodeRejectsNonImagesAndOversizedFiles() {
        assertThatThrownBy(() -> SquareImages.decode("<svg onload=alert(1)>".getBytes(), 1024,
                ErrorCode.LANDING_IMAGE_INVALID, "terlalu besar"))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> SquareImages.decode(new byte[2048], 1024, ErrorCode.LANDING_IMAGE_INVALID, "terlalu besar"))
                .hasMessage("terlalu besar");
    }
}
