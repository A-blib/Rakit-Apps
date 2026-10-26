package com.aris.templateapp.security;

import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.site.SiteDomainRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Siapa yang boleh memanggil backend dari JavaScript di browser (CORS). Satu-satunya pemanggil seperti itu adalah
 * Portal Pengurus ({@code {hosting}/{slug}/admin}, fetch langsung ke API); app Android dan server portal web bukan
 * browser, jadi tidak terpengaruh. Yang diizinkan:
 * <ul>
 *   <li>alamat OrgSpace-Hosting ({@code SITES_HOSTING_URL}) dan varian {@code www.}-nya;</li>
 *   <li>domain kustom organisasi yang aktif (Portal juga bisa dibuka lewat {@code osis.sch.id/admin}).</li>
 * </ul>
 * Asal lain (termasuk {@code null} dari halaman website yang di-sandbox) tidak mendapat header CORS, sehingga browser
 * tidak mengizinkan halamannya membaca jawaban. Dulu semua asal diizinkan ({@code *}); itu aman selama login memakai
 * header Authorization, tetapi berubah menjadi celah jika suatu saat login memakai cookie.
 */
@Component
public class CorsOriginPolicy implements CorsConfigurationSource {

    private static final List<String> METHODS = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH");

    private final Set<String> hostingOrigins;
    private final SiteDomainRepository siteDomainRepository;

    public CorsOriginPolicy(AppProperties properties, SiteDomainRepository siteDomainRepository) {
        this.siteDomainRepository = siteDomainRepository;
        URI hosting = URI.create(properties.sites().hostingUrl());
        String port = hosting.getPort() > 0 ? ":" + hosting.getPort() : "";
        String host = hosting.getHost().toLowerCase(Locale.ROOT);
        this.hostingOrigins = Set.of(hosting.getScheme() + "://" + host + port,
                hosting.getScheme() + "://www." + host + port);
    }

    @Override
    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (origin == null || !isAllowed(origin)) {
            return null;
        }
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin(origin);
        config.setAllowedMethods(METHODS);
        config.addAllowedHeader(CorsConfiguration.ALL);
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);
        return config;
    }

    boolean isAllowed(String origin) {
        String normalized = origin.toLowerCase(Locale.ROOT);
        if (hostingOrigins.contains(normalized)) {
            return true;
        }
        URI uri;
        try {
            uri = URI.create(normalized);
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (uri.getHost() == null || !("https".equals(uri.getScheme()) || "http".equals(uri.getScheme()))) {
            return false;
        }
        // shortcut: satu query per request CORS dari domain kustom (Portal saja); cache jika ini mulai terasa lambat.
        return siteDomainRepository.findByHostnameAndStatus(uri.getHost(), "active").isPresent();
    }
}
