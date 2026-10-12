package com.aris.templateapp.notification;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

/**
 * Notifikasi di dalam app (alur-provider.md bagian 4.6). Saat ini notifikasi hanya dibuat oleh seeder demo;
 * pembuat sungguhan (hasil pengecekan Upload) dan push notification FCM menyusul bersama fitur Upload.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int LIST_LIMIT = 50;

    private final NotificationRepository repository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, LIST_LIMIT)).stream()
                .map(n -> new NotificationResponse(n.getId(), n.getType(), n.getTemplateId(), n.getTitle(), n.getBody(),
                        n.getReadAt() != null, n.getResolvedAt() != null, n.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(UUID userId) {
        return repository.countByUserIdAndReadAtIsNull(userId);
    }

    @Transactional
    public void markRead(UUID userId, UUID notificationId) {
        Notification notification = repository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (notification.getReadAt() == null) {
            notification.setReadAt(clock.instant());
        }
    }

    @Transactional
    public Notification create(UUID userId, NotificationType type, UUID templateId, String title, String body) {
        return repository.save(new Notification(userId, type, templateId, title, body));
    }
}
