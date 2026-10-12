package com.aris.templateapp.data.remote.dto;

/**
 * Body /users/me/onboarding/creator dan PATCH /users/me. Data organisasi dikirim lewat endpoint
 * organisasi, jadi di sini cukup nama tampilan.
 */
public class CreatorOnboardingRequestDto {
    public final String displayName;

    public CreatorOnboardingRequestDto(String displayName) {
        this.displayName = displayName;
    }
}
