package com.aris.templateapp.organization.dto;

import com.aris.templateapp.organization.MemberRole;

import java.time.Instant;
import java.util.UUID;

public record MemberResponse(UUID userId, String displayName, String avatarUrl, MemberRole role,
                             String positionTitle, Instant joinedAt) {
}
