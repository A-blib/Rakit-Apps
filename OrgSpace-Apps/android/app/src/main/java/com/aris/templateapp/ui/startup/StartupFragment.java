package com.aris.templateapp.ui.startup;

import android.os.Bundle;
import android.os.SystemClock;
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
import com.aris.templateapp.databinding.FragmentStartupBinding;
import com.aris.templateapp.ui.common.HeroLoading;
import com.aris.templateapp.ui.creator.CreatorDashboardFragment;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Layar awal (permintaan Aris): animasi loading pahlawan selama StartupViewModel menentukan layar pertama.
 * Saat keputusan siap (dan animasi sudah tampil minimal {@code loading_min_duration_ms}), pahlawan melaju ke kanan,
 * lalu layar tujuan — dashboard mode terakhir — naik dari bawah (animasi di action nav_graph.xml).
 * Setelah pindah, layar ini dibuang dari back stack sehingga tombol kembali tidak membawa user ke sini.
 */
@AndroidEntryPoint
public class StartupFragment extends Fragment {

    private FragmentStartupBinding binding;
    private HeroLoading heroLoading;
    private final Runnable finish = this::finishLoading;
    private StartupDecision decision;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStartupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        heroLoading = new HeroLoading(binding.animation);
        heroLoading.start();
        long startedAt = SystemClock.uptimeMillis();
        long minDuration = getResources().getInteger(R.integer.loading_min_duration_ms);

        // ViewModel milik Activity agar keputusan tidak dihitung ulang saat layar dibuat ulang (mis. HP diputar).
        StartupViewModel viewModel = new ViewModelProvider(requireActivity()).get(StartupViewModel.class);
        viewModel.getDecision().observe(getViewLifecycleOwner(), result -> {
            if (result != null && decision == null) {
                decision = result;
                long remaining = minDuration - (SystemClock.uptimeMillis() - startedAt);
                binding.getRoot().postDelayed(finish, Math.max(0, remaining));
            }
        });
    }

    private void finishLoading() {
        heroLoading.finish(() -> {
            if (binding != null) {
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
                navController.navigate(R.id.action_startup_to_role_select);
                break;
            case CREATOR_DASHBOARD:
            default:
                navController.navigate(R.id.action_startup_to_creator_dashboard,
                        CreatorDashboardFragment.args(decision.isProviderSuspended()));
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.getRoot().removeCallbacks(finish);
        heroLoading.cancel();
        binding = null;
    }
}
