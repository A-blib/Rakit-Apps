package com.aris.templateapp.organization.dto;

import com.aris.templateapp.organization.MemberRole;

/** Pratinjau sebelum bergabung: cukup nama, logo, dan peran (tanpa daftar anggota). */
public record InvitationPreviewResponse(String organizationName, String logoUrl, MemberRole role) {
}
