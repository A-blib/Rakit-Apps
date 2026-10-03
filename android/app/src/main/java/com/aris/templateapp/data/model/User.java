package com.aris.templateapp.data.model;

import java.util.Collections;
import java.util.Set;

/** Data user untuk dipakai UI. Dibuat dari UserDto oleh UserMapper; tidak bergantung pada bentuk JSON. */
public class User {

    private final String id;
    private final String displayName;
    private final String email;
    private final String avatarUrl;
    private final UserRole activeMode;
    private final boolean onboardingCompleted;
    private final Set<UserRole> roles;
    private final ProviderStatus providerStatus;
    private final String providerRejectionReason;
    private final String websitePurpose;
    private final String organizationName;

    public User(String id, String displayName, String email, String avatarUrl, UserRole activeMode,
                boolean onboardingCompleted, Set<UserRole> roles, ProviderStatus providerStatus,
                String providerRejectionReason, String websitePurpose, String organizationName) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.activeMode = activeMode;
        this.onboardingCompleted = onboardingCompleted;
        this.roles = roles == null ? Collections.emptySet() : Collections.unmodifiableSet(roles);
        this.providerStatus = providerStatus;
        this.providerRejectionReason = providerRejectionReason;
        this.websitePurpose = websitePurpose;
        this.organizationName = organizationName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public UserRole getActiveMode() {
        return activeMode;
    }

    public boolean isOnboardingCompleted() {
        return onboardingCompleted;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    public boolean hasRole(UserRole role) {
        return roles.contains(role);
    }

    /** null jika belum mendaftar sebagai provider. */
    public ProviderStatus getProviderStatus() {
        return providerStatus;
    }

    /** Alasan penolakan; hanya ada jika status REJECTED. */
    public String getProviderRejectionReason() {
        return providerRejectionReason;
    }

    public boolean isProviderSuspended() {
        return providerStatus == ProviderStatus.SUSPENDED;
    }

    public String getWebsitePurpose() {
        return websitePurpose;
    }

    public String getOrganizationName() {
        return organizationName;
    }
}
