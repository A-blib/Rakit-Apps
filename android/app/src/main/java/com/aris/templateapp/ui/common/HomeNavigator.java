package com.aris.templateapp.ui.common;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.ui.creator.CreatorDashboardFragment;
import com.aris.templateapp.ui.startup.StartupDecision;

/**
 * Membawa user ke "rumah"-nya sesuai aturan bagian 6.1, dipakai setelah masuk/daftar, onboarding, beralih mode,
 * dan keluar. Semua layar sebelumnya dibuang dari back stack, sehingga tombol kembali menutup app
 * (bukan kembali ke layar Masuk atau form yang sudah selesai).
 */
public final class HomeNavigator {

    private HomeNavigator() {
    }

    /** @param user null = tamu */
    public static void navigateHome(Fragment fragment, User user) {
        NavController navController = NavHostFragment.findNavController(fragment);
        NavOptions clearAll = new NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build();
        StartupDecision decision = StartupDecision.forUser(user);
        switch (decision.getDestination()) {
            case ROLE_SELECT:
                navController.navigate(R.id.roleSelectFragment, null, clearAll);
                break;
            case PROVIDER_DASHBOARD:
                navController.navigate(R.id.providerDashboardFragment, null, clearAll);
                break;
            case CREATOR_DASHBOARD:
            case INTRO:
            default:
                navController.navigate(R.id.creatorDashboardFragment,
                        CreatorDashboardFragment.args(decision.isProviderSuspended()), clearAll);
                break;
        }
    }
}
