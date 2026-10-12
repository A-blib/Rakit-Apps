package com.aris.templateapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

// User dikenali lewat JWT (JwtAuthFilter), bukan lewat user bawaan Spring Security.
// Dimatikan agar Spring tidak membuat user "user" + password acak di log.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
// Mendaftarkan semua class @ConfigurationProperties (mis. AppProperties) tanpa perlu @Component.
@ConfigurationPropertiesScan
public class TemplateAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(TemplateAppApplication.class, args);
    }

}
