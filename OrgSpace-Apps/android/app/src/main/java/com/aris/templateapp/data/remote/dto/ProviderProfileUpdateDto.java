package com.aris.templateapp.data.remote.dto;

import java.util.List;

/** Body PATCH /providers/me/profile. */
public class ProviderProfileUpdateDto {
    public final String creatorName;
    public final String bio;
    public final String portfolioUrl;
    public final List<String> specialties;

    public ProviderProfileUpdateDto(String creatorName, String bio, String portfolioUrl, List<String> specialties) {
        this.creatorName = creatorName;
        this.bio = bio;
        this.portfolioUrl = portfolioUrl;
        this.specialties = specialties;
    }
}
