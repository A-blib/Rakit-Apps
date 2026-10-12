package com.aris.templateapp.monitoring;

import com.aris.templateapp.security.JwtAuthFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.UUID;

/**
 * Mencatat setiap request {@code /api/**} untuk dashboard monitoring. Dipasang paling luar (sebelum Spring Security),
 * sehingga request yang ditolak 401 juga terhitung.
 * <p>
 * Yang dicatat hanya pola endpoint, status, dan durasi; tidak ada isi request, query, atau IP.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class UsageTrackingFilter extends OncePerRequestFilter {

    /** Request yang tidak cocok dengan endpoint mana pun (404 / ditolak sebelum sampai controller). */
    static final String UNMATCHED = "(lainnya)";

    private final UsageRecorder recorder;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long start = System.nanoTime();
        int status = 500;
        try {
            chain.doFilter(request, response);
            status = response.getStatus();
        } finally {
            Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String route = pattern instanceof String p && p.startsWith("/api/") ? p : UNMATCHED;
            Object user = request.getAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE);
            recorder.record(request.getMethod(), route, status, (System.nanoTime() - start) / 1_000_000,
                    user instanceof UUID id ? id : null, ClientPlatform.of(request));
        }
    }
}
