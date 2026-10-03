package com.aris.templateapp.data.remote.dto;

import java.util.List;

/**
 * Bentuk JSON UserResponse dari backend. Nama field harus sama persis dengan JSON, karena Gson
 * mencocokkan berdasarkan nama (aturan R8 di keepRules/rules.keep menjaga nama ini di build rilis).
 */
public class UserDto {
    public String id;
    public String displayName;
    public String email;
    public String avatarUrl;
    public String activeMode;
    public boolean onboardingCompleted;
    public List<String> roles;
    public String providerStatus;
    public String providerRejectionReason;
    public CreatorProfileDto creatorProfile;
}
