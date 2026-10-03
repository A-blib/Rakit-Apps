package com.aris.templateapp.ui.startup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.ui.creator.CreatorDashboardFragment;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Layar kosong sementara (tertutup splash) yang menunggu StartupViewModel, lalu pindah ke layar tujuan.
 * Setelah pindah, layar ini dibuang dari back stack sehingga tombol kembali tidak membawa user ke sini.
 */
@AndroidEntryPoint
public class StartupFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return new View(requireContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // ViewModel milik Activity (bukan Fragment) agar sama dengan yang dipakai MainActivity untuk menahan splash.
        StartupViewModel viewModel = new ViewModelProvider(requireActivity()).get(StartupViewModel.class);
        viewModel.getDecision().observe(getViewLifecycleOwner(), decision -> {
            if (decision != null) {
                navigate(decision);
            }
        });
    }

    /** Aksi (action) didefinisikan di nav_graph.xml; masing-masing membuang layar startup dari back stack. */
    private void navigate(StartupDecision decision) {
        NavController navController = NavHostFragment.findNavController(this);
        switch (decision.getDestination()) {
            case INTRO:
                navController.navigate(R.id.action_startup_to_intro);
                break;
            case PROVIDER_DASHBOARD:
                navController.navigate(R.id.action_startup_to_provider_dashboard);
                break;
            case ROLE_SELECT:
                // Layar pilih peran dibuat di Fase 09; sampai saat itu user yang belum onboarding dibawa ke dashboard.
            case CREATOR_DASHBOARD:
            default:
                navController.navigate(R.id.action_startup_to_creator_dashboard,
                        CreatorDashboardFragment.args(decision.isProviderSuspended()));
                break;
        }
    }
}
