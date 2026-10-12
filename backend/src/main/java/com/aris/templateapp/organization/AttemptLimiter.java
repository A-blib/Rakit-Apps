package com.aris.templateapp.organization;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Membatasi percobaan kode undangan (10 kali per menit per user) agar kode tidak bisa ditebak beruntun.
 * Disimpan di memori: cukup untuk satu server; hilang saat restart, dan itu tidak masalah untuk batas per menit.
 */
@Component
@RequiredArgsConstructor
public class AttemptLimiter {

    static final int MAX_ATTEMPTS = 10;
    static final Duration WINDOW = Duration.ofMinutes(1);

    private final Clock clock;
    private final Map<UUID, Deque<Instant>> attempts = new ConcurrentHashMap<>();

    public void check(UUID userId) {
        Instant now = clock.instant();
        Deque<Instant> recent = attempts.computeIfAbsent(userId, id -> new ArrayDeque<>());
        synchronized (recent) {
            while (!recent.isEmpty() && recent.peekFirst().isBefore(now.minus(WINDOW))) {
                recent.pollFirst();
            }
            if (recent.size() >= MAX_ATTEMPTS) {
                throw new ApiException(ErrorCode.TOO_MANY_ATTEMPTS);
            }
            recent.addLast(now);
        }
    }
}
