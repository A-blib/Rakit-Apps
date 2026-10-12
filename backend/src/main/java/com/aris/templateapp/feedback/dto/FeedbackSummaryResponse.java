package com.aris.templateapp.feedback.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Baris daftar kritik &amp; saran.
 *
 * @param unread  ada pesan baru untuk yang sedang melihat (pengirim: balasan tim; admin: pesan pengirim)
 * @param sender  hanya diisi untuk admin
 */
public record FeedbackSummaryResponse(UUID id, String category, String subject, String status, String senderRole,
                                      Integer rating, String source, boolean unread, int messageCount,
                                      Sender sender, String organizationName, Instant lastMessageAt,
                                      Instant createdAt) {

    public record Sender(UUID id, String displayName, String email) {
    }
}
