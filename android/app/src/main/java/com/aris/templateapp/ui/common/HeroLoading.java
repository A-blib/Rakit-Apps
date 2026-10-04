package com.aris.templateapp.ui.common;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.aris.templateapp.R;

/**
 * Memutar animasi loading tiga bagian (res/raw/loading.json, dibuat tools/generate_lottie.py):
 * <ol>
 *   <li>Pembuka (frame 0–45): lingkaran berputar lalu berubah menjadi pahlawan — diputar sekali.</li>
 *   <li>Terbang di tempat (45–165): diulang terus selama data dimuat.</li>
 *   <li>Penutup (165–195): pahlawan melaju ke kanan keluar layar — diputar sekali saat data siap.</li>
 * </ol>
 * Lottie bisa memutar sebagian frame saja lewat {@code setMinAndMaxFrame}, jadi satu file cukup untuk ketiganya.
 */
public final class HeroLoading {

    /** Harus sama dengan LOADING_* di tools/generate_lottie.py. */
    static final int INTRO_END = 45;
    static final int LOOP_END = 165;
    static final int OUTRO_END = 195;

    private final LottieAnimationView view;
    private boolean introPlaying;
    @Nullable
    private Runnable onFinished;

    public HeroLoading(LottieAnimationView view) {
        this.view = view;
    }

    /** Mulai dari pembuka; setelah pembuka selesai, otomatis lanjut ke bagian terbang yang diulang. */
    public void start() {
        onFinished = null;
        view.removeAllAnimatorListeners();
        view.setAnimation(R.raw.loading);
        LottieTint.applyForeground(view);
        introPlaying = true;
        view.setRepeatCount(0);
        view.setMinAndMaxFrame(0, INTRO_END);
        view.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.removeAnimatorListener(this);
                introPlaying = false;
                if (onFinished != null) {
                    playOutro();
                } else {
                    playLoop();
                }
            }
        });
        view.setFrame(0);
        view.playAnimation();
    }

    /**
     * Data siap: putar penutup (melaju ke kanan), lalu jalankan {@code done}. Jika pembuka belum selesai,
     * penutup diputar tepat setelah pembuka, sehingga tidak ada lompatan gambar.
     */
    public void finish(Runnable done) {
        onFinished = done;
        if (!introPlaying) {
            playOutro();
        }
    }

    /** Berhenti tanpa penutup (mis. layar ditutup atau keadaan berganti ke error). */
    public void cancel() {
        onFinished = null;
        view.removeAllAnimatorListeners();
        view.cancelAnimation();
    }

    private void playLoop() {
        view.setMinAndMaxFrame(INTRO_END, LOOP_END);
        view.setRepeatCount(LottieDrawable.INFINITE);
        view.setFrame(INTRO_END);
        view.playAnimation();
    }

    private void playOutro() {
        Runnable done = onFinished;
        view.removeAllAnimatorListeners();
        view.setRepeatCount(0);
        view.setMinAndMaxFrame(LOOP_END, OUTRO_END);
        view.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.removeAnimatorListener(this);
                onFinished = null;
                if (done != null) {
                    done.run();
                }
            }
        });
        view.setFrame(LOOP_END);
        view.playAnimation();
    }

    /** Isi layar "muncul dari bawah": mulai agak di bawah & transparan, lalu naik ke tempatnya. */
    public static void slideUpIn(View content) {
        float distance = content.getResources().getDisplayMetrics().heightPixels * 0.35f;
        content.setAlpha(0f);
        content.setTranslationY(distance);
        content.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(content.getResources().getInteger(R.integer.slide_up_duration_ms))
                .setInterpolator(new DecelerateInterpolator(2f))
                .start();
    }
}
