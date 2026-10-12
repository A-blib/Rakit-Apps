package com.aris.templateapp.monitoring;

import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.monitoring.dto.ActivePlatformsResponse;
import com.aris.templateapp.monitoring.dto.OverviewResponse;
import com.aris.templateapp.monitoring.dto.SignupMethodsResponse;
import com.aris.templateapp.monitoring.dto.TimeseriesResponse;
import com.aris.templateapp.monitoring.dto.TrafficResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin: Monitoring", description = "Statistik pengguna, pertumbuhan, dan lalu lintas API (khusus admin)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private static final String PERIOD = "7d, 30d (bawaan), atau 90d; termasuk hari ini, zona APP_TIMEZONE";

    private final StatsService statsService;

    @Operation(summary = "Ringkasan dashboard", description = "Total saat ini, angka periode vs periode sebelumnya, DAU/WAU/MAU.")
    @GetMapping("/overview")
    public OverviewResponse overview(@Parameter(description = PERIOD) @RequestParam(required = false) String period) {
        return statsService.overview(statsService.range(period));
    }

    @Operation(summary = "Grafik harian satu metrik",
            description = "metric: signups, active_users, new_organizations, new_websites, publishes, template_views, "
                    + "template_downloads, feedback, api_requests, api_errors.")
    @GetMapping("/timeseries")
    public TimeseriesResponse timeseries(@RequestParam String metric,
                                         @Parameter(description = PERIOD) @RequestParam(required = false) String period) {
        return statsService.timeseries(StatsMetric.parse(metric), statsService.range(period));
    }

    @Operation(summary = "Pendaftar baru per metode login (email/Google/GitHub)")
    @GetMapping("/signup-methods")
    public SignupMethodsResponse signupMethods(@Parameter(description = PERIOD) @RequestParam(required = false) String period) {
        return statsService.signupMethods(statsService.range(period));
    }

    @Operation(summary = "Pengguna aktif per platform (Android / web)")
    @GetMapping("/active-platforms")
    public ActivePlatformsResponse activePlatforms(@Parameter(description = PERIOD) @RequestParam(required = false) String period) {
        return statsService.activePlatforms(statsService.range(period));
    }

    @Operation(summary = "Lalu lintas API per hari + endpoint paling sering dipanggil")
    @GetMapping("/traffic")
    public TrafficResponse traffic(@Parameter(description = PERIOD) @RequestParam(required = false) String period) {
        return statsService.traffic(statsService.range(period));
    }
}
