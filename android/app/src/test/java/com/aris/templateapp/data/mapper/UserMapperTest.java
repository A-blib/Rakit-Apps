package com.aris.templateapp.data.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.aris.templateapp.data.model.ProviderStatus;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.data.model.UserRole;
import com.aris.templateapp.data.remote.dto.CreatorProfileDto;
import com.aris.templateapp.data.remote.dto.UserDto;

import org.junit.Test;

import java.util.List;

public class UserMapperTest {

    @Test
    public void mapsRolesModeAndProviderStatus() {
        UserDto dto = new UserDto();
        dto.id = "u-1";
        dto.displayName = "Aris";
        dto.activeMode = "provider";
        dto.onboardingCompleted = true;
        dto.roles = List.of("creator", "provider");
        dto.providerStatus = "suspended";
        dto.creatorProfile = new CreatorProfileDto();
        dto.creatorProfile.websitePurpose = "sekolah";

        User user = UserMapper.toModel(dto);

        assertEquals(UserRole.PROVIDER, user.getActiveMode());
        assertTrue(user.hasRole(UserRole.CREATOR));
        assertTrue(user.hasRole(UserRole.PROVIDER));
        assertEquals(ProviderStatus.SUSPENDED, user.getProviderStatus());
        assertTrue(user.isProviderSuspended());
        assertEquals("sekolah", user.getWebsitePurpose());
    }

    @Test
    public void newUserWithoutProfiles() {
        UserDto dto = new UserDto();
        dto.activeMode = "creator";
        dto.roles = List.of();

        User user = UserMapper.toModel(dto);

        assertFalse(user.isOnboardingCompleted());
        assertTrue(user.getRoles().isEmpty());
        assertNull(user.getProviderStatus());
        assertNull(user.getWebsitePurpose());
    }
}
