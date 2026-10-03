package com.aris.templateapp.ui.auth;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.ui.creator.CreatorDashboardFragment;
import com.aris.templateapp.ui.startup.StartupDecision;

/**
 * Setelah masuk/daftar berhasil, user dibawa ke layar yang sama seperti saat app dibuka (bagian 6.1):
 * onboarding jika belum selesai, atau dashboard sesuai mode terakhir. Semua layar sebelumnya dibuang
 * dari back stack, sehingga tombol kembali tidak membuka layar Masuk lagi.
 */
public final class PostLoginNavigator {

    private PostLoginNavigator() {
    }

    public static void navigate(Fragment fragment, User user) {
        NavController navController = NavHostFragment.findNavController(fragment);
        NavOptions clearAll = new NavOptions.Builder().setPopUpTo(R.id.nav_graph, true).build();
        StartupDecision decision = StartupDecision.forUser(user);
        switch (decision.getDestination()) {
            case PROVIDER_DASHBOARD:
                navController.navigate(R.id.providerDashboardFragment, null, clearAll);
                break;
            case ROLE_SELECT:
                // Layar pilih peran dibuat di Fase 09; sampai saat itu user baru dibawa ke dashboard.
            case CREATOR_DASHBOARD:
            case INTRO:
            default:
                navController.navigate(R.id.creatorDashboardFragment,
                        CreatorDashboardFragment.args(decision.isProviderSuspended()), clearAll);
                break;
        }
    }
}
