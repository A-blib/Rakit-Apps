package com.aris.templateapp.organization.dto;

import com.aris.templateapp.organization.MemberRole;
import com.aris.templateapp.organization.OrgCategory;
import com.aris.templateapp.organization.OrgLevel;
import com.aris.templateapp.organization.OrganizationType;

import java.time.Instant;
import java.util.UUID;

/**
 * Profil lengkap ruang kerja.
 *
 * @param myRole      peran user yang meminta; app memakainya untuk menyembunyikan tombol yang tidak boleh
 * @param memberCount jumlah anggota saat ini
 * @param maxMembers  batas anggota (F4), untuk teks "12/30 anggota"
 */
public record OrganizationResponse(
        UUID id,
        OrganizationType type,
        String name,
        String shortName,
        OrgCategory category,
        OrgLevel level,
        String institutionName,
        String city,
        Integer periodStartYear,
        Integer periodEndYear,
        String logoUrl,
        String address,
        String email,
        String whatsapp,
        String instagram,
        String tiktok,
        String youtube,
        MemberRole myRole,
        long memberCount,
        int maxMembers,
        Instant createdAt) {
}
