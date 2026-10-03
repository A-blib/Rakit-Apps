package com.aris.templateapp.data.remote.dto;

import java.util.List;

/** Body /users/me/onboarding/provider. */
public class ProviderOnboardingRequestDto {
    public final String creatorName;
    public final String bio;
    public final String portfolioUrl;
    public final List<String> specialties;
    public final boolean agreedToTerms;

    public ProviderOnboardingRequestDto(String creatorName, String bio, String portfolioUrl, List<String> specialties,
                                        boolean agreedToTerms) {
        this.creatorName = creatorName;
        this.bio = bio;
        this.portfolioUrl = portfolioUrl;
        this.specialties = specialties;
        this.agreedToTerms = agreedToTerms;
    }
}
