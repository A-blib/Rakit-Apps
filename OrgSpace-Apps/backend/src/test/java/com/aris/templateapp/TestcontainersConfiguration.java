package com.aris.templateapp;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Menyalakan PostgreSQL sungguhan di container Docker khusus untuk test.
 * {@code @ServiceConnection} otomatis mengarahkan datasource ke container ini,
 * jadi test tidak menyentuh database development di laptop.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        // Disamakan dengan versi mayor PostgreSQL di laptop (18) agar perilakunya sama.
        return new PostgreSQLContainer(DockerImageName.parse("postgres:18"));
    }

}
