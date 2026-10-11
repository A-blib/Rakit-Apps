package com.aris.templateapp.organization.dto;

import com.aris.templateapp.organization.MemberRole;
import com.aris.templateapp.organization.OrganizationType;

import java.util.UUID;

/** Satu baris di pemilih ruang kerja. */
public record OrganizationSummaryResponse(UUID id, OrganizationType type, String name, String shortName,
                                          String logoUrl, MemberRole myRole) {
}
