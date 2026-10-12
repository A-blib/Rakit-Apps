package com.aris.templateapp.data.model;

import androidx.annotation.Nullable;

/** Profil lengkap ruang kerja (Profil organisasi) beserta peran user yang membukanya. */
public class Organization {

    public final String id;
    public final boolean personal;
    public final String name;
    @Nullable public final String shortName;
    @Nullable public final String category;
    @Nullable public final String level;
    @Nullable public final String institutionName;
    @Nullable public final String city;
    @Nullable public final Integer periodStartYear;
    @Nullable public final Integer periodEndYear;
    @Nullable public final String logoUrl;
    @Nullable public final String address;
    @Nullable public final String email;
    @Nullable public final String whatsapp;
    @Nullable public final String instagram;
    @Nullable public final String tiktok;
    @Nullable public final String youtube;
    public final WorkspaceRole myRole;
    public final long memberCount;
    public final int maxMembers;

    public Organization(String id, boolean personal, String name, @Nullable String shortName, @Nullable String category,
                        @Nullable String level, @Nullable String institutionName, @Nullable String city,
                        @Nullable Integer periodStartYear, @Nullable Integer periodEndYear, @Nullable String logoUrl,
                        @Nullable String address, @Nullable String email, @Nullable String whatsapp,
                        @Nullable String instagram, @Nullable String tiktok, @Nullable String youtube,
                        WorkspaceRole myRole, long memberCount, int maxMembers) {
        this.id = id;
        this.personal = personal;
        this.name = name;
        this.shortName = shortName;
        this.category = category;
        this.level = level;
        this.institutionName = institutionName;
        this.city = city;
        this.periodStartYear = periodStartYear;
        this.periodEndYear = periodEndYear;
        this.logoUrl = logoUrl;
        this.address = address;
        this.email = email;
        this.whatsapp = whatsapp;
        this.instagram = instagram;
        this.tiktok = tiktok;
        this.youtube = youtube;
        this.myRole = myRole;
        this.memberCount = memberCount;
        this.maxMembers = maxMembers;
    }

    /** Ubah profil, kelola anggota, undang: Admin ke atas (tabel peran 3.1). */
    public boolean canManage() {
        return myRole.atLeast(WorkspaceRole.ADMIN);
    }

    /** Serah terima & hapus organisasi: Pemilik saja. */
    public boolean isOwner() {
        return myRole == WorkspaceRole.OWNER;
    }
}
