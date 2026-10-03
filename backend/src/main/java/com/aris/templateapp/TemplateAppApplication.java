package com.aris.templateapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
// Mendaftarkan semua class @ConfigurationProperties (mis. AppProperties) tanpa perlu @Component.
@ConfigurationPropertiesScan
public class TemplateAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(TemplateAppApplication.class, args);
    }

}
