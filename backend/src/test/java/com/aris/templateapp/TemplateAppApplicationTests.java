package com.aris.templateapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/** Memastikan seluruh app bisa start: konfigurasi terbaca, Flyway jalan, Hibernate cocok dengan tabel. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
class TemplateAppApplicationTests {

    @Test
    void contextLoads() {
    }

}
