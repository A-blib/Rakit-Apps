package com.aris.templateapp.admin;

import com.aris.templateapp.admin.dto.AdminMeResponse;
import com.aris.templateapp.admin.dto.AdminUserResponse;
import com.aris.templateapp.admin.dto.SystemInfoResponse;
import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.common.response.PageResponse;
import com.aris.templateapp.config.AppProperties;
import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.security.CurrentUser;
import com.aris.templateapp.user.User;
import com.aris.templateapp.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

@Tag(name = "Admin", description = "Akun tim internal, daftar pengguna, kesehatan sistem (khusus admin)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

    private final CurrentUser currentUser;
    private final UserRepository userRepository;
    private final AdminUserQueries userQueries;
    private final JdbcClient jdbc;
    private final Environment environment;
    private final AppProperties properties;
    private final Clock clock;

    @Operation(summary = "Akun admin yang sedang login",
            description = "Dipanggil portal web setelah login. 403 ADMIN_ONLY jika akun bukan tim internal.")
    @GetMapping("/me")
    public AdminMeResponse me() {
        User user = userRepository.findById(currentUser.id()).orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));
        return new AdminMeResponse(user.getId(), user.getDisplayName(), user.getEmail(), user.getAvatarUrl());
    }

    @Operation(summary = "Daftar pengguna", description = "q = cari nama/email; role = creator, provider, admin. page mulai 0.")
    @GetMapping("/users")
    public PageResponse<AdminUserResponse> users(@RequestParam(required = false) String q,
                                                 @RequestParam(required = false) String role,
                                                 @RequestParam(defaultValue = "0") @Min(0) int page,
                                                 @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        if (role != null && !role.matches("creator|provider|admin")) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "role harus creator, provider, atau admin.");
        }
        return userQueries.page(new AdminUserQueries.Filter(q, role), page, size);
    }

    @Operation(summary = "Detail satu pengguna")
    @GetMapping("/users/{id}")
    public AdminUserResponse user(@PathVariable UUID id) {
        return userQueries.find(id).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Pengguna tidak ditemukan."));
    }

    @Operation(summary = "Kesehatan backend", description = "Uptime, koneksi database, versi skema.")
    @GetMapping("/system")
    public SystemInfoResponse system() {
        Instant now = clock.instant();
        Instant started = Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime());
        boolean databaseOk;
        String schemaVersion = null;
        try {
            schemaVersion = jdbc.sql("""
                            SELECT version FROM flyway_schema_history WHERE success
                             ORDER BY installed_rank DESC LIMIT 1""")
                    .query(String.class).optional().orElse(null);
            databaseOk = true;
        } catch (DataAccessException e) {
            databaseOk = false;
        }
        return new SystemInfoResponse(environment.getProperty("spring.application.name"),
                Arrays.asList(environment.getActiveProfiles()), Runtime.version().toString(), started,
                Duration.between(started, now).toSeconds(), now, properties.timezone(), databaseOk, schemaVersion);
    }
}
