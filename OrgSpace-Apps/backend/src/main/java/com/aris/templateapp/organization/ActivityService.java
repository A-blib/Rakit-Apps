package com.aris.templateapp.organization;

import com.aris.templateapp.organization.dto.ActivityPageResponse;
import com.aris.templateapp.organization.dto.ActivityResponse;
import com.aris.templateapp.user.User;
import com.aris.templateapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Menulis dan membaca Riwayat aktivitas. Nama aksi memakai pola {@code objek.kejadian} (mis. {@code member.joined})
 * agar mudah dikelompokkan; app menerjemahkannya menjadi kalimat.
 */
@Service
@RequiredArgsConstructor
public class ActivityService {

    static final int PAGE_SIZE = 20;

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    public void log(UUID organizationId, UUID actorUserId, String action, String targetType, UUID targetId,
                    Map<String, Object> metadata) {
        activityLogRepository.save(new ActivityLog(organizationId, actorUserId, action, targetType, targetId, metadata));
    }

    public void log(UUID organizationId, UUID actorUserId, String action) {
        log(organizationId, actorUserId, action, null, null, null);
    }

    @Transactional(readOnly = true)
    public ActivityPageResponse list(UUID organizationId, int page) {
        Slice<ActivityLog> slice = activityLogRepository.findByOrganizationIdOrderByCreatedAtDesc(
                organizationId, PageRequest.of(Math.max(page, 0), PAGE_SIZE));
        Set<UUID> actorIds = slice.getContent().stream().map(ActivityLog::getActorUserId)
                .filter(Objects::nonNull).collect(Collectors.toCollection(HashSet::new));
        Map<UUID, String> names = userRepository.findAllById(actorIds).stream()
                .collect(Collectors.toMap(User::getId, User::getDisplayName, (a, b) -> a));
        List<ActivityResponse> items = slice.getContent().stream()
                .map(log -> new ActivityResponse(log.getId(),
                        log.getActorUserId() == null ? null : names.get(log.getActorUserId()),
                        log.getAction(), log.getTargetType(), log.getTargetId(), log.getMetadata(), log.getCreatedAt()))
                .toList();
        return new ActivityPageResponse(items, slice.getNumber(), slice.hasNext());
    }
}
