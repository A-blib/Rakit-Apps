package com.aris.templateapp.notification.dto;

import com.aris.templateapp.notification.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(UUID id, NotificationType type, UUID templateId, String title, String body,
                                   boolean read, boolean resolved, Instant createdAt) {
}
