package com.aris.templateapp.organization.dto;

import com.aris.templateapp.organization.MemberRole;
import com.aris.templateapp.organization.OrgCategory;
import com.aris.templateapp.organization.OrgLevel;
import com.aris.templateapp.organization.OrganizationType;

import java.util.UUID;

/**
 * Satu baris di pemilih ruang kerja. Kategori & jenjang ikut dikirim untuk galeri ("Template untuk {org}", chip
 * kategori sesuai jenjang); keduanya null untuk ruang pribadi.
 */
public record OrganizationSummaryResponse(UUID id, OrganizationType type, String name, String shortName,
                                          String logoUrl, MemberRole myRole, OrgCategory category, OrgLevel level) {
}
