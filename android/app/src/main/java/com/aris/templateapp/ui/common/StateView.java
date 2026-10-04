package com.aris.templateapp.ui.common;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aris.templateapp.R;
import com.aris.templateapp.databinding.ViewStateBinding;

/**
 * Tampilan status untuk layar yang memuat data (bagian 8): loading (animasi), kosong (boleh dengan satu tombol aksi), atau error + "Coba lagi".
 * Saat data berhasil dimuat, panggil {@link #hide(View, Runnable)}: loading pahlawan melaju ke kanan, lalu isi layar
 * naik dari bawah (HeroLoading).
 * Loading hanya muncul untuk proses yang lama: setelah jeda {@code loading_show_delay_ms}, lalu tampil minimal
 * {@code loading_min_visible_ms} (res/values/integers.xml).
 */
public class StateView extends FrameLayout {

    private final ViewStateBinding binding;
    private final long showDelayMs;
    private final long minLoadingMs;
    /** Loading diminta tapi belum ditampilkan (masih dalam jeda {@code loading_show_delay_ms}). */
    @Nullable
    private Runnable delayedShow;
    private final HeroLoading heroLoading;
    /** Waktu (SystemClock.uptimeMillis) saat loading mulai tampil; 0 = loading tidak sedang tampil. */
    private long loadingSince;
    /** Perpindahan keadaan yang sedang ditunda sampai waktu minimal loading habis. */
    @Nullable
    private Runnable pending;

    public StateView(@NonNull Context context) {
        this(context, null);
    }

    public StateView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        binding = ViewStateBinding.inflate(LayoutInflater.from(context), this);
        showDelayMs = getResources().getInteger(R.integer.loading_show_delay_ms);
        minLoadingMs = getResources().getInteger(R.integer.loading_min_visible_ms);
        heroLoading = new HeroLoading(binding.animation);
        setVisibility(GONE);
    }

    /**
     * Proses yang cepat tidak perlu animasi (permintaan Aris): loading baru ditampilkan jika data belum datang setelah
     * {@code loading_show_delay_ms}. Selama jeda itu layar dibiarkan kosong (keadaan lama disembunyikan).
     */
    public void showLoading() {
        cancelPending();
        if (loadingSince != 0 || delayedShow != null) {
            return;
        }
        heroLoading.cancel();
        setVisibility(GONE);
        delayedShow = () -> {
            delayedShow = null;
            loadingSince = SystemClock.uptimeMillis();
            setVisibility(VISIBLE);
            binding.message.setText(R.string.state_loading);
            binding.retryButton.setVisibility(View.GONE);
            heroLoading.start();
        };
        postDelayed(delayedShow, showDelayMs);
    }

    public void showEmpty(CharSequence message) {
        afterLoading(() -> show(R.raw.empty, message, null, null));
    }

    /** Kosong + satu tombol aksi, mis. "Hapus filter" atau "Buka panduan". */
    public void showEmpty(CharSequence message, CharSequence actionText, Runnable action) {
        afterLoading(() -> show(R.raw.empty, message, actionText, action));
    }

    public void showError(CharSequence message, Runnable retry) {
        afterLoading(() -> show(R.raw.empty, message, getContext().getString(R.string.action_retry), retry));
    }

    public void hide() {
        afterLoading(() -> {
            heroLoading.cancel();
            setVisibility(GONE);
        });
    }

    /**
     * Data siap: sembunyikan StateView lalu tampilkan {@code content} setelah {@code bind} mengisinya.
     * Jika loading sedang tampil, urutannya: tunggu waktu minimal → pahlawan melaju ke kanan → isi layar naik dari
     * bawah. Jika tidak (mis. muat ulang diam-diam), isi langsung ditampilkan tanpa animasi.
     */
    public void hide(View content, Runnable bind) {
        afterLoading(() -> {
            boolean wasLoading = getVisibility() == VISIBLE && loadingSince != 0;
            Runnable reveal = () -> {
                setVisibility(GONE);
                bind.run();
                content.setVisibility(VISIBLE);
                if (wasLoading) {
                    HeroLoading.slideUpIn(content);
                }
            };
            if (wasLoading) {
                heroLoading.finish(reveal);
            } else {
                reveal.run();
            }
        });
    }

    /**
     * Menjalankan perpindahan keadaan: langsung jika loading belum sempat tampil (proses cepat), atau setelah loading
     * tampil minimal {@code loading_min_visible_ms} agar tidak berkedip.
     */
    private void afterLoading(Runnable change) {
        cancelPending();
        cancelDelayedShow();
        long remaining = loadingSince == 0 ? 0 : minLoadingMs - (SystemClock.uptimeMillis() - loadingSince);
        Runnable run = () -> {
            pending = null;
            change.run();
            loadingSince = 0;
        };
        if (remaining <= 0) {
            run.run();
        } else {
            pending = run;
            postDelayed(run, remaining);
        }
    }

    private void cancelDelayedShow() {
        if (delayedShow != null) {
            removeCallbacks(delayedShow);
            delayedShow = null;
        }
    }

    private void cancelPending() {
        if (pending != null) {
            removeCallbacks(pending);
            pending = null;
        }
    }

    /** Layar ditutup sebelum waktu tunda habis: jangan jalankan perubahan untuk view yang sudah tidak ada. */
    @Override
    protected void onDetachedFromWindow() {
        cancelPending();
        cancelDelayedShow();
        heroLoading.cancel();
        super.onDetachedFromWindow();
    }

    private void show(int animation, CharSequence message, @Nullable CharSequence actionText,
                      @Nullable Runnable retry) {
        heroLoading.cancel();
        setVisibility(VISIBLE);
        binding.animation.setAnimation(animation);
        // Loading memutar sebagian frame saja; animasi lain diputar penuh dan berulang.
        binding.animation.setMinAndMaxProgress(0f, 1f);
        binding.animation.setRepeatCount(com.airbnb.lottie.LottieDrawable.INFINITE);
        // Warna dipasang ulang setiap ganti animasi: pengaturan warna Lottie melekat pada animasi yang sedang
        // dimuat, sehingga hilang saat berganti (mis. loading → kosong) dan garis hitam tak terlihat di mode gelap.
        LottieTint.applyForeground(binding.animation);
        binding.animation.playAnimation();
        binding.message.setText(message);
        binding.retryButton.setText(actionText);
        binding.retryButton.setVisibility(retry == null ? View.GONE : View.VISIBLE);
        binding.retryButton.setOnClickListener(retry == null ? null : v -> retry.run());
    }
}
