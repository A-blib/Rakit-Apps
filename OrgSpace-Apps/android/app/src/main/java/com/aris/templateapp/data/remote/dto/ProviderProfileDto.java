package com.aris.templateapp.data.remote.dto;

import java.util.List;

/** Profil provider (GET/PATCH /providers/me/profile). */
public class ProviderProfileDto {
    public String creatorName;
    public String bio;
    public String portfolioUrl;
    public List<String> specialties;
    public String avatarUrl;
    public String email;
    public String status;
    public long publishedCount;
    public long totalDownloads;
}
