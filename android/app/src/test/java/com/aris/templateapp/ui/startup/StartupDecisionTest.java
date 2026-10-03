package com.aris.templateapp.ui.startup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.aris.templateapp.data.model.ProviderStatus;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.data.model.UserRole;
import com.aris.templateapp.ui.startup.StartupDecision.Destination;

import org.junit.Test;

import java.util.EnumSet;

/** Semua cabang bagian 6.1 untuk user yang sudah diketahui datanya. */
public class StartupDecisionTest {

    @Test
    public void guestGoesToCreatorDashboard() {
        assertEquals(Destination.CREATOR_DASHBOARD, StartupDecision.forUser(null).getDestination());
        assertEquals(Destination.CREATOR_DASHBOARD, StartupDecision.guest().getDestination());
    }

    @Test
    public void unfinishedOnboardingGoesToRoleSelect() {
        User user = user(false, UserRole.CREATOR, EnumSet.noneOf(UserRole.class), null);

        assertEquals(Destination.ROLE_SELECT, StartupDecision.forUser(user).getDestination());
    }

    @Test
    public void providerModeOpensProviderDashboard() {
        User user = user(true, UserRole.PROVIDER, EnumSet.of(UserRole.CREATOR, UserRole.PROVIDER), ProviderStatus.PENDING);

        StartupDecision decision = StartupDecision.forUser(user);

        assertEquals(Destination.PROVIDER_DASHBOARD, decision.getDestination());
        assertFalse(decision.isProviderSuspended());
    }

    @Test
    public void suspendedProviderIsRedirectedWithMessage() {
        User user = user(true, UserRole.PROVIDER, EnumSet.of(UserRole.PROVIDER), ProviderStatus.SUSPENDED);

        StartupDecision decision = StartupDecision.forUser(user);

        assertEquals(Destination.CREATOR_DASHBOARD, decision.getDestination());
        assertTrue(decision.isProviderSuspended());
    }

    @Test
    public void providerModeWithoutProviderRoleFallsBackToCreator() {
        User user = user(true, UserRole.PROVIDER, EnumSet.of(UserRole.CREATOR), null);

        assertEquals(Destination.CREATOR_DASHBOARD, StartupDecision.forUser(user).getDestination());
    }

    @Test
    public void creatorModeOpensCreatorDashboard() {
        User user = user(true, UserRole.CREATOR, EnumSet.of(UserRole.CREATOR, UserRole.PROVIDER), ProviderStatus.APPROVED);

        StartupDecision decision = StartupDecision.forUser(user);

        assertEquals(Destination.CREATOR_DASHBOARD, decision.getDestination());
        assertFalse(decision.isProviderSuspended());
    }

    private static User user(boolean onboarded, UserRole mode, EnumSet<UserRole> roles, ProviderStatus status) {
        return new User("u-1", "Aris", "aris@mail.com", null, mode, onboarded, roles, status, null, null, null);
    }
}
