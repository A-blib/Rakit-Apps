package com.aris.templateapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Aturan akses HTTP. Versi Fase 01: hanya membuka Swagger UI dan halaman error.
 * Endpoint auth publik dan filter JWT ditambahkan di Fase 02.
 */
@Configuration
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
            // Spring meneruskan error ke /error; tanpa ini error 400/500 berubah menjadi 401.
            "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API dipanggil app Android dengan token, bukan form browser ber-cookie,
                // jadi proteksi CSRF dan session server tidak diperlukan.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
