package com.aris.templateapp.organization;

import com.aris.templateapp.common.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

class AttemptLimiterTest {

    /** Jam yang bisa dimajukan, agar test jendela waktu tidak perlu menunggu sungguhan. */
    private static final class MovableClock extends Clock {
        private Instant now = Instant.parse("2026-10-26T03:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }

    private final MovableClock clock = new MovableClock();
    private final AttemptLimiter limiter = new AttemptLimiter(clock, 3, 5, 2);

    @Test
    void emailLimitIsCaseInsensitiveAndResetsAfterTheWindow() {
        limiter.checkLogin("10.0.0.1", "rina@mail.com");
        limiter.checkLogin("10.0.0.2", "RINA@mail.com");
        limiter.checkLogin("10.0.0.3", " rina@MAIL.com ");
        // Ganti IP tidak menolong: hitungan per email tetap jalan.
        assertThatThrownBy(() -> limiter.checkLogin("10.0.0.4", "rina@mail.com")).isInstanceOf(ApiException.class);
        // Email lain dari IP yang sama tetap boleh.
        assertThatNoException().isThrownBy(() -> limiter.checkLogin("10.0.0.4", "budi@mail.com"));

        clock.advance(Duration.ofSeconds(61));
        assertThatNoException().isThrownBy(() -> limiter.checkLogin("10.0.0.4", "rina@mail.com"));
    }

    @Test
    void ipLimitStopsOneAttackerTryingManyEmails() {
        for (int i = 0; i < 5; i++) {
            limiter.checkLogin("10.0.0.9", "akun" + i + "@mail.com");
        }
        assertThatThrownBy(() -> limiter.checkLogin("10.0.0.9", "akun-lain@mail.com")).isInstanceOf(ApiException.class);
        assertThatNoException().isThrownBy(() -> limiter.checkLogin("10.0.0.10", "akun-lain@mail.com"));
    }

    @Test
    void formLimitIsPerIpPerWebsite() {
        limiter.checkFormSubmission("10.0.0.1", "osis-smansa");
        limiter.checkFormSubmission("10.0.0.1", "OSIS-SMANSA");
        assertThatThrownBy(() -> limiter.checkFormSubmission("10.0.0.1", "osis-smansa"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Terlalu banyak kiriman dari jaringan ini. Tunggu sekitar 1 menit, lalu kirim lagi.");
        // Website lain dan pengunjung lain tidak ikut tertahan.
        assertThatNoException().isThrownBy(() -> limiter.checkFormSubmission("10.0.0.1", "pramuka-tunas"));
        assertThatNoException().isThrownBy(() -> limiter.checkFormSubmission("10.0.0.2", "osis-smansa"));
    }

    @Test
    void idleKeysAreForgotten() {
        limiter.checkLogin("10.0.0.1", "rina@mail.com");
        assertThat(limiter.trackedKeys()).isEqualTo(2);

        clock.advance(Duration.ofMinutes(5));
        limiter.forgetIdleKeys();
        assertThat(limiter.trackedKeys()).as("belum lewat FORGET_AFTER").isEqualTo(2);

        clock.advance(AttemptLimiter.FORGET_AFTER);
        limiter.forgetIdleKeys();
        assertThat(limiter.trackedKeys()).isZero();
    }
}
