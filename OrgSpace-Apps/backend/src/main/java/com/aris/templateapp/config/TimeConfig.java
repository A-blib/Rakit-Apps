package com.aris.templateapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Sumber "waktu sekarang" untuk seluruh app. Class lain meminta {@link Clock} lewat konstruktor
 * alih-alih memanggil {@code Instant.now()}, sehingga test bisa memakai jam palsu (mis. untuk token kedaluwarsa).
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
