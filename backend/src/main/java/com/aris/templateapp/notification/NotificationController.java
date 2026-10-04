package com.aris.templateapp.notification;

import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.notification.dto.NotificationResponse;
import com.aris.templateapp.notification.dto.UnreadCountResponse;
import com.aris.templateapp.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Notifikasi", description = "Notifikasi di dalam app")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUser currentUser;

    @Operation(summary = "Daftar notifikasi terbaru (maks 50)")
    @GetMapping
    public List<NotificationResponse> list() {
        return notificationService.list(currentUser.id());
    }

    @Operation(summary = "Jumlah notifikasi belum dibaca")
    @GetMapping("/unread-count")
    public UnreadCountResponse unreadCount() {
        return new UnreadCountResponse(notificationService.unreadCount(currentUser.id()));
    }

    @Operation(summary = "Tandai notifikasi sudah dibaca")
    @PatchMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@PathVariable UUID id) {
        notificationService.markRead(currentUser.id(), id);
    }
}
