package com.aris.templateapp.data.remote.dto;

/** Body /users/me/onboarding/creator. Field null tidak dikirim (tombol "Lewati" hanya mengirim displayName). */
public class CreatorOnboardingRequestDto {
    public final String displayName;
    public final String websitePurpose;
    public final String organizationName;

    public CreatorOnboardingRequestDto(String displayName, String websitePurpose, String organizationName) {
        this.displayName = displayName;
        this.websitePurpose = websitePurpose;
        this.organizationName = organizationName;
    }
}
