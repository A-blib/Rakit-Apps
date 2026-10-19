package com.aris.templateapp.ui.auth;

import android.content.Context;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aris.templateapp.R;
import com.aris.templateapp.data.model.User;
import com.aris.templateapp.databinding.ViewAuthTransitionBinding;
import com.aris.templateapp.ui.common.HomeNavigator;

/**
 * Pengatur transisi halus dan estetik saat proses autentikasi (masuk/daftar) berhasil.
 * Memberikan jeda waktu (delay) yang anggun agar keyboard tertutup sempurna,
 * logo mekar secara dinamis, dan layar beranda meluncur masuk dengan mulus tanpa hentakan mendadak.
 */
public final class AuthTransitionHelper {

    private AuthTransitionHelper() {
    }

    public static void play(
            @NonNull Fragment fragment,
            @NonNull ViewAuthTransitionBinding transitionBinding,
            @Nullable View contentToHide,
            @Nullable User user,
            boolean isRegister
    ) {
        View root = fragment.getView();
        if (root == null || !fragment.isAdded()) {
            return;
        }

        // 1. Kunci tombol kembali selama proses transisi agar alur tidak terinterupsi
        fragment.requireActivity().getOnBackPressedDispatcher().addCallback(
                fragment.getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // Kunci navigasi mundur saat sedang memuat ruang kerja
                    }
                }
        );

        // 2. Tutup keyboard secepatnya secara halus
        hideKeyboard(fragment);

        // 3. Setel judul dan teks penjelasan
        if (isRegister) {
            transitionBinding.transitionTitle.setText(R.string.register_transition_title);
            transitionBinding.transitionSubtitle.setText(R.string.register_transition_subtitle);
        } else {
            transitionBinding.transitionTitle.setText(R.string.auth_transition_title);
            transitionBinding.transitionSubtitle.setText(R.string.auth_transition_subtitle);
        }

        // 4. Sembunyikan konten form secara halus jika ada
        if (contentToHide != null) {
            contentToHide.animate()
                    .alpha(0f)
                    .setDuration(220)
                    .start();
        }

        // 5. Tampilkan kontainer transisi dengan animasi mekar (scale & alpha)
        View container = transitionBinding.getRoot();
        container.setVisibility(View.VISIBLE);
        container.setAlpha(0f);
        container.animate()
                .alpha(1f)
                .setDuration(260)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        float density = fragment.getResources().getDisplayMetrics().density;

        // Inisialisasi posisi & skala elemen awal
        transitionBinding.glowAura.setScaleX(0.4f);
        transitionBinding.glowAura.setScaleY(0.4f);
        transitionBinding.glowAura.setAlpha(0f);

        transitionBinding.logoCard.setScaleX(0.55f);
        transitionBinding.logoCard.setScaleY(0.55f);
        transitionBinding.logoCard.setAlpha(0f);
        transitionBinding.logoCard.setTranslationY(24f * density);

        transitionBinding.orbitDot.setScaleX(0f);
        transitionBinding.orbitDot.setScaleY(0f);

        transitionBinding.transitionTitle.setAlpha(0f);
        transitionBinding.transitionTitle.setTranslationY(16f * density);

        transitionBinding.transitionSubtitle.setAlpha(0f);
        transitionBinding.transitionSubtitle.setTranslationY(12f * density);

        transitionBinding.loadingIndicator.setAlpha(0f);

        // Koreografi animasi masuk bertingkat
        // Logo card: mekar pegas
        transitionBinding.logoCard.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .translationY(0f)
                .setDuration(550)
                .setInterpolator(new OvershootInterpolator(1.4f))
                .start();

        // Orbit dot: mekar aksen
        transitionBinding.orbitDot.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setStartDelay(180)
                .setDuration(350)
                .setInterpolator(new OvershootInterpolator(2.0f))
                .start();

        // Aura cahaya
        transitionBinding.glowAura.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .alpha(0.45f)
                .setDuration(600)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Judul status
        transitionBinding.transitionTitle.animate()
                .alpha(1.0f)
                .translationY(0f)
                .setStartDelay(150)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Subtitle
        transitionBinding.transitionSubtitle.animate()
                .alpha(1.0f)
                .translationY(0f)
                .setStartDelay(240)
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Loading indicator
        transitionBinding.loadingIndicator.animate()
                .alpha(1.0f)
                .setStartDelay(320)
                .setDuration(300)
                .start();

        // Jeda waktu yang cukup untuk meresapi transisi (UI/UX Pro Max) dan keyboard tertutup rapi
        int delayMs = fragment.getResources().getInteger(R.integer.auth_transition_min_duration_ms);
        container.postDelayed(() -> {
            if (fragment.isAdded()) {
                HomeNavigator.navigateHome(fragment, user);
            }
        }, delayMs);
    }

    private static void hideKeyboard(Fragment fragment) {
        View view = fragment.getView();
        if (view != null && fragment.getContext() != null) {
            InputMethodManager imm = (InputMethodManager)
                    fragment.requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}
