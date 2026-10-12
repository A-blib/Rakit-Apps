package com.aris.templateapp.admin;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Semua endpoint {@code /api/admin/**} khusus tim internal ({@code users.is_admin = true}). Dicek di satu tempat ini,
 * jadi controller admin baru otomatis terlindungi tanpa perlu ingat menambah pemeriksaan sendiri.
 * <p>
 * Status admin dibaca dari database setiap request (bukan dari klaim JWT), agar mencabut hak admin langsung berlaku
 * tanpa menunggu access token kedaluwarsa.
 */
@Component
@RequiredArgsConstructor
public class AdminGuard implements HandlerInterceptor {

    private final CurrentUser currentUser;
    private final JdbcClient jdbc;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        requireAdmin(currentUser.id());
        return true;
    }

    public void requireAdmin(UUID userId) {
        boolean admin = jdbc.sql("SELECT is_admin FROM users WHERE id = :id")
                .param("id", userId)
                .query(Boolean.class)
                .optional()
                .orElse(false);
        if (!admin) {
            throw new ApiException(ErrorCode.ADMIN_ONLY);
        }
    }
}
