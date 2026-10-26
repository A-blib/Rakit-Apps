package com.aris.templateapp.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Menolak menyalakan backend production jika alamat publik belum diisi atau masih localhost. application.yml memberi
 * nilai localhost untuk laptop; tanpa pemeriksaan ini backend tetap menyala dan mengirim link localhost ke app dan
 * pengunjung. Placeholder di application-prod.yml saja tidak cukup: Spring Boot membiarkan placeholder yang tidak
 * ditemukan sebagai teks "${NAMA}" saat mengikat properti, bukan menggagalkan startup.
 */
@Component
@Profile("prod")
class ProductionConfigCheck {

    ProductionConfigCheck(AppProperties properties) {
        List<String> problems = problems(properties);
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Environment production belum lengkap: " + String.join(", ", problems)
                    + ". Isi di /etc/orgspace/backend.env (docs/deploy/Panduan-Hosting-OrgSpace.docx bab 4.3).");
        }
    }

    static List<String> problems(AppProperties properties) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("SITES_HOSTING_URL", properties.sites().hostingUrl());
        values.put("SITES_DOMAIN", properties.sites().domain());
        values.put("SITES_PUBLIC_IP", properties.sites().publicIp());
        values.put("SITES_CUSTOM_TARGET", properties.sites().customTarget());
        values.put("GITHUB_REDIRECT_URI", properties.github().redirectUri());
        return values.entrySet().stream()
                .filter(e -> e.getValue() == null || e.getValue().isBlank() || e.getValue().contains("${")
                        || e.getValue().contains("localhost") || e.getValue().contains("127.0.0.1"))
                .map(e -> e.getKey() + (e.getValue() == null || e.getValue().isBlank() || e.getValue().contains("${")
                        ? " (kosong)" : " (masih " + e.getValue() + ")"))
                .toList();
    }
}
