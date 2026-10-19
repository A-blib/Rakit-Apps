package com.aris.templateapp.ui.startup;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.FragmentStartupBinding;
import com.aris.templateapp.ui.creator.CreatorDashboardFragment;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Layar awal: Animasi pembuka (entrance animation) yang estetik dan elegan saat pengguna masuk ke aplikasi.
 * Menampilkan logo OrgSpace yang mekar (bloom spring), aura cahaya bernapas (breathing aura),
 * kemunculan judul &amp; tagline bertingkat (staggered cascade), serta transisi mulus ke dashboard tujuan.
 */
@AndroidEntryPoint
public class StartupFragment extends Fragment {

    private FragmentStartupBinding binding;
    private StartupDecision decision;
    private ValueAnimator auraPulseAnimator;
    private long startedAt;
    private boolean isExiting = false;

    private final Runnable checkFinishRunnable = this::tryFinishAnimation;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStartupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        startedAt = SystemClock.uptimeMillis();
        playEntranceAnimation();

        // ViewModel milik Activity agar keputusan tetap konsisten jika layar dibuat ulang
        StartupViewModel viewModel = new ViewModelProvider(requireActivity()).get(StartupViewModel.class);
        viewModel.getDecision().observe(getViewLifecycleOwner(), result -> {
            if (result != null && decision == null) {
                decision = result;
                long minDuration = getResources().getInteger(R.integer.loading_min_duration_ms);
                long elapsed = SystemClock.uptimeMillis() - startedAt;
                long remaining = Math.max(0, minDuration - elapsed);

                if (binding != null) {
                    binding.getRoot().postDelayed(checkFinishRunnable, remaining);
                }
            }
        });
    }

    /**
     * Memutar koreografi animasi masuk yang estetik dan halus:
     * 1. Logo mekar dengan gaya pegas (Overshoot)
     * 2. Titik orbit kuning emas muncul ceria
     * 3. Aura cahaya di belakang logo membesar lalu bernapas lembut
     * 4. Judul dan tagline meluncur naik bertingkat
     * 5. Indikator loading memudar masuk di bagian bawah
     */
    private void playEntranceAnimation() {
        if (binding == null) return;

        float density = getResources().getDisplayMetrics().density;

        // Inisialisasi keadaan awal elemen agar transisi terlihat mulus
        binding.glowAura.setScaleX(0.3f);
        binding.glowAura.setScaleY(0.3f);
        binding.glowAura.setAlpha(0f);

        binding.logoCard.setScaleX(0.5f);
        binding.logoCard.setScaleY(0.5f);
        binding.logoCard.setAlpha(0f);
        binding.logoCard.setTranslationY(30f * density);

        binding.orbitDot.setScaleX(0f);
        binding.orbitDot.setScaleY(0f);

        binding.appName.setAlpha(0f);
        binding.appName.setTranslationY(22f * density);

        binding.appTagline.setAlpha(0f);
        binding.appTagline.setTranslationY(16f * density);

        binding.bottomSection.setAlpha(0f);

        // Animasi kartu logo: mekar dengan pegas Overshoot (700ms)
        binding.logoCard.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .translationY(0f)
                .setDuration(700)
                .setInterpolator(new OvershootInterpolator(1.35f))
                .start();

        // Animasi titik aksen kuning: mekar dengan sedikit pantulan
        binding.orbitDot.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setStartDelay(320)
                .setDuration(450)
                .setInterpolator(new OvershootInterpolator(2.2f))
                .start();

        // Animasi aura cahaya lembut: membesar lalu mulai loop pernapasan (breathing)
        binding.glowAura.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .alpha(0.45f)
                .setDuration(800)
                .setInterpolator(new DecelerateInterpolator(2.0f))
                .withEndAction(this::startAuraBreathing)
                .start();

        // Animasi teks judul "OrgSpace": meluncur naik bertingkat
        binding.appName.animate()
                .alpha(1.0f)
                .translationY(0f)
                .setStartDelay(220)
                .setDuration(550)
                .setInterpolator(new DecelerateInterpolator(2.0f))
                .start();

        // Animasi teks tagline: menyusul judul
        binding.appTagline.animate()
                .alpha(1.0f)
                .translationY(0f)
                .setStartDelay(360)
                .setDuration(550)
                .setInterpolator(new DecelerateInterpolator(2.0f))
                .start();

        // Bagian loading bawah memudar masuk dengan tenang
        binding.bottomSection.animate()
                .alpha(1.0f)
                .setStartDelay(500)
                .setDuration(450)
                .start();
    }

    /**
     * Efek denyut bernapas (breathing pulse) lembut pada aura di belakang logo.
     */
    private void startAuraBreathing() {
        if (binding == null || isExiting) return;

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.05f, 1.25f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.05f, 1.25f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0.28f, 0.55f);

        auraPulseAnimator = ObjectAnimator.ofPropertyValuesHolder(binding.glowAura, scaleX, scaleY, alpha);
        auraPulseAnimator.setDuration(1600);
        auraPulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        auraPulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        auraPulseAnimator.start();
    }

    /**
     * Saat keputusan siap dan durasi minimal animasi telah terpenuhi:
     * Navigasikan langsung ke layar tujuan. Navigation Component akan menjalankan
     * exitAnim (fade-through) dan enterAnim (soft rise) secara simultan dan mulus.
     */
    private void tryFinishAnimation() {
        if (!isAdded() || binding == null || decision == null || isExiting) {
            return;
        }
        isExiting = true;

        if (auraPulseAnimator != null) {
            auraPulseAnimator.cancel();
        }

        navigate(decision);
    }

    /** Aksi (action) didefinisikan di nav_graph.xml; masing-masing membuang layar startup dari back stack. */
    private void navigate(StartupDecision decision) {
        if (!isAdded()) {
            return;
        }
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
        if (binding != null) {
            binding.getRoot().removeCallbacks(checkFinishRunnable);
        }
        if (auraPulseAnimator != null) {
            auraPulseAnimator.cancel();
            auraPulseAnimator = null;
        }
        binding = null;
    }
}
