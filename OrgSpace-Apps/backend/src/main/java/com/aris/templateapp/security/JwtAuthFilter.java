package com.aris.templateapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Dijalankan sekali untuk setiap request. Jika ada header {@code Authorization: Bearer <token>} yang sah,
 * ID user disimpan di SecurityContext sehingga request dianggap sudah login.
 * <p>
 * Token yang salah tidak langsung ditolak di sini: request diteruskan tanpa login, lalu
 * SecurityConfig yang menolak (401) jika endpoint-nya memang butuh login.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    public static final String USER_ID_ATTRIBUTE = "orgspace.userId";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            jwtService.parseUserId(header.substring(BEARER_PREFIX.length())).ifPresent(userId -> {
                // Principal = UUID user. Belum ada peran/otoritas, jadi daftarnya kosong.
                var authentication = new UsernamePasswordAuthenticationToken(userId, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // SecurityContext dikosongkan lagi setelah rantai filter Security selesai, sedangkan pencatat
                // monitoring (UsageTrackingFilter) membungkus rantai itu; ID user dititipkan lewat atribut request.
                request.setAttribute(USER_ID_ATTRIBUTE, userId);
            });
        }
        chain.doFilter(request, response);
    }
}
