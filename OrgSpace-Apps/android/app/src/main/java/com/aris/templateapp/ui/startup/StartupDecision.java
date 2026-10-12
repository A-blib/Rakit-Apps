package com.aris.templateapp.ui.startup;

import com.aris.templateapp.data.model.User;
import com.aris.templateapp.data.model.UserRole;

/**
 * Keputusan layar pertama (bagian 6.1), ditulis sebagai fungsi murni tanpa Android
 * agar mudah diuji dengan unit test biasa.
 */
public final class StartupDecision {

    public enum Destination { INTRO, ROLE_SELECT, CREATOR_DASHBOARD, PROVIDER_DASHBOARD }

    private final Destination destination;
    private final boolean providerSuspended;

    private StartupDecision(Destination destination, boolean providerSuspended) {
        this.destination = destination;
        this.providerSuspended = providerSuspended;
    }

    public Destination getDestination() {
        return destination;
    }

    /** true jika user diarahkan ke dashboard pembuat website KARENA mode provider ditangguhkan (tampilkan pesan). */
    public boolean isProviderSuspended() {
        return providerSuspended;
    }

    public static StartupDecision introFirst() {
        return new StartupDecision(Destination.INTRO, false);
    }

    public static StartupDecision guest() {
        return new StartupDecision(Destination.CREATOR_DASHBOARD, false);
    }

    /**
     * <pre>
     * onboardingCompleted = false              → pilih peran
     * activeMode = provider & punya profil
     *     status != suspended                  → Dashboard Provider
     *     status = suspended                   → Dashboard Pembuat Website + pesan
     * selain itu                               → Dashboard Pembuat Website
     * </pre>
     */
    public static StartupDecision forUser(User user) {
        if (user == null) {
            return guest();
        }
        if (!user.isOnboardingCompleted()) {
            return new StartupDecision(Destination.ROLE_SELECT, false);
        }
        if (user.getActiveMode() == UserRole.PROVIDER && user.hasRole(UserRole.PROVIDER)) {
            return user.isProviderSuspended()
                    ? new StartupDecision(Destination.CREATOR_DASHBOARD, true)
                    : new StartupDecision(Destination.PROVIDER_DASHBOARD, false);
        }
        return new StartupDecision(Destination.CREATOR_DASHBOARD, false);
    }
}
