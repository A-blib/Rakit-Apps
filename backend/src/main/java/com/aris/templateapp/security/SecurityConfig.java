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
import tools.jackson.databind.json.JsonMapper;

/**
 * Aturan akses HTTP: endpoint auth dan Swagger UI terbuka, sisanya wajib membawa access token.
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
            "/error"
    };

    private final JwtAuthFilter jwtAuthFilter;
    private final JsonMapper jsonMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API dipanggil app Android dengan token, bukan form browser ber-cookie,
                // jadi proteksi CSRF dan session server tidak diperlukan.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        // Event dilihat/didownload juga dikirim tamu (dihitung lewat install_id, alur-provider.md 3.5).
                        .requestMatchers(HttpMethod.POST, "/api/templates/*/events").permitAll()
                        // Galeri template boleh dilihat tamu (alur-pembuatan-website.md 6.2).
                        .requestMatchers(HttpMethod.GET, "/api/templates").permitAll()
                        // Artikel Panduan pengecekan upload (alur-fitur-upload.md 5.11) bisa dibaca siapa saja.
                        .requestMatchers(HttpMethod.GET, "/api/help/articles/*").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedEntryPoint()))
                // Filter JWT dipasang sebelum filter login bawaan Spring agar user sudah dikenali lebih dulu.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
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
