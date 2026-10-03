package com.aris.templateapp.data.mapper;

import com.aris.templateapp.data.model.ProviderStatus;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.data.model.UserRole;
import com.aris.templateapp.data.remote.dto.UserDto;

import java.util.EnumSet;
import java.util.Set;

/** Mengubah DTO (bentuk JSON) menjadi model UI. Satu-satunya tempat yang tahu dua bentuk ini. */
public final class UserMapper {

    private UserMapper() {
    }

    public static User toModel(UserDto dto) {
        if (dto == null) {
            return null;
        }
        Set<UserRole> roles = EnumSet.noneOf(UserRole.class);
        if (dto.roles != null) {
            for (String role : dto.roles) {
                roles.add(UserRole.fromValue(role));
            }
        }
        return new User(
                dto.id,
                dto.displayName,
                dto.email,
                dto.avatarUrl,
                UserRole.fromValue(dto.activeMode),
                dto.onboardingCompleted,
                roles,
                ProviderStatus.fromValue(dto.providerStatus),
                dto.creatorProfile == null ? null : dto.creatorProfile.websitePurpose,
                dto.creatorProfile == null ? null : dto.creatorProfile.organizationName);
    }
}
