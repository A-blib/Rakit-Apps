package com.aris.templateapp.security;

import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.common.response.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

/**
 * Aturan akses HTTP: endpoint auth, public sites, dan Swagger UI terbuka, sisanya wajib membawa access token.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
            "/api/auth/**",
            // Alat bantu pengembangan; controller-nya hanya dibuat di profile dev (di prod path ini tidak ada).
            "/api/dev/**",
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
            // Spring meneruskan error ke /error; tanpa ini error 400/500 berubah menjadi 401.
            "/error",
            // Caddy On-Demand TLS ask endpoint (alur-publish-website.md 7.11)
            "/internal/caddy/ask"
    };

    private final JwtAuthFilter jwtAuthFilter;
    private final JsonMapper jsonMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API dipanggil app Android dengan token, bukan form browser ber-cookie,
                // jadi proteksi CSRF dan session server tidak diperlukan.
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Izinkan preflight OPTIONS dari browser untuk CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        // Event dilihat/didownload juga dikirim tamu (dihitung lewat install_id, alur-provider.md 3.5).
                        .requestMatchers(HttpMethod.POST, "/api/templates/*/events").permitAll()
                        // Galeri template boleh dilihat tamu (alur-pembuatan-website.md 6.2).
                        .requestMatchers(HttpMethod.GET, "/api/templates").permitAll()
                        // Artikel Panduan pengecekan upload (alur-fitur-upload.md 5.11) bisa dibaca siapa saja.
                        .requestMatchers(HttpMethod.GET, "/api/help/articles/*").permitAll()
                        // Thumbnail template tayang tampil di galeri tamu; pemeriksaan pemilik untuk draft ada di controller.
                        .requestMatchers(HttpMethod.GET, "/api/templates/*/thumbnail").permitAll()
                        // Detail & paket template untuk layar Unduh pembuat website (tamu juga boleh).
                        .requestMatchers(HttpMethod.GET, "/api/templates/*", "/api/templates/*/package").permitAll()
                        // Daftar kategori organisasi untuk form (juga dibaca sebelum login) dan logo organisasi
                        // (tampil di pratinjau undangan dan nanti di website organisasi).
                        .requestMatchers(HttpMethod.GET, "/api/meta/categories", "/api/organizations/*/logo").permitAll()
                        // Foto profil user: dimuat ImageView tanpa header login, seperti logo organisasi.
                        .requestMatchers(HttpMethod.GET, "/api/users/*/avatar").permitAll()
                        // Portal web: landing & pusat dokumentasi dibaca tanpa login (docs/rancangan/portal-web.md).
                        .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                        // Website organisasi yang tayang dapat diakses publik tanpa login.
                        .requestMatchers(HttpMethod.GET, "/sites/**").permitAll()
                        .requestMatchers(HttpMethod.HEAD, "/sites/**").permitAll()
                        // Pengiriman formulir publik (data-ws-form: pendaftaran, aspirasi) oleh pengunjung website.
                        .requestMatchers(HttpMethod.POST, "/api/public/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedEntryPoint()))
                // Filter JWT dipasang sebelum filter login bawaan Spring agar user sudah dikenali lebih dulu.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Dipanggil saat endpoint butuh login tetapi token tidak ada/tidak sah. Menulis ErrorResponse
     * yang sama dengan error lain, sehingga app tahu harus mencoba refresh token.
     */
    private AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(ErrorCode.UNAUTHORIZED.status().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            jsonMapper.writeValue(response.getWriter(),
                    ErrorResponse.of(ErrorCode.UNAUTHORIZED.name(), ErrorCode.UNAUTHORIZED.defaultMessage()));
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
