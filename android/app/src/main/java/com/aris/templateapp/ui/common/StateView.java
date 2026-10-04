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
 * Saat data berhasil dimuat, panggil {@link #hide(Runnable)} dengan kode yang menampilkan isi layar.
 * Animasi loading selalu tampil minimal {@code loading_min_duration_ms} (res/values/integers.xml).
 */
public class StateView extends FrameLayout {

    private final ViewStateBinding binding;
    private final long minLoadingMs;
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
        minLoadingMs = getResources().getInteger(R.integer.loading_min_duration_ms);
        setVisibility(GONE);
    }

    public void showLoading() {
        cancelPending();
        if (loadingSince == 0) {
            loadingSince = SystemClock.uptimeMillis();
            show(R.raw.loading, getContext().getString(R.string.state_loading), null, null);
        }
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
        hide(null);
    }

    /**
     * Menyembunyikan StateView lalu menjalankan {@code thenShowContent} (mis. menampilkan isi layar).
     * Jika loading baru saja muncul, keduanya ditunda sampai waktu minimal loading habis, sehingga isi layar
     * tidak muncul bersamaan dengan animasi yang masih berjalan.
     */
    public void hide(@Nullable Runnable thenShowContent) {
        afterLoading(() -> {
            binding.animation.cancelAnimation();
            setVisibility(GONE);
            if (thenShowContent != null) {
                thenShowContent.run();
            }
        });
    }

    /** Menjalankan perpindahan keadaan sekarang, atau setelah loading tampil selama {@code loading_min_duration_ms}. */
    private void afterLoading(Runnable change) {
        cancelPending();
        long remaining = loadingSince == 0 ? 0 : minLoadingMs - (SystemClock.uptimeMillis() - loadingSince);
        Runnable run = () -> {
            pending = null;
            loadingSince = 0;
            change.run();
        };
        if (remaining <= 0) {
            run.run();
        } else {
            pending = run;
            postDelayed(run, remaining);
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
        super.onDetachedFromWindow();
    }

    private void show(int animation, CharSequence message, @Nullable CharSequence actionText,
                      @Nullable Runnable retry) {
        setVisibility(VISIBLE);
        binding.animation.setAnimation(animation);
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
